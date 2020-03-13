package com.spider.amazon.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.common.exception.ServiceException;
import com.spider.amazon.cons.RespErrorEnum;
import com.spider.amazon.dto.GetInvoiceDTO;
import com.spider.amazon.mapper.InvoiceInfoInfoDOMapper;
import com.spider.amazon.model.InvoiceInfoInfoDO;
import com.spider.amazon.service.IBopService;
import com.spider.amazon.service.IInvoiceService;
import com.spider.amazon.vo.GetInvoiceRepResultDataVO;
import com.spider.amazon.vo.GetInvoiceRepVO;
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
public class InvoiceServiceImpl implements IInvoiceService {

    @Autowired
    private IBopService bopServiceImpl;

    @Autowired
    private SqlSessionTemplate sqlSessionTemplate;

    /**
     * 获取Invoice信息
     * @param requestDTO
     */
    @Override
    public void getInvoice(GetInvoiceDTO requestDTO) {
        if (ObjectUtil.isEmpty(requestDTO)) {
            requestDTO=GetInvoiceDTO.builder()
                    .pageNo(1).pageSize(10000).asin("").channel("").invoiceDate(DateUtil.format(DateUtil.date(),"yyyyMMdd")).build();
        }

        GetInvoiceRepVO responseVO = bopServiceImpl.callBopGetInvoice(requestDTO);
        dealResult(transResVo2ResDo(responseVO));
    }


    /**
     * 处理请求结果
     *
     * @param members 1.数据校验（暂无传输安全机制）
     *                2.数据处理
     *                3.数据入库 （支持数据快速入库）
     */
    public void dealResult(List<InvoiceInfoInfoDO> members) {

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
            InvoiceInfoInfoDOMapper mapper = batchSqlSession.getMapper(InvoiceInfoInfoDOMapper.class);

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
    public List<InvoiceInfoInfoDO> transResVo2ResDo(GetInvoiceRepVO responseVO) {
        List<InvoiceInfoInfoDO> reulstList = new ArrayList<>();

        for(GetInvoiceRepResultDataVO resultDataVO :responseVO.getResult().getDataResult()) {
            reulstList.add(InvoiceInfoInfoDO.builder()
                    .asin(resultDataVO.getAsin())
                    .buyeruserid(resultDataVO.getBuyerUserID())
                    .channel(resultDataVO.getChannel())
                    .comboitemnum(resultDataVO.getComboItemNum())
                    .invoicedate(DateUtil.parse(StrUtil.str(resultDataVO.getInvoiceDate()).replace("T"," ")))
                    .invoicenum(resultDataVO.getInvoiceNum())
                    .isfromcombo(Integer.valueOf(resultDataVO.getIsFromCombo()))
                    .itemnum(resultDataVO.getItemNum())
                    .lineamt(resultDataVO.getLineAmt())
                    .linenum(Integer.valueOf(resultDataVO.getLineNum()))
                    .orderqty(resultDataVO.getOrderQty())
                    .paymentdate(DateUtil.parse(StrUtil.str(resultDataVO.getPaymentDate()).replace("T"," ")))
                    .shipaddr1(resultDataVO.getShipAddr1())
                    .shipcity(resultDataVO.getShipCity())
                    .shipcountry(resultDataVO.getShipCountry())
                    .shipstate(resultDataVO.getShipState())
                    .status(resultDataVO.getStatus())
                    .unitprice(resultDataVO.getUnitPrice())
                    .build());
        }

        return reulstList;
    }

}
