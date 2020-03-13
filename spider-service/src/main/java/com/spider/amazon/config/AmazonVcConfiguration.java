package com.spider.amazon.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * @ClassName AmazonVcConfiguration
 * @Desc Amazon VC后台请求配置
 */
@Configuration
@Data
public class AmazonVcConfiguration {

    @Value("${amzvc.server}")
    private String server;

}
