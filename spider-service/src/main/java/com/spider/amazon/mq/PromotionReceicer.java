package com.spider.amazon.mq;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.rabbitmq.client.Channel;
import com.spider.amazon.config.SpiderConfig;
import com.spider.amazon.entity.PromotionList;
import com.spider.amazon.webmagic.amzvc.AmazonVcPromotionsPipeline;
import com.spider.amazon.webmagic.amzvc.AmazonVcPromotionsProcessor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Spider;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class PromotionReceicer{

    @Autowired
    private SpiderConfig spiderConfig;

    @RabbitListener(queues = "${promotion.queue.name}",containerFactory = "singleManuelListenerContainer")
    public void consumePromotionQueue(Message message,Channel channel,@Header(AmqpHeaders.DELIVERY_TAG) long tag) {

        log.info("TASK：[consumePromotionQueue] [consumePromotionQueue] message=>[{}]", StrUtil.str(message.getBody(),message.getMessageProperties().getContentEncoding()));
        Map<String,Object> params = JSONUtil.parseObj(StrUtil.str(message.getBody(),message.getMessageProperties().getContentEncoding()));
        // 1.从请求体中获取参数
        List<PromotionList> promotionList=JSONUtil.toList(JSONUtil.parseArray(params.get("promotionList")),PromotionList.class);
        String crawId= String.valueOf(params.get("crawId"));

        // 2.调用爬虫
        log.info("调用爬虫 promotionList=>[{}] crawId=>[{}]",promotionList,crawId);
        Spider spider= Spider.create(new AmazonVcPromotionsProcessor());
        spider.addPipeline(new AmazonVcPromotionsPipeline());
        spider.thread(2);
        for ( PromotionList singlePromotionList:promotionList) {
            log.info("singlePromotionList=>[{}]",singlePromotionList);
            Request request=new Request(singlePromotionList.getPromotionDetailUrl());
            request.putExtra("craw_id",crawId);
            spider.addRequest(request);
        }
        spider.run();

        // 3.手动确认消息
        try {
            log.info("手动确认信息 =>[{}] [{}]",tag,channel);
            channel.basicAck(tag,false);            // 确认消息
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
