package com.spider.amazon.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.common.exception.ServiceException;
import com.spider.amazon.cons.RespErrorEnum;
import com.spider.amazon.dto.GetPOHeaderDTO;
import com.spider.amazon.mapper.LaPoInfoDOMapper;
import com.spider.amazon.model.LaPoInfoDO;
import com.spider.amazon.service.IBopService;
import com.spider.amazon.service.IPoHeaderService;
import com.spider.amazon.vo.GetPOHeaderRepResultDataVO;
import com.spider.amazon.vo.GetPOHeaderRepVO;
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
public class PoHeaderServiceImpl implements IPoHeaderService {

    @Autowired
    private IBopService bopServiceImpl;

    @Autowired
    private SqlSessionTemplate sqlSessionTemplate;

    private final static int BATCHCOUNT=100;

    /**
     * 获取POHeader信息
     * @param requestDTO
     */
    @Override
    public void getPOHeader(GetPOHeaderDTO requestDTO) {
        if (ObjectUtil.isEmpty(requestDTO)) {
            requestDTO=GetPOHeaderDTO.builder()
                    .pageNo(1).pageSize(10000).asin("").poDate(DateUtil.formatDate(DateUtil.date())).build();
        }

        GetPOHeaderRepVO responseVO = bopServiceImpl.callBopGetPOHeader(requestDTO);
        dealResult(transResVo2ResDo(responseVO));
    }


    /**
     * 处理请求结果
     *
     * @param members 1.数据校验（暂无传输安全机制）
     *                2.数据处理
     *                3.数据入库 （支持数据快速入库）
     */
    public void dealResult(List<LaPoInfoDO> members) {

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
            LaPoInfoDOMapper mapper = batchSqlSession.getMapper(LaPoInfoDOMapper.class);

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
     * @param responseVO
     * @return
     */
    public List<LaPoInfoDO> transResVo2ResDo(GetPOHeaderRepVO responseVO) {
        List<LaPoInfoDO> reulstList = new ArrayList<>();

        for(GetPOHeaderRepResultDataVO resultDataVO :responseVO.getResult().getDataResult()) {
            reulstList.add(LaPoInfoDO.builder()
                    .asin(resultDataVO.getASIN())
                    .biPoRef(null)
                    .estimatereceivedate(DateUtil.parse(StrUtil.str(resultDataVO.getEstimateReceiveDate()).replace("T"," ")))
                    .insertTime(DateUtil.date())
                    .itemnum(resultDataVO.getItemNum())
                    .podate(DateUtil.parse(StrUtil.str(resultDataVO.getPODate()).replace("T"," ")))
                    .ponum(resultDataVO.getPONum())
                    .poqty(resultDataVO.getPOQty())
                    .porefnum(resultDataVO.getPORefNum())
                    .status(resultDataVO.getStatus())
                    .unitprice(resultDataVO.getUnitPrice())
                    .vendorcompany(resultDataVO.getVendorCompany())
                    .vendorname(resultDataVO.getVendorName())
                    .warehouse(resultDataVO.getWarehouse())
                    .build());
        }

        return reulstList;
    }


}
