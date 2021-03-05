package com.spider.amazon.mapper;

import com.spider.SpiderServiceApplication;
import com.spider.amazon.cons.AmazonAdNodeType;
import com.spider.amazon.model.AmazonAdConsumeLogDO;
import com.spider.amazon.model.AmazonAdConsumeSettingDO;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes= SpiderServiceApplication.class)
class AmazonAdConsumeSettingDOMapperTest {

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    private AmazonAdConsumeSettingDOMapper amazonAdConsumeSettingDOMapper;

    @Autowired
    private AmazonAdConsumeLogDOMapper amazonAdConsumeLogDOMapper;

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

        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);

        transactionTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {

                List<AmazonAdConsumeLogDO> preLogs = amazonAdConsumeLogDOMapper.getAllBySettingId(1l);

                AmazonAdConsumeLogDO newLog = new AmazonAdConsumeLogDO();

                newLog.setAsin("Test");
                newLog.setBrand("Test");
                newLog.setSettingId("1");
                newLog.setType(AmazonAdNodeType.SEARCH_RESULT_AD);
                newLog.setCreatedAt(LocalDateTime.now().minusYears(1));
                newLog.setUpdatedAt(LocalDateTime.now().minusYears(1));
                newLog.setCreatedBy("test");
                newLog.setUpdatedBy("test");

                amazonAdConsumeLogDOMapper.insert(newLog);

                List<AmazonAdConsumeLogDO> afterLogs = amazonAdConsumeLogDOMapper.getAllBySettingId(1l);

                Assert.assertTrue(afterLogs.size() > preLogs.size());
            }
        });

    }
}