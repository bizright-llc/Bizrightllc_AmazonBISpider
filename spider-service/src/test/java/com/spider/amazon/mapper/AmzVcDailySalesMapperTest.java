package com.spider.amazon.mapper;

import com.spider.SpiderServiceApplication;
import com.spider.amazon.entity.AmzVcDailySales;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes= SpiderServiceApplication.class)
class AmzVcDailySalesMapperTest {

    @Autowired
    private AmzVcDailySalesMapper amzVcDailySalesMapper;

    @Test
    public void testInsertData(){
        AmzVcDailySales newRecord = new AmzVcDailySales();

        newRecord.setAsin("test");
        newRecord.setSubcategorySalesRank("test");
        newRecord.setViewingDate("09/14/2020");

        amzVcDailySalesMapper.insert(newRecord);
    }

}