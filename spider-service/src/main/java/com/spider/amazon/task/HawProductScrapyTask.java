package com.spider.amazon.task;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.common.exception.ServiceException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.spider.amazon.config.SpiderConfig;
import com.spider.amazon.cons.RespErrorEnum;
import com.spider.amazon.cons.TaskSts;
import com.spider.amazon.mapper.SkuScrapyTaskDOMapper;
import com.spider.amazon.model.SkuScrapyTaskDO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.AbstractJavaTypeMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Haw商品信息抓取任务
 */
@Component
@Slf4j
public class HawProductScrapyTask {

    @Autowired
    private SpiderConfig spiderConfig;

    @Autowired
    private SkuScrapyTaskDOMapper skuScrapyTaskDOMapper;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private Environment env;

    @Autowired
    private ObjectMapper objectMapper;

    private final static int UPD_MAX_COUNT=2;

    /**
     * 定时调用Haw抓取任务
     */
    @Scheduled(cron =  "0 0/30 * * * ? ")
    public void scheduleHawProductSpider() {
        log.info("[执行Haw抓取任务]");

        // 1.查询待更新任务状态任务（控制每次抓取执行爬虫数）
        Map<String,Object> params = new HashMap<>();
        params.put("taskSts",TaskSts.TASK_WAITING);
        log.debug("查询待筛选更新任务状态的任务列表  params=>[{}]", params.toString());
        List<SkuScrapyTaskDO> resultList = skuScrapyTaskDOMapper.selectTaskListByTaskSts(params);
        if (CollUtil.isEmpty(resultList)) {
            log.debug("无待抓取数据任务");
            return;
        }
        log.info("返回结果 resutlList [{}]",resultList);

        // 2.异步调用抓取爬虫，通过taskId列表更新任务状态
        for (int resultindex=0;resultindex<resultList.size();resultindex++) {
            SkuScrapyTaskDO skuScrapyTaskDO=resultList.get(resultindex);
            if (resultindex>=UPD_MAX_COUNT) {
                break;
            }
            params.clear();
            params.put("taskId", StrUtil.toString(skuScrapyTaskDO.getTaskId()));
            params.put("oldTaskSts", TaskSts.TASK_WAITING);
            params.put("taskSts", TaskSts.TASK_SCRAPY);
            log.debug(" =>[{}]",params);
            try {
                // 异步队列
                rabbitTemplate.setMessageConverter(new Jackson2JsonMessageConverter());
                rabbitTemplate.setExchange(env.getProperty("haw.exchange.name"));
                rabbitTemplate.setRoutingKey(env.getProperty("haw.routing.key.name"));
                Message message= MessageBuilder.withBody(objectMapper.writeValueAsBytes(params)).setDeliveryMode(MessageDeliveryMode.PERSISTENT).build();
                message.getMessageProperties().setHeader(AbstractJavaTypeMapper.DEFAULT_CONTENT_CLASSID_FIELD_NAME, MessageProperties.CONTENT_TYPE_JSON);
                rabbitTemplate.convertAndSend(message);
            } catch (Exception e) {
                throw new ServiceException(RespErrorEnum.TASK_DEAL_ERROR.getSubStatusCode(),RespErrorEnum.TASK_DEAL_ERROR.getSubStatusMsg());
            }
        }
    }

    /**
     * Haw抓取数据处理
     */
    @Scheduled(cron = "0 5/30 * * * ? ")
    public void scheduleHawDataDeal() {
        log.info("[执行Haw数据处理任务]=>[scheduleHawDataDeal]");

        // 1.获取数据抓取成功状态的任务
        Map<String,Object> params = new HashMap<>();
        params.put("taskSts",TaskSts.TASK_SCRAPYSUCC);
        log.debug("查询数据抓取成功任务状态的任务列表  params=>[{}]", params.toString());
        List<SkuScrapyTaskDO> resultList = skuScrapyTaskDOMapper.selectTaskListByTaskSts(params);
        if (CollUtil.isEmpty(resultList)) {
            log.debug("无数据抓取成功状态任务");
            return;
        }
        log.info("返回结果 resutlList [{}]",resultList);

        // 2.开启异步队列处理
        for (int resultindex=0;resultindex<resultList.size();resultindex++) {
            SkuScrapyTaskDO skuScrapyTaskDO=resultList.get(resultindex);
            if (resultindex>=UPD_MAX_COUNT) {
                break;
            }
            params.clear();
            params.put("taskId", StrUtil.toString(skuScrapyTaskDO.getTaskId()));
            log.debug("params=>[{}]",params);
            try {
                // 异步队列
                rabbitTemplate.setMessageConverter(new Jackson2JsonMessageConverter());
                rabbitTemplate.setExchange(env.getProperty("haw.dealdata.exchange.name"));
                rabbitTemplate.setRoutingKey(env.getProperty("haw.dealdata.routing.key.name"));
                Message message= MessageBuilder.withBody(objectMapper.writeValueAsBytes(params)).setDeliveryMode(MessageDeliveryMode.PERSISTENT).build();
                message.getMessageProperties().setHeader(AbstractJavaTypeMapper.DEFAULT_CONTENT_CLASSID_FIELD_NAME, MessageProperties.CONTENT_TYPE_JSON);
                rabbitTemplate.convertAndSend(message);
            } catch (Exception e) {
                throw new ServiceException(RespErrorEnum.TASK_DEAL_ERROR.getSubStatusCode(),RespErrorEnum.TASK_DEAL_ERROR.getSubStatusMsg());
            }
        }

    }
}
