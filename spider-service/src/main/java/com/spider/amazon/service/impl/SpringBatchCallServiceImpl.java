package com.spider.amazon.service.impl;

import com.common.exception.ServiceException;
import com.spider.amazon.config.SpiderConfig;
import com.spider.amazon.cons.RespErrorEnum;
import com.spider.amazon.service.ISpringBatchCallService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.support.SimpleJobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class SpringBatchCallServiceImpl implements ISpringBatchCallService {

    @Autowired
    private SimpleJobLauncher csvJobLauncherForAmzDailySales;

    @Autowired
    private Job importJobForAmzDailySales;

    @Autowired
    private SimpleJobLauncher csvJobLauncherForAmzDailyInventory;

    @Autowired
    private Job importJobForAmzDailyInventory;

    @Autowired
    private SimpleJobLauncher csvJobLauncherForAmzScBuyBox;

    @Autowired
    private Job importJobForAmzScBuyBox;

    @Autowired
    private SimpleJobLauncher csvJobLauncherForAmzScFbaFee;

    @Autowired
    private Job importJobForAmzScFbaFee;

    @Override
    public void callVcSalesReportDataDeal() {
        log.info("1.ste39p=>调用 ［结果文件处理］");
        runBatch(csvJobLauncherForAmzDailySales, importJobForAmzDailySales);
    }

    @Override
    public void callVcInventoryReportDataDeal() {
        log.info("1.step45=>调用 ［结果文件处理］");
        runBatch(csvJobLauncherForAmzDailyInventory, importJobForAmzDailyInventory);
    }

    @Override
    public void callScBuyBoxReportDataDeal() {
        log.info("1.step52=>调用 ［结果文件处理］");
        runBatch(csvJobLauncherForAmzScBuyBox, importJobForAmzScBuyBox);
    }

    @Override
    public void callScFbaFeeReportDataDeal() {
        log.info("1.step52=>调用 ［结果文件处理］");
        runBatch(csvJobLauncherForAmzScFbaFee, importJobForAmzScFbaFee);
    }


    void runBatch(SimpleJobLauncher simpleJobLauncher, Job job) {
        log.info("0.step142=>调用 Batch处理");
        JobParameters jobParameters = new JobParametersBuilder()
                .addLong("time", System.currentTimeMillis())
                .toJobParameters();
        try {
            simpleJobLauncher.run(job, jobParameters);
        } catch (JobExecutionAlreadyRunningException e) {
            throw new ServiceException(RespErrorEnum.SPIDER_EXEC.getSubStatusCode(), e.getMessage());
        } catch (JobRestartException e) {
            throw new ServiceException(RespErrorEnum.SPIDER_EXEC.getSubStatusCode(), e.getMessage());
        } catch (JobInstanceAlreadyCompleteException e) {
            throw new ServiceException(RespErrorEnum.SPIDER_EXEC.getSubStatusCode(), e.getMessage());
        } catch (JobParametersInvalidException e) {
            throw new ServiceException(RespErrorEnum.SPIDER_EXEC.getSubStatusCode(), e.getMessage());
        }
    }

}
