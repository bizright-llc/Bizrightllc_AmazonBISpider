package com.spider.amazon.handler;


import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.common.exception.ServiceException;
import com.spider.amazon.cons.GetDataOfTaskByOffsetOperaTypeEnum;
import com.spider.amazon.cons.GetDataOfTaskByOffsetTransObject;
import com.spider.amazon.cons.RespErrorEnum;
import com.spider.amazon.cusinterface.SpiderGetDataType;
import com.spider.amazon.dto.BzyGetDataOfTaskByOffsetDTO;
import com.spider.amazon.handler.abs.AbstractGetDataByOffsetHandler;
import com.spider.amazon.mapper.reviewsInfoCopy1DOMapper;
import com.spider.amazon.model.reviewsInfoCopy1DO;
import com.spider.amazon.service.impl.BaZhuaYuServiceImpl;
import com.spider.amazon.utils.CSVUtils;
import com.spider.amazon.vo.BzyGetDataOfTaskByOffsetRepVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.assertj.core.util.DateUtil;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;


@Slf4j
@Component
@SpiderGetDataType(GetDataOfTaskByOffsetOperaTypeEnum.SKU_REVIEW_INFO_GET_DATA)
public class ReviewsInfoGetSpiderDataByOffsetHandler extends AbstractGetDataByOffsetHandler {


    @Autowired
    private BaZhuaYuServiceImpl baZhuaYuServiceImpl;

    @Autowired
    private SqlSessionTemplate sqlSessionTemplate;


    @Override
    public void getAllBzyDataByOffset(GetDataOfTaskByOffsetOperaTypeEnum operaTypeEnum,Object objDTO) {
        log.info("八爪鱼任务API通过偏移量获取所有数据");
        BzyGetDataOfTaskByOffsetDTO bzyGetDataOfTaskByOffsetDTO  = (BzyGetDataOfTaskByOffsetDTO) objDTO;

        log.info("获取所有数据");
        getAllPage(bzyGetDataOfTaskByOffsetDTO);
    }

    /**
     * 多页查询
     *
     * @param requestDTO
     */
    private void getAllPage(BzyGetDataOfTaskByOffsetDTO requestDTO) {
        int totpagenum = 0;
        /**
         * 循环获取逻辑
         * 偏移量逻辑：此接口根据数据起始偏移量（offset）和请求数据量获取任务数据，初始请求请将偏移量设置为0（offset = 0），
         * 数据量size ∈[1,1000]，每次通过此接口请求数据返回的偏移量（offset > 0）可以作为读取下一批数据的起始偏移量。
         * 例如某任务有1000条数据， 第一次调用offset = 0, size = 100，则将会返回任务的头100条数据，
         * 以及下一次的起始偏移量offset = x（x不一定等于100）。第二次调用时请求参数设置为offset = x，size = 100，
         * 则返回任务的第101-200条数据，以及下一次起始偏移量offset=x1，然后用x1作为下次的起始偏移量，以此类推
         */
        JSONObject resultData;
        do {
            BzyGetDataOfTaskByOffsetRepVO responseVO =  baZhuaYuServiceImpl.callGetDataOfTaskByOffset(requestDTO);
            if (isSuccess(responseVO.getError())) {
                resultData=responseVO.getData();
                if (ObjectUtil.isNotEmpty(resultData)) { // 存在数据列表
                    dealResult(transResVo2ResDo(resultData));
                } else {
                    break;
                }
            } else {
                break;
            }
        } while (hasDataFlgAndSetNextDTO(resultData,requestDTO));
    }

    /**
     * 判断请求结果是否成功
     * @param requestResult
     * @return
     */
    private boolean isSuccess(String requestResult){
        return StrUtil.equals(requestResult,"success")?true:false;
    }

    /**
     * 判断是否可以继续请求，并且设置下一个请求体
     * @param resultData
     * @return
     */
    private boolean hasDataFlgAndSetNextDTO(JSONObject resultData,BzyGetDataOfTaskByOffsetDTO requestDTO){
        if (ObjectUtil.isNotEmpty(resultData) && Long.valueOf(resultData.get("restTotal").toString())>0L ) {
            requestDTO.setOffset(ObjectUtil.isNotEmpty(resultData.get("offset"))?resultData.get("offset").toString():"");
            return ObjectUtil.isNotEmpty(requestDTO.getOffset())?true:false;
        } else {
            return false;
        }
    }


