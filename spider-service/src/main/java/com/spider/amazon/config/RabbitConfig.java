package com.spider.amazon.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.amqp.SimpleRabbitListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

/**
 Broker:它提供一种传输服务,它的角色就是维护一条从生产者到消费者的路线，保证数据能按照指定的方式进行传输,
 Exchange：消息交换机,它指定消息按什么规则,路由到哪个队列。
 Queue:消息的载体,每个消息都会被投到一个或多个队列。
 Binding:绑定，它的作用就是把exchange和queue按照路由规则绑定起来.
 Routing Key:路由关键字,exchange根据这个关键字进行消息投递。
 vhost:虚拟主机,一个broker里可以有多个vhost，用作不同用户的权限分离。
 Producer:消息生产者,就是投递消息的程序.
 Consumer:消息消费者,就是接受消息的程序.
 Channel:消息通道,在客户端的每个连接里,可建立多个channel.
 */
@Configuration
@Slf4j
public class RabbitConfig {
    @Autowired
    private Environment env;

    @Autowired
    private CachingConnectionFactory connectionFactory;

    @Autowired
    private SimpleRabbitListenerContainerFactoryConfigurer factoryConfigurer;

    /**
     * 队列参数
     */
    /**
     * Haw信息抓取队列
     */
    @Value("${haw.queue.name}")
    private String HAW_QUEUE_NAME;
    @Value("${haw.exchange.name}")
    private String HAW_EXCHANGE_NAME;
    @Value("${haw.routing.key.name}")
    private String HAW_ROUTINGKEY_NAME;

    /**
     * Haw数据处理队列
     */
    @Value("${haw.dealdata.queue.name}")
    private String HAW_DATADEAL_QUEUE_NAME;
    @Value("${haw.dealdata.exchange.name}")
    private String HAW_DATADEAL_EXCHANGE_NAME;
    @Value("${haw.dealdata.routing.key.name}")
    private String HAW_DATADEAL_ROUTINGKEY_NAME;

    /**
     * Promotion爬取队列
     */
    @Value("${promotion.queue.name}")
    private String PROMOTION_QUEUE_NAME;
    @Value("${promotion.exchange.name}")
    private String PROMOTION_EXCHANGE_NAME;
    @Value("${promotion.routing.key.name}")
    private String PROMOTION_ROUTINGKEY_NAME;


    /**
     * 单一消费者
     * @return
     */
    @Bean(name = "singleListenerContainer")
    public SimpleRabbitListenerContainerFactory listenerContainer(){
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(new Jackson2JsonMessageConverter());
        factory.setConcurrentConsumers(1);
        factory.setMaxConcurrentConsumers(1);
        factory.setPrefetchCount(1);
        factory.setTxSize(1);
        factory.setAcknowledgeMode(AcknowledgeMode.AUTO);
        return factory;
    }

    /**
     * 单一消费者(手动确认)
     * @return
     */
    @Bean(name = "singleManuelListenerContainer")
    public SimpleRabbitListenerContainerFactory listenerManuelContainer(){
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(new Jackson2JsonMessageConverter());
        factory.setConcurrentConsumers(1);
        factory.setMaxConcurrentConsumers(1);
        factory.setPrefetchCount(1);
        factory.setTxSize(1);
        factory.setAcknowledgeMode(AcknowledgeMode.MANUAL);
        return factory;
    }

    /**
     * 多个消费者
     * @return
     */
    @Bean(name = "multiListenerContainer")
    public SimpleRabbitListenerContainerFactory multiListenerContainer(){
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factoryConfigurer.configure(factory,connectionFactory);
        factory.setMessageConverter(new Jackson2JsonMessageConverter());
        factory.setAcknowledgeMode(AcknowledgeMode.NONE);
        factory.setConcurrentConsumers(env.getProperty("spring.rabbitmq.listener.concurrency",int.class));
        factory.setMaxConcurrentConsumers(env.getProperty("spring.rabbitmq.listener.max-concurrency",int.class));
        factory.setPrefetchCount(env.getProperty("spring.rabbitmq.listener.prefetch",int.class));
        return factory;
    }

    @Bean
    public RabbitTemplate rabbitTemplate(){
        connectionFactory.setPublisherConfirms(true);
        connectionFactory.setPublisherReturns(true);
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMandatory(true);
        rabbitTemplate.setConfirmCallback(new RabbitTemplate.ConfirmCallback() {
            @Override
            public void confirm(CorrelationData correlationData, boolean ack, String cause) {
                log.info("消息发送成功:correlationData({}),ack({}),cause({})",correlationData,ack,cause);
            }
        });
        rabbitTemplate.setReturnCallback(new RabbitTemplate.ReturnCallback() {
            @Override
            public void returnedMessage(Message message, int replyCode, String replyText, String exchange, String routingKey) {
                log.info("消息丢失:exchange({}),route({}),replyCode({}),replyText({}),message:{}",exchange,routingKey,replyCode,replyText,message);
            }
        });
        return rabbitTemplate;
    }


    /**
     * Haw异步调用queue
     */
    @Bean
    public Queue hawProductInfoQueue() {
        return new Queue(HAW_QUEUE_NAME,true);
    }

    /**
     * Haw异步调用Exchange
     */
    @Bean
    public DirectExchange hawProductInfoExchange() {
        return new DirectExchange(HAW_EXCHANGE_NAME,true,false);
    }

    /**
     * Haw异步调用Exchange
     */
    @Bean
    public Binding hawProductInfoBingding() {
        return BindingBuilder.bind(hawProductInfoQueue()).to(hawProductInfoExchange()).with(HAW_ROUTINGKEY_NAME);
    }

    /**
     * Haw数据处理异步调用queue
     */
    @Bean
    public Queue hawDataDealQueue() {
        return new Queue(HAW_DATADEAL_QUEUE_NAME,true);
    }

    /**
     * Haw数据处理异步调用Exchange
     */
    @Bean
    public DirectExchange hawDataDealExchange() {
        return new DirectExchange(HAW_DATADEAL_EXCHANGE_NAME,true,false);
    }

    /**
     * Haw数据处理异步调用Exchange
     */
    @Bean
    public Binding hawDataDealBingding() {
        return BindingBuilder.bind(hawDataDealQueue()).to(hawDataDealExchange()).with(HAW_DATADEAL_ROUTINGKEY_NAME);
    }

    /**
     * promotion异步调用queue
     */
    @Bean
    public Queue promotionQueue() {
        return new Queue(PROMOTION_QUEUE_NAME,true);
    }

    /**
     * promotion异步调用Exchange
     */
    @Bean
    public DirectExchange promotionExchange() {
        return new DirectExchange(PROMOTION_EXCHANGE_NAME,true,false);
    }

    /**
     * promotion异步调用Exchange
     */
    @Bean
    public Binding promotionBingding() {
        return BindingBuilder.bind(promotionQueue()).to(promotionExchange()).with(PROMOTION_ROUTINGKEY_NAME);
    }

}

