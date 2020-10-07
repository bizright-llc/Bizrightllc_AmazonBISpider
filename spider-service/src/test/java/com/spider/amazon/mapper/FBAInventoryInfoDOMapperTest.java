package com.spider.amazon.mapper;

import com.spider.amazon.model.FBAInventoryInfoDO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class FBAInventoryInfoDOMapperTest {

    @Autowired
    private FBAInventoryInfoDOMapper mapper;

    @Test
    void insert() {
        FBAInventoryInfoDO record = FBAInventoryInfoDO.builder().asin("Test").price("1,123.12").priceNum(new BigDecimal(1123.12)).build();

        try{
            mapper.insert(record);
        }catch (Exception e){
            e.printStackTrace();
            fail();
        }

    }
}