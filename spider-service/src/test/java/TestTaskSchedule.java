import com.spider.SpiderServiceApplication;
import com.spider.amazon.config.SpiderConfig;
import com.spider.amazon.service.impl.SpringBatchCallServiceImpl;
import com.spider.amazon.task.ScheduleTask;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.launch.support.SimpleJobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes=SpiderServiceApplication.class)
class TestTaskSchedule {


    @Autowired
    private SpiderConfig spiderConfig;

    @Autowired
    private SimpleJobLauncher csvJobLauncherForAmzDailySales;

    @Autowired
    private Job importJobForAmzDailySales;

    @Autowired
    private SpringBatchCallServiceImpl springBatchCallServiceImpl;

    @Test
    void testVcWeeklySales(){
        ScheduleTask scheduleTask =new ScheduleTask();
        scheduleTask.schedulerVcDailySales();
    }

    /**
     * 定时下载Amazon VC 每日销量报表
     */
//    @Test
//    void testVcWeeklySales2(){
//        System.out.println("0.step56=>开始执行［schedulerVcDailySales］");
//        Spider spider = Spider.create(new AmazonVcDailySales());
//        spider.addUrl(spiderConfig.getSpiderIndex());
//        spider.setExitWhenComplete(true);
//        spider.run();
//
//        System.out.println("1.step66=>调用 ［结果文件处理］");
//        runBatch(csvJobLauncherForAmzDailySales,importJobForAmzDailySales);
//    }
//
//
//    private void runBatch(SimpleJobLauncher simpleJobLauncher,Job job) {
//        System.out.println("0.step142=>调用 Batch处理");
//        JobParameters jobParameters = new JobParametersBuilder().addLong("time", System.currentTimeMillis())
//                .toJobParameters();
//        try {
//            simpleJobLauncher.run(job, jobParameters);
//        } catch (JobExecutionAlreadyRunningException e) {
//            throw new ServiceException(RespErrorEnum.SPIDER_EXEC.getSubStatusCode(),e.getMessage());
//        } catch (JobRestartException e) {
//            throw new ServiceException(RespErrorEnum.SPIDER_EXEC.getSubStatusCode(),e.getMessage());
//        } catch (JobInstanceAlreadyCompleteException e) {
//            throw new ServiceException(RespErrorEnum.SPIDER_EXEC.getSubStatusCode(),e.getMessage());
//        } catch (JobParametersInvalidException e) {
//            throw new ServiceException(RespErrorEnum.SPIDER_EXEC.getSubStatusCode(),e.getMessage());
//        }
//    }

    @Test
    void callSpringBatch(){
        springBatchCallServiceImpl.callVcSalesReportDataDeal();
    }


}