package com.spider.amazon.mapper;

import com.spider.SpiderServiceApplication;
import com.spider.amazon.model.VcPromotionInfoDO;
import com.spider.amazon.utils.SpringContextUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes= SpiderServiceApplication.class)
class VcPromotionInfoDOMapperTest {

    @Autowired
    private VcPromotionInfoDOMapper vcPromotionInfoDOMapper;

    @Test
    public void testInsert(){

        VcPromotionInfoDO newRecord = new VcPromotionInfoDO();
        newRecord.setCrawId("test");
        newRecord.setCreatedOn(LocalDateTime.now());

        vcPromotionInfoDOMapper.insert(newRecord);
    }

}