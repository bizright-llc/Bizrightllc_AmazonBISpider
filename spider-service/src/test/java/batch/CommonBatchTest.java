package batch;

import com.spider.SpiderServiceApplication;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.support.SimpleJobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes=SpiderServiceApplication.class)
public class CommonBatchTest {

    @Autowired
    private SimpleJobLauncher csvJobLauncherForAmzDailySales;

    @Autowired
    private Job importJobForAmzDailySales;

    @Test
    public void test() throws Exception {
        // 后置参数：使用JobParameters中绑定参数
        JobParameters jobParameters = new JobParametersBuilder().addLong("time", System.currentTimeMillis())
                .toJobParameters();
        csvJobLauncherForAmzDailySales.run(importJobForAmzDailySales, jobParameters);
    }
}