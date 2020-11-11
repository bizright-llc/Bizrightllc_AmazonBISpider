package com.spider.amazon.service;

import com.spider.SpiderServiceApplication;
import com.spider.amazon.cons.AmazonAdNodeType;
import com.spider.amazon.dto.AmazonAdDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes= SpiderServiceApplication.class)
class AmazonAdServiceTest {

    @Autowired
    private AmazonAdService amazonAdService;

    @Test
    void insertAdConsumeLog() throws InterruptedException {

        for (int i=0; i<101; i++){
            AmazonAdDTO amazonAd = AmazonAdDTO.builder()
                    .title(String.format("test title %s", i))
                    .type(AmazonAdNodeType.SEARCH_RESULT_AD)
                    .build();

            System.out.println(String.format("Insert log %s", i));

            amazonAdService.insertAdConsumeLog(amazonAd);

        }

        Thread.sleep(30000);

    }
}