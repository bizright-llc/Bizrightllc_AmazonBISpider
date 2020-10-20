package com.spider.amazon.service;

import com.spider.SpiderServiceApplication;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes= SpiderServiceApplication.class)
class ProxyServiceTest {

    @Autowired
    private ProxyService proxyService;

    @Test
    void testRefreshProxyPool() {

        proxyService.refreshIpPool();

    }
}