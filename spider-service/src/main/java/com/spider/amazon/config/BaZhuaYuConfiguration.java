package com.spider.amazon.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * @ClassName BaZhuaYuConfiguration
 * @Desc 八爪鱼API服务参数
 */
@Configuration
@Data
public class BaZhuaYuConfiguration {

    @Value("${bazhuayu.server}")
    private String server;

    @Value("${bazhuayu.timeout}")
    private Integer timeout;

    @Value("${bazhuayu.username}")
    private String username;

    @Value("${bazhuayu.password}")
    private String password;

    @Value("${bazhuayu.addr.skudetailaddr}")
    private String skuDetailAddr;


}