    /**
     * 处理请求结果
     *
     * @param members 1.数据校验（暂无传输安全机制）
     *                2.数据处理
     *                3.数据入库 （支持数据快速入库）
     */
    private void dealResult(List<reviewsInfoCopy1DO> members) {

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
            reviewsInfoCopy1DOMapper mapper = batchSqlSession.getMapper(reviewsInfoCopy1DOMapper.class);

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
     * @return
     */
    private List<reviewsInfoCopy1DO> transResVo2ResDo(JSONObject resultData) {
        List<reviewsInfoCopy1DO> reulstList = new ArrayList<>();
        // JSONObject转数组
        JSONArray dataListJsonArr ;
        if (isExistDataList(resultData)) {
            dataListJsonArr = JSONUtil.parseArray(resultData.get("dataList"),true);
        } else {
            return Collections.emptyList();
        }

        // 循环遍历JsonArr数组
        for (int arrIndex=0;arrIndex<dataListJsonArr.size();arrIndex++) {
            JSONObject jsonObject=dataListJsonArr.getJSONObject(arrIndex);
            reulstList.add(
                    reviewsInfoCopy1DO.builder()
                            .asin(ObjectUtil.isNotEmpty(jsonObject.get(GetDataOfTaskByOffsetTransObject.ASIN))? String.valueOf(jsonObject.get(GetDataOfTaskByOffsetTransObject.ASIN)) :"")
                            .commentnum(ObjectUtil.isNotEmpty(jsonObject.get(GetDataOfTaskByOffsetTransObject.COMMENT_NUM))? String.valueOf(jsonObject.get(GetDataOfTaskByOffsetTransObject.COMMENT_NUM)) :"")
                            .customername(ObjectUtil.isNotEmpty(jsonObject.get(GetDataOfTaskByOffsetTransObject.CUSTOMER_NAME))? String.valueOf(jsonObject.get(GetDataOfTaskByOffsetTransObject.CUSTOMER_NAME)) :"")
                            .customerurl(ObjectUtil.isNotEmpty(jsonObject.get(GetDataOfTaskByOffsetTransObject.CUSTOMER_URL))? String.valueOf(jsonObject.get(GetDataOfTaskByOffsetTransObject.CUSTOMER_URL)) :"")
                            .date(ObjectUtil.isNotEmpty(jsonObject.get(GetDataOfTaskByOffsetTransObject.DATE))? DateUtil.parse(jsonObject.get(GetDataOfTaskByOffsetTransObject.DATE).toString()) : null)
                            .helpefulnum(ObjectUtil.isNotEmpty(jsonObject.get(GetDataOfTaskByOffsetTransObject.HELPEFUL_NUM))? String.valueOf(jsonObject.get(GetDataOfTaskByOffsetTransObject.HELPEFUL_NUM)) :"")
                            .inserttime(ObjectUtil.isNotEmpty(jsonObject.get(GetDataOfTaskByOffsetTransObject.INSERT_TIME))? DateUtil.parse(jsonObject.get(GetDataOfTaskByOffsetTransObject.INSERT_TIME).toString())  :null)
                            .nothelpfulnum(ObjectUtil.isNotEmpty(jsonObject.get(GetDataOfTaskByOffsetTransObject.NOT_HELPFUL_NUM))? String.valueOf(jsonObject.get(GetDataOfTaskByOffsetTransObject.NOT_HELPFUL_NUM)) :"")
                            .property(ObjectUtil.isNotEmpty(jsonObject.get(GetDataOfTaskByOffsetTransObject.PROPERTY))? String.valueOf(jsonObject.get(GetDataOfTaskByOffsetTransObject.PROPERTY)) :"")
                            .reviewcontent(ObjectUtil.isNotEmpty(jsonObject.get(GetDataOfTaskByOffsetTransObject.REVIEW_CONTENT))? String.valueOf(jsonObject.get(GetDataOfTaskByOffsetTransObject.REVIEW_CONTENT)) :"")
                            .reviewimageurl(ObjectUtil.isNotEmpty(jsonObject.get(GetDataOfTaskByOffsetTransObject.REVIEW_IMAGE_URL))? String.valueOf(jsonObject.get(GetDataOfTaskByOffsetTransObject.REVIEW_IMAGE_URL)) :"")
                            .reviewsid(ObjectUtil.isNotEmpty(jsonObject.get(GetDataOfTaskByOffsetTransObject.REVIEWS_ID))? String.valueOf(jsonObject.get(GetDataOfTaskByOffsetTransObject.REVIEWS_ID)) :"")
                            .reviewtitle(ObjectUtil.isNotEmpty(jsonObject.get(GetDataOfTaskByOffsetTransObject.REVIEW_TITLE))? String.valueOf(jsonObject.get(GetDataOfTaskByOffsetTransObject.REVIEW_TITLE)) :"")
                            .star(ObjectUtil.isNotEmpty(jsonObject.get(GetDataOfTaskByOffsetTransObject.STAR))? String.valueOf(jsonObject.get(GetDataOfTaskByOffsetTransObject.STAR)) :"")
                            .verifiedpurchase(ObjectUtil.isNotEmpty(jsonObject.get(GetDataOfTaskByOffsetTransObject.VERIFIED_PURCHASE))? String.valueOf(jsonObject.get(GetDataOfTaskByOffsetTransObject.VERIFIED_PURCHASE)) :"")
                            .build()
            );
        }

        return reulstList;
    }

    private boolean isExistDataList(JSONObject resultData){
        if (ObjectUtil.isNotEmpty(resultData.get("dataList"))) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 预先读取文件处理
     *
     * @return
     */
    private Map<String, Object> stepForAmzDailySalesPrepare(String filePath) {
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
