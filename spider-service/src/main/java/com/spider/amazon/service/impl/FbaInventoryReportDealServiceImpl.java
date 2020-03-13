package com.spider.amazon.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ObjectUtil;
import com.common.exception.ServiceException;
import com.spider.amazon.cons.RespErrorEnum;
import com.spider.amazon.mapper.FBAInventoryInfoDOMapper;
import com.spider.amazon.model.FBAInventoryInfoDO;
import com.spider.amazon.service.IFbaInventoryReportDealService;
import com.spider.amazon.utils.CSVUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @ClassName FbaInventoryReportDealImpl
 * @Desc Fba库存数据入库处理
 */
@Service
@Slf4j
public class FbaInventoryReportDealServiceImpl implements IFbaInventoryReportDealService {


    @Autowired
    private FBAInventoryInfoDOMapper fbaInventoryInfoDOMapper;

    @Autowired
    private SqlSessionTemplate sqlSessionTemplate;

    private final int startrow=1;
    private final int endrow=10000;
    private final int colnum=21;

    /**
     * 入库
     * @param fileName
     * @param filePath
     */
    @Override
    public void dealFbaInventoryReport(String fileName, String filePath, int offerSetDay)  {

        // 0.检查文件是否存在
        if (!FileUtil.exist(filePath+fileName)) {
            throw new ServiceException(RespErrorEnum.FILE_NOT_EXIT.getSubStatusCode(), RespErrorEnum.FILE_NOT_EXIT.getSubStatusMsg());
        }

        // 1.检查当日入库文件是否存在,存在则直接返回，否则进行入库
        Date date=DateUtil.offsetDay(DateUtil.date(),offerSetDay);
        int totNum=fbaInventoryInfoDOMapper.selectCountByDate(date);
        if (totNum!=0) {
            if (log.isInfoEnabled()) {
                log.info("step42=>已经存在当日文件");
            }
            return;
        }

        // 2.入库处理
        List<List<String>> csvRowList = CSVUtils.readCSVAdv(filePath+fileName, startrow, endrow, colnum);
        dealResult(transResVo2ResDo(csvRowList,date));


    }


    /**
     * 处理请求结果
     *
     * @param members 1.数据校验（暂无传输安全机制）
     *                2.数据处理
     *                3.数据入库 （支持数据快速入库）
     */
    public void dealResult(List<FBAInventoryInfoDO> members) {

        // 1.数据校验
        if (ObjectUtil.isEmpty(members)) {
            if (log.isInfoEnabled()){
                log.info("服务器无返回数据");
            }
//            throw new ServiceException(RespErrorEnum.SERVICE_NO_RESPOND.getSubStatusCode(), RespErrorEnum.SERVICE_NO_RESPOND.getSubStatusMsg());
            return ;
        }

        // 2.数据处理
        int result = 1;
        SqlSession batchSqlSession = null;
        try {
            batchSqlSession = this.sqlSessionTemplate
                    .getSqlSessionFactory()
                    .openSession(ExecutorType.BATCH, false);// 获取批量方式的sqlsession
            //通过新的session获取mapper
            FBAInventoryInfoDOMapper mapper = batchSqlSession.getMapper(FBAInventoryInfoDOMapper.class);

            int batchCount = 100;// 每批commit的个数
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
     * @param csvRowList
     * @return
     */
    public List<FBAInventoryInfoDO> transResVo2ResDo(List<List<String>> csvRowList,Date date) {
        List<FBAInventoryInfoDO> reulstList = new ArrayList<>();

        for(int index=0; index<csvRowList.size();++index) {
            List<String> resultData=csvRowList.get(index);
            log.info("resultData:"+resultData.toString());
            reulstList.add(FBAInventoryInfoDO.builder().merchantSku(resultData.get(0))
                    .fulfillmentNetworkSku(resultData.get(1))
                    .asin(resultData.get(2))
                    .title(resultData.get(3))
                    .condition(resultData.get(4))
                    .price(resultData.get(5))
                    .mfnListingExists(resultData.get(6))
                    .mfnFulfillableQty(resultData.get(7))
                    .afnListingExists(resultData.get(8))
                    .afnWarehouseQty(resultData.get(9))
                    .afnFulfillableQty(resultData.get(10))
                    .afnUnsellableQty(resultData.get(11))
                    .afnEncumberedQty(resultData.get(12))
                    .afnTotalQty(resultData.get(13))
                    .volume(resultData.get(14))
                    .afnInboundWorkingQty(resultData.get(15))
                    .afnInboundShippedQty(resultData.get(16))
                    .afnInboundReceivingQty(resultData.get(17))
                    .inserttime(date).build());
        }

        return reulstList;
    }

}
