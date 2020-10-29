package com.spider.amazon.service;

import com.spider.amazon.dto.AmazonAdConsumeItemDTO;
import com.spider.amazon.dto.AmazonAdConsumeSettingDTO;
import com.spider.amazon.dto.AmazonAdDTO;
import com.spider.amazon.model.AmazonAdConsumeItemDO;
import com.spider.amazon.model.AmazonAdConsumeSettingDO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface AmazonAdService {

    /**
     * Get all setting
     *
     * @return
     */
    List<AmazonAdConsumeSettingDTO> getAllSetting();

    /**
     * Get all active setting
     *
     * @return
     */
    List<AmazonAdConsumeSettingDTO> getAllActiveSetting();

    /**
     * Get Setting by id
     *
     * @param settingId
     * @return
     */
    AmazonAdConsumeSettingDTO getSettingById(Long settingId);

    /**
     * Get all setting by created user id
     *
     * @param userId
     * @return
     */
    List<AmazonAdConsumeSettingDTO> getAllSettingByCreatedUser(Long userId);

    /**
     * Get all active setting by created user id
     *
     * @param userId
     * @return
     */
    List<AmazonAdConsumeSettingDTO> getAllActiveSettingByCreatedUser(Long userId);

    /**
     * Create new setting
     *
     * @param newSetting
     */
    void insertSetting(Long userId, AmazonAdConsumeSettingDTO newSetting);

    /**
     * Update setting
     *
     * @param updateSetting
     */
    void updateSetting(Long userId, AmazonAdConsumeSettingDTO updateSetting);

    /**
     * Active ad consume
     *
     * @param settingId
     */
    void startAdConsume(Long userId, Long settingId);

    /**
     * Disactive the ad consume
     *
     * @param settingId
     */
    void stopAdConsume(Long userId, Long settingId);

    /**
     * Create new item in setting
     *
     * @param newItem
     */
    void insertAdConsumeItem(Long userId, AmazonAdConsumeItemDTO newItem);

    /**
     * update item setting
     *
     * @param item
     */
    void updateAdConsumeItem(Long userId, AmazonAdConsumeItemDTO item);

    /**
     * Set item in while list
     *
     * @param itemId
     */
    void whileListItem(Long userId, Long itemId);

    /**
     * Set item in black list
     *
     * @param itemId
     */
    void blackListItem(Long userId, Long itemId);

    /**
     * Remove setting
     *
     * @param settingId
     */
    void removeSetting(Long userId, Long settingId);

    /**
     * Remove item
     *
     * @param itemId
     */
    void removeItem(Long userId, Long itemId);

//    /**
//     * Check the ad consume or not
//     *
//     * @param consumeSettingDTO
//     * @param amazonAdDTO
//     * @return
//     */
//    boolean consume(AmazonAdConsumeSettingDTO consumeSettingDTO, AmazonAdDTO amazonAdDTO);
//
//    /**
//     * Return the setting want to consume this ad
//     *
//     * @param amazonAdDTO
//     * @return
//     */
//    List<AmazonAdConsumeSettingDTO> consume(AmazonAdDTO amazonAdDTO);

    List<AmazonAdConsumeSettingDTO> consume(AmazonAdDTO amazonAdDTO, List<AmazonAdConsumeSettingDTO> settingDTOS);

    /**
     * Insert log to database
     *
     * @param amazonAdDTO
     */
    void insertAdConsumeLog(AmazonAdDTO amazonAdDTO);

}
