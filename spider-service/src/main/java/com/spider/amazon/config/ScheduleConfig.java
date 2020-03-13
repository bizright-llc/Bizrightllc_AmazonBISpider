package com.spider.amazon.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * 多定时任务并行调度
 */
@Configuration
@EnableScheduling
public class ScheduleConfig implements SchedulingConfigurer {

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        taskRegistrar.setTaskScheduler(taskScheduler());
    }

    @Bean
    public TaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
        taskScheduler.setPoolSize(10);// 配置线程池大小，根据任务数量定制
        taskScheduler.setThreadNamePrefix("spring-task-scheduler-thread-");// 线程名称前缀
        taskScheduler.setAwaitTerminationSeconds(3600);// 线程池关闭前最大等待时间，确保最后一定关闭
        taskScheduler.setWaitForTasksToCompleteOnShutdown(true);// 线程池关闭时等待所有任务完成
        taskScheduler.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());// 任务丢弃策略
        return taskScheduler;
    }
}

