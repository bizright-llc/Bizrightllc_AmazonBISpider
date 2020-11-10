package com.spider.amazon.service.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.spider.amazon.dto.AmazonAdConsumeItemDTO;
import com.spider.amazon.dto.AmazonAdConsumeSettingDTO;
import com.spider.amazon.dto.AmazonAdDTO;
import com.spider.amazon.mapper.AmazonAdConsumeSettingDOMapper;
import com.spider.amazon.mapper.UserDOMapper;
import com.spider.amazon.model.AmazonAdConsumeItemDO;
import com.spider.amazon.model.AmazonAdConsumeLogDO;
import com.spider.amazon.model.AmazonAdConsumeSettingDO;
import com.spider.amazon.model.UserDO;
import com.spider.amazon.service.AmazonAdService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;

/**
 * The service deal with the amazon ad consume setting
 */
@Service
public class AmazonAdServiceImpl implements AmazonAdService {

    private PlatformTransactionManager transactionManager;

    private AmazonAdConsumeSettingDOMapper amazonAdConsumeSettingDOMapper;

    private UserDOMapper userDOMapper;

    private ModelMapper modelMapper;

//    private BlockingQueue<AmazonAdDTO> insertLogQueue;

    private BlockingQueue<AmazonAdConsumeLogDO> logQueue;

    @Autowired
    public AmazonAdServiceImpl(PlatformTransactionManager transactionManager, AmazonAdConsumeSettingDOMapper amazonAdConsumeSettingDOMapper, UserDOMapper userDOMapper, ModelMapper modelMapper) {
        this.transactionManager = transactionManager;
        this.amazonAdConsumeSettingDOMapper = amazonAdConsumeSettingDOMapper;
        this.userDOMapper = userDOMapper;
        this.modelMapper = modelMapper;
    }

    @Override
    public List<AmazonAdConsumeSettingDTO> getAllSetting() {
        return amazonAdConsumeSettingDOMapper.getAllSetting().stream().map(this::settingDOToDto).collect(Collectors.toList());
    }

    @Override
    public List<AmazonAdConsumeSettingDTO> getAllActiveSetting() {

        List<AmazonAdConsumeSettingDTO> activeAdConsumeSetting = amazonAdConsumeSettingDOMapper.getAllActiveSetting().stream().map(this::settingDOToDto).collect(Collectors.toList());

        return activeAdConsumeSetting;
    }

    @Override
    public AmazonAdConsumeSettingDTO getSettingById(Long settingId) {
        return settingDOToDto(amazonAdConsumeSettingDOMapper.getSettingById(settingId));
    }

    @Override
    public List<AmazonAdConsumeSettingDTO> getAllSettingByCreatedUser(Long userId) {
        return amazonAdConsumeSettingDOMapper.getAllSettingByCreatedUser(userId).stream().map(this::settingDOToDto).collect(Collectors.toList());
    }

    @Override
    public List<AmazonAdConsumeSettingDTO> getAllActiveSettingByCreatedUser(Long userId) {
        return amazonAdConsumeSettingDOMapper.getAllSettingByCreatedUser(userId).stream().map(this::settingDOToDto).collect(Collectors.toList());
    }

    @Override
    public void insertSetting(Long userId, AmazonAdConsumeSettingDTO newSetting) {
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);

        transactionTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {

                UserDO userDO = userDOMapper.getUserAccountByUserId(userId);

                if (userDO == null) {
                    throw new IllegalArgumentException(String.format("[insertSetting] user id %s not exist", userId));
                }

                AmazonAdConsumeSettingDO newSettingDO = settingDtoToDO(newSetting);

                newSettingDO.setCreatedBy(userDO.getUserid().toString());
                newSettingDO.setUpdatedBy(userDO.getUserid().toString());

                amazonAdConsumeSettingDOMapper.insertSetting(newSettingDO);
            }
        });

    }

    @Override
    public void updateSetting(Long userId, AmazonAdConsumeSettingDTO updateSetting) {
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);

        transactionTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {

                UserDO userDO = userDOMapper.getUserAccountByUserId(userId);

                if (userDO == null) {
                    throw new IllegalArgumentException(String.format("[insertSetting] user id %s not exist", userId));
                }

                AmazonAdConsumeSettingDO oldSetting = amazonAdConsumeSettingDOMapper.getSettingById(updateSetting.getId());

                if (oldSetting == null) {
                    throw new IllegalArgumentException(String.format("[insertSetting] setting id %s not exist", updateSetting.getId()));
                }

                oldSetting.setName(updateSetting.getName());
                oldSetting.setDescription(updateSetting.getDescription());
                oldSetting.setSearchWords(updateSetting.getSearchWords());

                oldSetting.setUpdatedBy(userId.toString());

                amazonAdConsumeSettingDOMapper.updateSetting(oldSetting);
            }
        });
    }

    @Override
    public void startAdConsume(Long userId, Long settingId) {
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);

        transactionTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {

                UserDO userDO = userDOMapper.getUserAccountByUserId(userId);

                if (userDO == null) {
                    throw new IllegalArgumentException(String.format("[startAdConsume] user id %s not exist", userId));
                }

                AmazonAdConsumeSettingDO oldSetting = amazonAdConsumeSettingDOMapper.getSettingById(settingId);

                if (oldSetting == null) {
                    throw new IllegalArgumentException(String.format("[startAdConsume] setting id %s not exist", settingId));
                }

                oldSetting.setActive(true);
                oldSetting.setUpdatedBy(userId.toString());

                amazonAdConsumeSettingDOMapper.updateSetting(oldSetting);
            }
        });
    }

    @Override
    public void stopAdConsume(Long userId, Long settingId) {
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);

        transactionTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {

                UserDO userDO = userDOMapper.getUserAccountByUserId(userId);

                if (userDO == null) {
                    throw new IllegalArgumentException(String.format("[stopAdConsume] user id %s not exist", userId));
                }

                AmazonAdConsumeSettingDO oldSetting = amazonAdConsumeSettingDOMapper.getSettingById(settingId);

                if (oldSetting == null) {
                    throw new IllegalArgumentException(String.format("[stopAdConsume] setting id %s not exist", settingId));
                }

                oldSetting.setActive(true);
                oldSetting.setUpdatedBy(userId.toString());

                amazonAdConsumeSettingDOMapper.updateSetting(oldSetting);
            }
        });
    }

    @Override
    public void insertAdConsumeItem(Long userId, AmazonAdConsumeItemDTO newItem) {

        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);

        transactionTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {

                UserDO userDO = userDOMapper.getUserAccountByUserId(userId);

                if (userDO == null) {
                    throw new IllegalArgumentException(String.format("[insertSetting] user id %s not exist", userId));
                }

                AmazonAdConsumeItemDO newSettingDO = itemDtoToDO(newItem);

                newSettingDO.setCreatedBy(userId.toString());
                newSettingDO.setUpdatedBy(userId.toString());

                amazonAdConsumeSettingDOMapper.insertAdConsumeItem(newSettingDO);
            }
        });
    }

    @Override
    public void updateAdConsumeItem(Long userId, AmazonAdConsumeItemDTO item) {

        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);

        transactionTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {

                UserDO userDO = userDOMapper.getUserAccountByUserId(userId);

                if (userDO == null) {
                    throw new IllegalArgumentException(String.format("[updateAdConsumeItem] user id %s not exist", userId));
                }

                AmazonAdConsumeItemDO oldItem = amazonAdConsumeSettingDOMapper.getSettingItemById(item.getId());

                if (oldItem == null) {
                    throw new IllegalArgumentException(String.format("[updateAdConsumeItem] item id %s not exist", item.getId()));
                }

                oldItem.setName(item.getName());
                oldItem.setAsin(item.getAsin());
                oldItem.setKeyword(item.getKeyword());
                oldItem.setUpdatedBy(userId.toString());

                amazonAdConsumeSettingDOMapper.updateAdConsumeItem(oldItem);
            }
        });

    }

    @Override
    public void whileListItem(Long userId, Long itemId) {
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);

        transactionTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {

                UserDO userDO = userDOMapper.getUserAccountByUserId(userId);

                if (userDO == null) {
                    throw new IllegalArgumentException(String.format("[whileListItem] user id %s not exist", userId));
                }

                AmazonAdConsumeItemDO oldItem = amazonAdConsumeSettingDOMapper.getSettingItemById(itemId);

                if (oldItem == null) {
                    throw new IllegalArgumentException(String.format("[whileListItem] item id %s not exist", itemId));
                }

                oldItem.setConsume(false);
                oldItem.setUpdatedBy(userId.toString());

                amazonAdConsumeSettingDOMapper.updateAdConsumeItem(oldItem);
            }
        });
    }

    @Override
    public void blackListItem(Long userId, Long itemId) {
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);

        transactionTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {

                UserDO userDO = userDOMapper.getUserAccountByUserId(userId);

                if (userDO == null) {
                    throw new IllegalArgumentException(String.format("[blackListItem] user id %s not exist", userId));
                }

                AmazonAdConsumeItemDO oldItem = amazonAdConsumeSettingDOMapper.getSettingItemById(itemId);

                if (oldItem == null) {
                    throw new IllegalArgumentException(String.format("[blackListItem] item id %s not exist", itemId));
                }

                oldItem.setConsume(true);
                oldItem.setUpdatedBy(userId.toString());

                amazonAdConsumeSettingDOMapper.updateAdConsumeItem(oldItem);
            }
        });
    }

    @Override
    public void removeSetting(Long userId, Long settingId) {
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);

        transactionTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {

                UserDO userDO = userDOMapper.getUserAccountByUserId(userId);

                if (userDO == null) {
                    throw new IllegalArgumentException(String.format("[removeSetting] user id %s not exist", userId));
                }

                AmazonAdConsumeSettingDO oldSetting = amazonAdConsumeSettingDOMapper.getSettingById(settingId);

                if (oldSetting == null) {
                    throw new IllegalArgumentException(String.format("[removeSetting] setting id %s not exist", settingId));
                }

                oldSetting.setRemoved(true);
                oldSetting.setUpdatedBy(userId.toString());

                amazonAdConsumeSettingDOMapper.updateSetting(oldSetting);
            }
        });
    }

    @Override
    public void removeItem(Long userId, Long itemId) {
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);

        transactionTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {

                UserDO userDO = userDOMapper.getUserAccountByUserId(userId);

                if (userDO == null) {
                    throw new IllegalArgumentException(String.format("[removeItem] user id %s not exist", userId));
                }

                AmazonAdConsumeItemDO oldItem = amazonAdConsumeSettingDOMapper.getSettingItemById(itemId);

                if (oldItem == null) {
                    throw new IllegalArgumentException(String.format("[removeItem] item id %s not exist", itemId));
                }

                oldItem.setRemoved(true);
                oldItem.setUpdatedBy(userId.toString());

                amazonAdConsumeSettingDOMapper.updateAdConsumeItem(oldItem);
            }
        });
    }

    /**
     * @param amazonAdDTO
     * @param settingDTOS
     * @return
     */
    @Override
    public List<AmazonAdConsumeSettingDTO> consume(AmazonAdDTO amazonAdDTO, List<AmazonAdConsumeSettingDTO> settingDTOS) {

        if (settingDTOS == null) {
            return new ArrayList<AmazonAdConsumeSettingDTO>();
        }

        List<AmazonAdConsumeSettingDTO> result = new ArrayList<>();

        for (AmazonAdConsumeSettingDTO adConsumeSettingDTO : settingDTOS) {
            if (consume(amazonAdDTO, adConsumeSettingDTO)) {
                result.add(adConsumeSettingDTO);
            }
        }

        return result;
    }

    /**
     * Check amazon ad need consume or not
     * return true means need consume
     * return false means dont need consume
     *
     * @param amazonAdDTO
     * @param setting
     * @return
     */
    @Override
    public boolean consume(AmazonAdDTO amazonAdDTO, AmazonAdConsumeSettingDTO setting) {

        if(amazonAdDTO == null || setting == null){
            return false;
        }

        if (isBlack(amazonAdDTO, setting) && !isWhite(amazonAdDTO, setting)) {
            return true;
        }

        return false;

    }

    @Override
    public void insertAdConsumeLog(AmazonAdDTO amazonAdDTO) {
        if (logQueue == null) {
            this.logQueue = new LinkedBlockingQueue<>();
        }

        AmazonAdConsumeLogDO newLog = new AmazonAdConsumeLogDO();
        newLog.setTitle(amazonAdDTO.getTitle());
        newLog.setAsin(amazonAdDTO.getAsin());
        newLog.setType(amazonAdDTO.getType());
        newLog.setBrand(amazonAdDTO.getBrand());
        newLog.setSettingId(amazonAdDTO.getSettingId().toString());
        newLog.setCreatedAt(LocalDateTime.now());
        newLog.setUpdatedAt(LocalDateTime.now());
        newLog.setCreatedBy("system");
        newLog.setUpdatedBy("system");

        logQueue.offer(newLog);

        if (logQueue.size() >= 20) {

            List<AmazonAdConsumeLogDO> logToPersist = new ArrayList<>();

            logQueue.drainTo(logToPersist);

            CompletableFuture.runAsync(() -> {
                try{
                    insertAdConsumeLogToDB(logToPersist);
                }catch (Exception ex){
                    ex.printStackTrace();
                }
            });

        }

    }

    private void insertAdConsumeLogToDB(List<AmazonAdConsumeLogDO> adConsumeList){

        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);

        transactionTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {
                adConsumeList.stream().forEach(l -> {
                    amazonAdConsumeSettingDOMapper.insertLog(l);
                });
            }
        });

    }

    /**
     * 判断是否在黑名单中，在黑名单中进行消耗
     *
     * @return
     */
    private boolean isBlack(AmazonAdDTO amazonAd, AmazonAdConsumeSettingDTO consumeSettingDTO) {

        List<AmazonAdConsumeItemDTO> blackList = consumeSettingDTO.getItems().stream().filter(i -> i.getConsume() == true).collect(Collectors.toList());

        for (AmazonAdConsumeItemDTO blackAdProduct : blackList) {
            if (containsInfo(amazonAd.getAsin(), blackAdProduct.getAsin()) ||
                    containsInfo(amazonAd.getTitle(), blackAdProduct.getName()) ||
                    containsInfo(amazonAd.getTitle(), blackAdProduct.getKeyword())) {
                return true;
            }
        }

        return false;
    }

    private boolean isWhite(AmazonAdDTO amazonAd, AmazonAdConsumeSettingDTO consumeSettingDTO) {

        List<AmazonAdConsumeItemDTO> whiteList = consumeSettingDTO.getItems().stream().filter(i -> i.getConsume() == false).collect(Collectors.toList());

        for (AmazonAdConsumeItemDTO adSetting : whiteList) {
            if (containsInfo(amazonAd.getAsin(), adSetting.getAsin()) ||
                    containsInfo(amazonAd.getTitle(), adSetting.getName()) ||
                    containsInfo(amazonAd.getTitle(), adSetting.getKeyword())) {
                return true;
            }
        }

        return false;
    }

    /**
     * 是否包含所含信息
     *
     * @param fullText
     * @param containsText
     * @return
     */
    private boolean containsInfo(String fullText, String containsText) {
        if (ObjectUtil.isEmpty(fullText) || ObjectUtil.isEmpty(containsText)) {
            return false;
        }
        return StrUtil.containsAnyIgnoreCase(fullText, containsText);
    }

    private AmazonAdConsumeSettingDTO settingDOToDto(AmazonAdConsumeSettingDO amazonAdConsumeSettingDO) {
        return modelMapper.map(amazonAdConsumeSettingDO, AmazonAdConsumeSettingDTO.class);
    }

    private AmazonAdConsumeSettingDO settingDtoToDO(AmazonAdConsumeSettingDTO amazonAdConsumeSettingDTO) {
        return modelMapper.map(amazonAdConsumeSettingDTO, AmazonAdConsumeSettingDO.class);
    }

    private AmazonAdConsumeItemDTO itemDOToDto(AmazonAdConsumeItemDO amazonAdConsumeItemDO) {
        return modelMapper.map(amazonAdConsumeItemDO, AmazonAdConsumeItemDTO.class);
    }

    private AmazonAdConsumeItemDO itemDtoToDO(AmazonAdConsumeItemDTO amazonAdConsumeItemDTO) {
        return modelMapper.map(amazonAdConsumeItemDTO, AmazonAdConsumeItemDO.class);
    }
}
