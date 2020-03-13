package com.spider.amazon.service.impl;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.common.exception.ServiceException;
import com.spider.amazon.cons.CalTimeTypeEnum;
import com.spider.amazon.cons.DateFormat;
import com.spider.amazon.cons.RespErrorEnum;
import com.spider.amazon.dto.VcPoInfoDTO;
import com.spider.amazon.mapper.VendorPODetailInfoDOMapper;
import com.spider.amazon.model.VendorPODetailInfoDO;
import com.spider.amazon.service.IBopService;
import com.spider.amazon.service.IVendorPoInfoService;
import com.spider.amazon.vo.VcPoInfoRepResultDataVO;
import com.spider.amazon.vo.VcPoInfoRepVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Service
@Slf4j
public class VendorPoInfoServiceImpl implements IVendorPoInfoService {

    @Autowired
    private IBopService bopServiceImpl;

    @Autowired
    private SqlSessionTemplate sqlSessionTemplate;

    private final static int BATCHCOUNT=10;

    /**
     * 获取VC PO 信息
     * @param vcPoInfoDTO
     */
    @Override
    public void getVcPoInfo( VcPoInfoDTO vcPoInfoDTO) {

        // 1.判断是否有请求实体
        if (ObjectUtil.isEmpty(vcPoInfoDTO)) {
            vcPoInfoDTO = VcPoInfoDTO.builder().pageNo(1)
                    .pageSize(10000)
                    .lastUpdate(getLastUpdate(CalTimeTypeEnum.NOW_LAST_DAY,""))
                    .poNum("")
                    .asin("")
                    .enterDate(null)
                    .vendor("")
                    .build();
        }

        // 2.暂定实际情况每天数据不超过1W条，超过1W条要做翻页请求处理
        VcPoInfoRepVO vcPoInfoRepVO = bopServiceImpl.callBopVcPoInfo(vcPoInfoDTO);

        // 3.返回数据信息处理
        dealResult(transPoVo2PoDo(vcPoInfoRepVO));

    }

    /**
     * 计算时间
     *
     * @param calTimeType
     * @param getDate
     * @return
     */
    public String getLastUpdate(CalTimeTypeEnum calTimeType, String getDate) {

        Date date = DateUtil.date(Calendar.getInstance());
        DateTime newDate = new DateTime();

        // 根据CalTimeTypeEnum判断计算方法
        if (calTimeType.equals(CalTimeTypeEnum.NOW_LAST_DAY)) {
            newDate = DateUtil.offsetDay(date, -1);
        } else if (calTimeType.equals(CalTimeTypeEnum.SPECIFIED_DAY)) {
            newDate = DateUtil.parseDate(getDate);
        } else {
            return null;
        }

        return DateUtil.format(newDate, DateFormat.YEAR_MONTH_DAY);

    }

    /**
     * 处理请求结果
     *
     * @param members 1.数据校验（暂无传输安全机制）
     *                2.数据处理
     *                3.数据入库 （支持数据快速入库）
     */
    public void dealResult(List<VendorPODetailInfoDO> members) {

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
            VendorPODetailInfoDOMapper mapper = batchSqlSession.getMapper(VendorPODetailInfoDOMapper.class);

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
     * @param vcPoInfoRepVO
     * @return
     */
    public List<VendorPODetailInfoDO> transPoVo2PoDo(VcPoInfoRepVO vcPoInfoRepVO) {
        List<VendorPODetailInfoDO> reulstList = new ArrayList<>();

        for(VcPoInfoRepResultDataVO vcPoInfoRepResultDataVO :vcPoInfoRepVO.getResult().getDataResult()) {
            reulstList.add(VendorPODetailInfoDO.builder().asin(vcPoInfoRepResultDataVO.getASIN())
                    .acceptedQuantity(vcPoInfoRepResultDataVO.getQtyAccepted())
                    .backorder(null)
                    .deliveryWindowEnd(null)
                    .deliveryWindowStart(null)
                    .expectedShipDate(StrUtil.sub(StrUtil.str(vcPoInfoRepResultDataVO.getExpectedShipDate()).replace("T"," "),0,10))
                    .modelNumber(vcPoInfoRepResultDataVO.getModelNumber())
                    .po(vcPoInfoRepResultDataVO.getPONum())
                    .quantityOutstanding(vcPoInfoRepResultDataVO.getQtyOutStanding())
                    .quantityReceived(vcPoInfoRepResultDataVO.getQtyReceived())
                    .quantitySubmitted(vcPoInfoRepResultDataVO.getQtySubmitted())
                    .shipToLocation(vcPoInfoRepResultDataVO.getShipToLocation())
                    .shipWindowEnd(StrUtil.sub(StrUtil.str(vcPoInfoRepResultDataVO.getShipWindowEnd()).replace("T"," "),0,10))
                    .shipWindowStart(StrUtil.sub(StrUtil.str(vcPoInfoRepResultDataVO.getShipWindowStart()).replace("T"," "),0,10))
                    .sku(vcPoInfoRepResultDataVO.getSKU())
                    .status(vcPoInfoRepResultDataVO.getStatus())
                    .title(vcPoInfoRepResultDataVO.getTitle())
                    .totalCost(vcPoInfoRepResultDataVO.getTotalCost())
                    .unitCost(vcPoInfoRepResultDataVO.getUnitCost())
                    .vendor(vcPoInfoRepResultDataVO.getVendor())
                    .inserttime(DateUtil.parse(StrUtil.sub(StrUtil.str(vcPoInfoRepResultDataVO.getLastUpdate()).replace("T"," "),0,10)))
                    .build());
        }

        return reulstList;
    }
}
