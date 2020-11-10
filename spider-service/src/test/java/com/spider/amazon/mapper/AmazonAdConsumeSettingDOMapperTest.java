package com.spider.amazon.mapper;

import com.spider.SpiderServiceApplication;
import com.spider.amazon.cons.AmazonAdNodeType;
import com.spider.amazon.model.AmazonAdConsumeLogDO;
import com.spider.amazon.model.AmazonAdConsumeSettingDO;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes= SpiderServiceApplication.class)
class AmazonAdConsumeSettingDOMapperTest {

    @Autowired
    private AmazonAdConsumeSettingDOMapper amazonAdConsumeSettingDOMapper;

    @Test
    void getAllSetting() {
    }

    @Test
    void getAllActiveSetting() {

        List<AmazonAdConsumeSettingDO> settings = amazonAdConsumeSettingDOMapper.getAllActiveSetting();

        Assert.assertTrue(settings.size() > 0);

    }

    @Test
    void insertLog() {
        AmazonAdConsumeLogDO newLog = new AmazonAdConsumeLogDO();

        newLog.setAsin("Test");
        newLog.setBrand("Test");
        newLog.setSettingId("1");
        newLog.setType(AmazonAdNodeType.SEARCH_RESULT_AD);
        newLog.setCreatedAt(LocalDateTime.now().minusYears(1));
        newLog.setUpdatedAt(LocalDateTime.now().minusYears(1));

        amazonAdConsumeSettingDOMapper.insertLog(newLog);
    }
}