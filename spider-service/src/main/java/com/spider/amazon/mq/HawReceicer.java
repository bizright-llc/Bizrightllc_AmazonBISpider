package com.spider.amazon.mq;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.spider.amazon.config.SpiderConfig;
import com.spider.amazon.cons.SqlResult;
import com.spider.amazon.mapper.SkuScrapyTaskDOMapper;
import com.spider.amazon.service.HawProductService;
import com.spider.amazon.webmagic.haw.HawProductInfoPipeline;
import com.spider.amazon.webmagic.haw.HawProductInfoProcessor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Spider;

import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class HawReceicer {


    @Autowired
    private SkuScrapyTaskDOMapper skuScrapyTaskDOMapper;

    @Autowired
    private SpiderConfig spiderConfig;

    @Autowired
    private HawProductService hawProductServiceImpl;


    @RabbitListener(queues = "${haw.queue.name}",containerFactory = "singleListenerContainer")
    public void consumeHawQueue(Message message) {
        log.info("TASK：[更新任务状态] [调用爬虫] message=>[{}]", StrUtil.str(message.getBody(),message.getMessageProperties().getContentEncoding()));
        Map<String,Object> params = JSONUtil.parseObj(StrUtil.str(message.getBody(),message.getMessageProperties().getContentEncoding()));

//    @RabbitListener(queues = "${haw.queue.name}",containerFactory = "singleListenerContainer")
//    public void consumeHawQueue(Message message) {
//
//    }
//
//    public void consumeHawQueue1() {
//        Map<String,Object> params=new HashMap<>();
//        params.put("taskId","03ee6a7ddb7c4aebbceab020d9323f91");
//        params.put("taskSts","SC");
//        params.put("oldTaskSts","W");

        // 0.更新任务状态
        log.debug("选中任务更新任务状态  params=>[{}]", params);
        int result = skuScrapyTaskDOMapper.updateByTaskStsAndTaskId(params);
        if (result != SqlResult.SUCC_OOM) {
            return;
//            throw new ServiceException(RespErrorEnum.TASK_DEAL_ERROR.getSubStatusCode(),RespErrorEnum.TASK_DEAL_ERROR.getSubStatusMsg());
        }
        String taskId= params.get("taskId").toString();

        // 1.数据预处理
//        hawProductServiceImpl.taskFileVendorSkuIntoDB(params);

        // 2.查询任务要抓取id信息
        log.debug("查询任务要抓取id信息  params=>[{}]", params.toString());
        List<Map<String, Object>> resultMapList = skuScrapyTaskDOMapper.selectByTaskId(params);
        log.info(" resultMapList=>[{}]" ,resultMapList);
        StringBuffer proIdListBuf=new StringBuffer();
        StringBuffer vSkuListBuf=new StringBuffer();
        StringBuffer mAsinListBuf=new StringBuffer();


        for (Map<String,Object> entryMap:resultMapList) {
            proIdListBuf.append(entryMap.get("product_id")).append("|");
            vSkuListBuf.append(entryMap.get("vendor_sku")).append("|");
            mAsinListBuf.append(entryMap.get("merchant_suggested_asin")).append("|");
        }
        String proIdListStr= ObjectUtils.isNotEmpty(proIdListBuf.toString())?StrUtil.removeSuffix(proIdListBuf.toString(),"\\|"):"";
        String vSkuListStr= ObjectUtils.isNotEmpty(vSkuListBuf.toString())?StrUtil.removeSuffix(vSkuListBuf.toString(),"\\|"):"";
        String mAsinListStr= ObjectUtils.isNotEmpty(mAsinListBuf.toString())?StrUtil.removeSuffix(mAsinListBuf.toString(),"\\|"):"";


        // 3.调用爬虫
        Spider spider= Spider.create(new HawProductInfoProcessor());
        spider.addPipeline(new HawProductInfoPipeline());
        Request request = new Request(spiderConfig.getSpiderHawIndex());
        request.putExtra(HawProductInfoProcessor.PRODUCT_ID_LIST,proIdListStr);
        request.putExtra(HawProductInfoProcessor.TASK_ID,taskId);
        request.putExtra(HawProductInfoProcessor.VENDOR_SKU_LIST,vSkuListStr);
        request.putExtra(HawProductInfoProcessor.M_ASIN_LIST,mAsinListStr);
        spider.addRequest(request);
        log.info("调用Haw信息抓取爬虫 PRODUCT_ID_LIST=>[{}] TASK_ID=>[{}] VENDOR_SKU_LIST=>[{}] M_ASIN_LIST=>[{}]",proIdListStr,taskId,vSkuListStr,mAsinListStr);
        spider.start();

    }


}
