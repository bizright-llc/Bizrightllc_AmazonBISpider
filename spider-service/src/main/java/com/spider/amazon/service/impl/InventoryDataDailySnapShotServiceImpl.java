package com.spider.amazon.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.common.exception.ServiceException;
import com.spider.amazon.cons.PageQryType;
import com.spider.amazon.cons.RespErrorEnum;
import com.spider.amazon.dto.GetInventoryDataDailySnapShotDTO;
import com.spider.amazon.mapper.LaInventoryInfoDOMapper;
import com.spider.amazon.model.LaInventoryInfoDO;
import com.spider.amazon.service.IBopService;
import com.spider.amazon.service.IInventoryDataDailySnapShotService;
import com.spider.amazon.utils.CSVUtils;
import com.spider.amazon.vo.GetInventoryDataDailySnapShotRepResultDataVO;
import com.spider.amazon.vo.GetInventoryDataDailySnapShotRepVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class InventoryDataDailySnapShotServiceImpl implements IInventoryDataDailySnapShotService {

    @Autowired
    private IBopService bopServiceImpl;

    @Autowired
    private SqlSessionTemplate sqlSessionTemplate;


    /**
     * 获取WInventoryDataDailySnapShot信息
     *
     * @param requestDTO
     */
    @Override
    public void getInventoryDataDailySnapShot(GetInventoryDataDailySnapShotDTO requestDTO) {
        if (ObjectUtil.isEmpty(requestDTO)) {
            requestDTO = GetInventoryDataDailySnapShotDTO.builder()
                    .pageNo(1).pageSize(1000).getType(PageQryType.QRY_SINGLE).build();
        }

        if (StrUtil.equals(requestDTO.getGetType(), PageQryType.QRY_ALL)) {
            getAllPage(requestDTO);
        } else {
            getSinglePage(requestDTO);
        }


    }

    /**
     * 单页查询
     *
     * @param requestDTO
     */
    public void getSinglePage(GetInventoryDataDailySnapShotDTO requestDTO) {
        GetInventoryDataDailySnapShotRepVO responseVO = bopServiceImpl.callBopGetInventoryDataDailySnapShot(requestDTO);
        dealResult(transResVo2ResDo(responseVO));
    }

    /**
     * 多页查询
     *
     * @param requestDTO
     */
    public void getAllPage(GetInventoryDataDailySnapShotDTO requestDTO) {
        int totpagenum = 0;
        boolean calflg = true;
        do {
            GetInventoryDataDailySnapShotRepVO responseVO = bopServiceImpl.callBopGetInventoryDataDailySnapShot(requestDTO);
            if (calflg == true) {
                calflg = false;
                long dataCount = ObjectUtil.isNotEmpty(responseVO) ? (ObjectUtil.isNotEmpty(responseVO.getResult()) ? (ObjectUtil.isNotEmpty(responseVO.getResult().getDataCount()) ? responseVO.getResult().getDataCount() : 0L) : 0L) : 0L;
                totpagenum= (dataCount<=requestDTO.getPageSize())?1: (int) ((dataCount % requestDTO.getPageSize() == 0) ? dataCount / requestDTO.getPageSize() : dataCount / requestDTO.getPageSize() + 1);
            }
            requestDTO.setPageNo(requestDTO.getPageNo()+1);
            dealResult(transResVo2ResDo(responseVO));
        } while (requestDTO.getPageNo()<=totpagenum);
    }


    /**
     * 处理请求结果
     *
     * @param members 1.数据校验（暂无传输安全机制）
     *                2.数据处理
     *                3.数据入库 （支持数据快速入库）
     */
    public void dealResult(List<LaInventoryInfoDO> members) {

        // 1.数据校验
        if (ObjectUtil.isEmpty(members)) {
            if (log.isInfoEnabled()) {
                log.info("服务器无返回数据");
            }
//            throw new ServiceException(RespErrorEnum.SERVICE_NO_RESPOND.getSubStatusCode(), RespErrorEnum.SERVICE_NO_RESPOND.getSubStatusMsg());
            return;
        }

        // 2.数据处理
        int result = 1;
        SqlSession batchSqlSession = null;
        try {
            batchSqlSession = this.sqlSessionTemplate
                    .getSqlSessionFactory()
                    .openSession(ExecutorType.BATCH, false);// 获取批量方式的sqlsession
            //通过新的session获取mapper
            LaInventoryInfoDOMapper mapper = batchSqlSession.getMapper(LaInventoryInfoDOMapper.class);

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
     *
     * @param responseVO
     * @return
     */
    public List<LaInventoryInfoDO> transResVo2ResDo(GetInventoryDataDailySnapShotRepVO responseVO) {
        List<LaInventoryInfoDO> reulstList = new ArrayList<>();

        for (GetInventoryDataDailySnapShotRepResultDataVO resultDataVO : responseVO.getResult().getDataResult()) {

            reulstList.add(LaInventoryInfoDO.builder()
                    .avcinopenorderqty(resultDataVO.getAVCInOpenOrderQty())
                    .enterdate(DateUtil.parse(StrUtil.str(resultDataVO.getEnterDate()).replace("T", " ")))
                    .inopenorderqty(resultDataVO.getInOpenOrderQty())
                    .insertTime(DateUtil.date())
                    .instockqty(resultDataVO.getInStockQty())
                    .instockqty(resultDataVO.getInStockQty())
                    .itemnum(resultDataVO.getItemNum())
                    .lastupdate(DateUtil.parse(StrUtil.str(resultDataVO.getLastUpdate()).replace("T", " ")))
                    .snapshotdate(DateUtil.parse(StrUtil.str(resultDataVO.getSnapShotDate()).replace("T", " ")))
                    .status(resultDataVO.getStatus())
                    .warehouse(resultDataVO.getWarehouse())
                    .wootinopenorderqty(resultDataVO.getWootInOpenOrderQty())
                    .build());
        }

        return reulstList;
    }

    /**
     * 预先读取文件处理
     *
     * @return
     */
    public Map<String, Object> stepForAmzDailySalesPrepare(String filePath) {
        Map<String, Object> resultMap = new HashMap<>();
        if (FileUtil.exist(filePath)) {
            // 读取文件第一行
            List<List<String>> csvRowList = CSVUtils.readCSVAdv(filePath, 0, 1, 11);
            // 获取报表维度及时间
            String reportingRange = csvRowList.get(0).get(7);
            String viewing = csvRowList.get(0).get(8);
            if (log.isInfoEnabled()) {
                log.info("reportingRange:" + reportingRange);
                log.info("viewing:" + viewing);
            }
            resultMap.put("reportingRange", reportingRange.substring(reportingRange.indexOf("[") + 1, reportingRange.indexOf("]")));
            resultMap.put("viewing", viewing.substring(viewing.indexOf("[") + 1, viewing.indexOf("]")));

            return resultMap;
        } else {
            return null;
        }
    }

}
