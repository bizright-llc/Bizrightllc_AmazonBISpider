package com.spider.amazon.service.impl;

import com.spider.SpiderServiceApplication;
import com.spider.amazon.dto.ProxyDTO;
import com.spider.amazon.service.ProxyService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.ArrayList;
import java.util.List;


import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes= SpiderServiceApplication.class)
class ProxyServiceImplTest {

    @Autowired
    private ProxyService proxyService;

    @Test
    void testAddProxies() {

        List<ProxyDTO> proxies = new ArrayList<>();
        for (int i=0; i< 1000; i++){
            ProxyDTO proxyDTO = new ProxyDTO();
            proxyDTO.setIp(String.format("0.0.%s.%s", i/255, i%255));
            proxyDTO.setPort(String.format("1111"));
            proxyDTO.setProvider("Test");

            proxies.add(proxyDTO);
        }

        try{
            long startTime = System.currentTimeMillis();

            proxyService.addProxies(proxies);

            long endTime = System.currentTimeMillis();

            // 6958 ms
            System.out.println(String.format("Insert 1000 proxy time: %s", endTime - startTime));

        }catch (Exception ex){
            fail();
        }
    }

    @Test
    void testUpdateProxies() {

        List<ProxyDTO> proxies = new ArrayList<>();
        for (int i=0; i< 1000; i++){
            ProxyDTO proxyDTO = new ProxyDTO();
            proxyDTO.setIp(String.format("0.0.%s.%s", i/255, i%255));
            proxyDTO.setPort(String.format("1111"));
            proxyDTO.setProvider("Test");

            proxies.add(proxyDTO);
        }

        try{
            long startTime = System.currentTimeMillis();

            proxyService.addProxies(proxies);

            long endTime = System.currentTimeMillis();

            // 10643 ms
            System.out.println(String.format("Insert 1000 proxy time: %s", endTime - startTime));

        }catch (Exception ex){
            fail();
        }
    }

    @Test
    void testGetRandomProxy(){

        List<ProxyDTO> proxies = proxyService.getRandomActiveProxy(200);

        if(proxies.size() != 200){
            fail();
        }

        for (ProxyDTO proxyDTO: proxies){
            if (!proxyDTO.getActive()){
                fail();
            }
        }

        System.exit(2);
    }
}