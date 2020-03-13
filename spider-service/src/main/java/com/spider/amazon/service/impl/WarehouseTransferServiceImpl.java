package com.spider.amazon.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.common.exception.ServiceException;
import com.spider.amazon.cons.RespErrorEnum;
import com.spider.amazon.dto.GetWarehouseTransferDTO;
import com.spider.amazon.mapper.FbaPoInfoDOMapper;
import com.spider.amazon.model.FbaPoInfoDO;
import com.spider.amazon.service.IBopService;
import com.spider.amazon.service.IWarehouseTransferService;
import com.spider.amazon.vo.GetWarehouseTransferRepResultDataVO;
import com.spider.amazon.vo.GetWarehouseTransferRepVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class WarehouseTransferServiceImpl implements IWarehouseTransferService {

    @Autowired
    private IBopService bopServiceImpl;

    @Autowired
    private SqlSessionTemplate sqlSessionTemplate;

    @Autowired
    private FbaPoInfoDOMapper fbaPoInfoDOMapper;

    private final static int BATCHCOUNT=100;

    /**
     * 获取Warehouse Transfer信息
     * @param requestDTO
     */
    @Override
    public void getWarehouseTransfer(GetWarehouseTransferDTO requestDTO) {
        if (ObjectUtil.isEmpty(requestDTO)) {
            requestDTO=GetWarehouseTransferDTO.builder()
                    .pageNo(1).pageSize(10000).build();
        }
        int result=fbaPoInfoDOMapper.deleteAll();
        GetWarehouseTransferRepVO responseVO = bopServiceImpl.callBopGetWarehouseTransfer(requestDTO);
        dealResult(transResVo2ResDo(responseVO));
    }


    /**
     * 处理请求结果
     *
     * @param members 1.数据校验（暂无传输安全机制）
     *                2.数据处理
     *                3.数据入库 （支持数据快速入库）
     */
    public void dealResult(List<FbaPoInfoDO> members) {

        // 1.数据校验
        if (ObjectUtil.isEmpty(members)) {
            if (log.isInfoEnabled()){
                log.info("服务器无返回数据");
            }
//            throw new ServiceException(RespErrorEnum.SERVICE_NO_RESPOND.getSubStatusCode(), RespErrorEnum.SERVICE_NO_RESPOND.getSubStatusMsg());
            return ;        }

        // 2.数据处理
        int result = 1;
        SqlSession batchSqlSession = null;
        try {
            batchSqlSession = this.sqlSessionTemplate
                    .getSqlSessionFactory()
                    .openSession(ExecutorType.BATCH, false);// 获取批量方式的sqlsession
            //通过新的session获取mapper
            FbaPoInfoDOMapper mapper = batchSqlSession.getMapper(FbaPoInfoDOMapper.class);

            int batchCount = BATCHCOUNT;// 每批commit的个数
            int batchLastIndex = batchCount;// 每批最后一个的下标

            for (int index = 0; index < members.size(); ) {
                if (batchLastIndex >= members.size()) {
                    batchLastIndex = members.size();

                    result = result + mapper.insertBatch(members.subList(index, batchLastIndex));
                    batchSqlSession.commit();
                    //清理缓存，防止溢出
                    batchSqlSession.clearCache();
                    if (log.isInfoEnabled()) {
                        log.info("index:" + index + " batchLastIndex:" + batchLastIndex);
                    }
                    break;// 数据插入完毕，退出循环
                } else {

                    result = result + mapper.insertBatch(members.subList(index, batchLastIndex));
                    batchSqlSession.commit();
                    //清理缓存，防止溢出
                    batchSqlSession.clearCache();
                    if (log.isInfoEnabled()) {
                        log.info("index:" + index + " batchLastIndex:" + batchLastIndex);
                    }
                    index = batchLastIndex;// 设置下一批下标
                    batchLastIndex = index + (batchCount - 1);
                }
                if (log.isInfoEnabled()) {
                    log.info("=============>result=[" + result + "] begin=[" + index + "] end=[" + batchLastIndex + "]");
                }
            }
            batchSqlSession.commit();
        } catch (Exception e) {
            throw new ServiceException(RespErrorEnum.SERVICE_DATA_EXPC.getSubStatusCode(), RespErrorEnum.SERVICE_DATA_EXPC.getSubStatusMsg(), e);
        } finally {
            batchSqlSession.close();
        }

        return;
    }

    /**
     * 转换列表对象
     * @param responseVO
     * @return
     */
    public List<FbaPoInfoDO> transResVo2ResDo(GetWarehouseTransferRepVO responseVO) {
        List<FbaPoInfoDO> reulstList = new ArrayList<>();

        for(GetWarehouseTransferRepResultDataVO resultDataVO :responseVO.getResult().getDataResult()) {
            reulstList.add(FbaPoInfoDO.builder()
                    .asin(resultDataVO.getASIN())
                    .biPoRef(null)
                    .estsenddate(DateUtil.parse(StrUtil.str(resultDataVO.getEstSendDate()).replace("T"," ")))
                    .insertTime(DateUtil.date())
                    .itemnum(resultDataVO.getItemNum())
                    .pendingqty(resultDataVO.getPendingQty())
                    .rcvdate(DateUtil.parse(StrUtil.str(resultDataVO.getReceiveDate()).replace("T"," ")))
                    .rcvwarehouse(resultDataVO.getReceiveWarehouse())
                    .receiveqty(resultDataVO.getReceiveQty())
                    .senddate(DateUtil.parse(StrUtil.str(resultDataVO.getSendDate()).replace("T"," ")))
                    .sendwarehouse(resultDataVO.getSendWarehouse())
                    .sentqty(resultDataVO.getSentQty())
                    .status(resultDataVO.getStatus())
                    .warehouse(resultDataVO.getWarehouse())
                    .wtrefnum(resultDataVO.getWTRefNum())
                    .estrcvdate(DateUtil.parse(StrUtil.str(resultDataVO.getEstReceiveDate()).replace("T"," ")))
                    .build());
        }

        return reulstList;
    }

}
