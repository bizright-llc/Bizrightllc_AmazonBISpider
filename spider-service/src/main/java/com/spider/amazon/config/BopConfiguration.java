package com.spider.amazon.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Data
public class BopConfiguration {

    @Value("${bop.server}")
    private String server;

    @Value("${bop.timeout}")
    private Integer timeout;

}
