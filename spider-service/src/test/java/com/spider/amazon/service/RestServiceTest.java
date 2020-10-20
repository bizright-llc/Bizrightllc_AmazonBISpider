package com.spider.amazon.service;

import com.spider.SpiderServiceApplication;
import com.spider.amazon.dto.ProxyDTO;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = SpiderServiceApplication.class)
class RestServiceTest {

    @Autowired
    private RestService restService;

    @Test
    void testValidProxy() {

        ProxyDTO proxyDto = ProxyDTO
                .builder()
                .ip("zproxy.lum-superproxy.io")
                .port("22225")
                .username("lum-customer-ipower-zone-static-country-us")
                .password("38rnyeoymh2g")
                .build();

        try{
            boolean result = restService.testProxy(proxyDto);

            Assert.assertTrue(result);
        }catch (Exception ex){
            fail();
        }

    }

    @Test
    void testUnValidProxy() {

        ProxyDTO proxyDto = ProxyDTO
                .builder()
                .ip("zproxy.lum-superproxy.io")
                .port("22225")
                .username("lum-customer-ipower-zone-static-country-us")
                .password("38rnyeoymh2")
                .build();

        try{
            boolean result = restService.testProxy(proxyDto);

            Assert.assertFalse(result);
        }catch (Exception ex){
            fail();
        }

    }

    @Test
    void getPlainJSON() throws Exception {

        String response = restService.getPlainJSON("http://lumtest.com/myip.json");

        System.out.println(response);

    }

    @Test
    void testGetPlainJSONWithProxy() throws Exception {

        ProxyDTO proxyDto = ProxyDTO
                .builder()
                .ip("zproxy.lum-superproxy.io")
                .port("22225")
                .username("lum-customer-ipower-zone-static-country-us")
                .password("38rnyeoymh2g")
                .build();

        String response = restService.getPlainJSON("http://lumtest.com/myip.json", proxyDto);

        System.out.println(response);

    }
}