package com.spider.amazon.batch.vcdailyinventory;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.spider.amazon.config.DruidConfiguration;
import com.spider.amazon.cons.DateFormat;
import com.spider.amazon.entity.AmzVcDailyInventory;
import com.spider.amazon.utils.CSVUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.launch.support.SimpleJobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.support.JobRepositoryFactoryBean;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.FileSystemResource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.interceptor.DefaultTransactionAttribute;

import javax.sql.DataSource;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * @description spring batch cvs文件批处理配置需要注入Spring Batch以下组成部分
 * spring batch组成：
 * 1）JobRepository 注册job的容器
 * 2）JonLauncher 用来启动job的接口
 * 3）Job 实际执行的任务，包含一个或多个Step
 * 4）Step Step步骤包括ItemReader、ItemProcessor和ItemWrite
 * 5）ItemReader 读取数据的接口
 * 6）ItemProcessor 处理数据的接口
 * 7）ItemWrite 输出数据的接口
 */
@Configuration
@EnableBatchProcessing // 开启批处理的支持
@Import(DruidConfiguration.class) // 注入datasource
@Slf4j
public class CsvBatchConfigForAmzDailyInventory {

    private Map<String, Object> paramMaps;

    private final static String filePath = "C:\\Users\\paulin.f\\Downloads\\Inventory Health_US.csv";
    private final static String fileName = "Inventory Health_US";

    @Autowired
    private StepBuilderFactory stepBuilderFactory;


    /**
     * ItemReader定义：读取文件数据+entirty映射
     *
     * @return
     */
    @Bean
    public ItemReader<AmzVcDailyInventory> readerForAmzDailyInventory() {
        // 使用FlatFileItemReader去读cvs文件，一行即一条数据
        FlatFileItemReader<AmzVcDailyInventory> reader = new FlatFileItemReader<>();
        // 设置文件处在路径
        // reader.setResource(new ClassPathResource("Inventory Health_US.csv"));
        reader.setResource(new FileSystemResource(filePath));
        reader.setLinesToSkip(2); // 跳过头两行
        // entity与csv数据做映射
        reader.setLineMapper(new DefaultLineMapper<AmzVcDailyInventory>() {
            {
                setLineTokenizer(new DelimitedLineTokenizer() {
                    {
                        // availableInventory与sellableOnHandUnits一样
                        setNames(new String[]{"asin", "productTitle", "netReceived", "netReceivedUnits", "sellThroughRate",
                                "openPurchaseOrderQuantity", "sellableOnHandInventory", "sellableOnHandInventoryTrailing30DayAverage",
                                "sellableOnHandUnits", "unsellableOnHandInventory", "unsellableOnHandInventoryTrailing30DayAverage",
                                "unsellableOnHandUnits", "aged90DaysSellableInventory", "aged90DaysSellableInventoryTrailing30DayAverage",
                                "aged90DaysSellableUnits", "replenishmentCategory"});
                    }

                });
                setFieldSetMapper(new BeanWrapperFieldSetMapper<AmzVcDailyInventory>() {
                    {
                        setTargetType(AmzVcDailyInventory.class);
                    }
                });
            }
        });
        return reader;
    }


    /**
     * 注册ItemProcessor: 处理数据+校验数据
     *
     * @return
     */
    @Bean
    public ItemProcessor<AmzVcDailyInventory, AmzVcDailyInventory> processorForAmzDailyInventory() {
        CsvItemProcessorForAmzDailyInventory csvItemProcessorForAmzDailyInventory = new CsvItemProcessorForAmzDailyInventory();
        // 设置校验器
        csvItemProcessorForAmzDailyInventory.setValidator(csvBeanValidatorForAmzDailyInventory());
        return csvItemProcessorForAmzDailyInventory;
    }

    /**
     * 注册校验器
     *
     * @return
     */
    @Bean
    public CsvBeanValidatorForAmzDailyInventory csvBeanValidatorForAmzDailyInventory() {
        return new CsvBeanValidatorForAmzDailyInventory<AmzVcDailyInventory>();
    }

    /**
     * ItemWriter定义：指定datasource，设置批量插入sql语句，写入数据库
     *
     * @param dataSource
     * @return
     */
    @Bean
    public ItemWriter<AmzVcDailyInventory> writerForAmzDailyInventory(DataSource dataSource) {
        // 使用jdbcBcatchItemWrite写数据到数据库中
        JdbcBatchItemWriter<AmzVcDailyInventory> writer = new JdbcBatchItemWriter<>();
        // 设置有参数的sql语句
        writer.setItemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<AmzVcDailyInventory>());
        // 处理头两行
        paramMaps = stepForAmzDailyInventoryPrepare(filePath);
        String sql = "";
        if (CollUtil.isNotEmpty(paramMaps)) {
            String viewingDate= paramMaps.get("viewing").toString().split("-")[0];
            String viewingDateEnd= paramMaps.get("viewing").toString().split("-")[1];
            viewingDate= DateUtil.format(DateUtil.parse(viewingDate, DateFormat.YEAR_MONTH_DAY_MMddyy1 ), DateFormat.YEAR_MONTH_DAY_yyyyMMdd);
            viewingDateEnd=DateUtil.format(DateUtil.parse(viewingDateEnd,DateFormat.YEAR_MONTH_DAY_MMddyy1 ), DateFormat.YEAR_MONTH_DAY_yyyyMMdd);
            // 每日库存报表插入sql
            sql = "INSERT INTO [dbo].[vendorInventoryInfo] ([ASIN], [product_title], [net_received], [net_received_units], [sell_through_rate], " +
                    "[open_purchase_order_quantity], [sellable_on_hand_inventory]," +
                    "[sellable_on_hand_inventory_trailing_30_day_average], [sellable_on_hand_units]," +
                    "[unsellable_on_hand_inventory], [unsellable_on_hand_inventory_trailing_30_day_average], " +
                    "[unsellable_on_hand_units], [aged_90+_days_sellable_inventory], " +
                    "[aged_90+_days_sellable_inventory_trailing_30_day_average], [aged_90+_days_sellable_units]," +
                    "[replenishment_category], [Available_Inventory], [insertTime]) " +
                    "VALUES (:asin,:productTitle,:netReceived,:netReceivedUnits,:sellThroughRate," +
                    ":openPurchaseOrderQuantity,:sellableOnHandInventory,:sellableOnHandInventoryTrailing30DayAverage," +
                    ":sellableOnHandUnits,:unsellableOnHandInventory,:unsellableOnHandInventoryTrailing30DayAverage," +
                    ":unsellableOnHandUnits,:aged90DaysSellableInventory,:aged90DaysSellableInventoryTrailing30DayAverage," +
                    ":aged90DaysSellableUnits,:replenishmentCategory,replace(:availableInventory,',',''),'" +
                    viewingDate + "')";
        } else {
            sql = "INSERT INTO [dbo].[BATCH_JOB_EXEC](EXEC_ID) VALUES (:asin)";
        }
        writer.setSql(sql);
        writer.setDataSource(dataSource);

        return writer;
    }

    /**
     * JobRepository定义：设置数据库，注册Job容器
     *
     * @param dataSource
     * @param transactionManager
     * @return
     * @throws Exception
     */
    @Bean
    public JobRepository cvsJobRepositoryForAmzDailyInventory(DataSource dataSource, PlatformTransactionManager transactionManager) throws Exception {
        JobRepositoryFactoryBean jobRepositoryFactoryBean = new JobRepositoryFactoryBean();
        jobRepositoryFactoryBean.setDatabaseType("sqlserver");
        jobRepositoryFactoryBean.setTransactionManager(transactionManager);
        jobRepositoryFactoryBean.setDataSource(dataSource);
        return jobRepositoryFactoryBean.getObject();
    }

    /**
     * jobLauncher定义：
     *
     * @param dataSource
     * @param transactionManager
     * @return
     * @throws Exception
     */
    @Bean
    public SimpleJobLauncher csvJobLauncherForAmzDailyInventory(DataSource dataSource, PlatformTransactionManager transactionManager) throws Exception {
        SimpleJobLauncher jobLauncher = new SimpleJobLauncher();
        // 设置jobRepository
        jobLauncher.setJobRepository(cvsJobRepositoryForAmzDailyInventory(dataSource, transactionManager));
        return jobLauncher;
    }

    /**
     * 定义job
     *
     * @param jobs
     * @param stepForAmzDailyInventory
     * @return
     */
    @Bean
    public Job importJobForAmzDailyInventory(JobBuilderFactory jobs, Step stepForAmzDailyInventoryCheckFile, Step stepForAmzDailyInventory, Step stepForAmzDailyInventoryDealFile) {
        return jobs.get("importJobForAmzDailyInventory")
                .incrementer(new RunIdIncrementer())
                .listener(csvJobListenerForAmzDailyInventory())
                .start(stepForAmzDailyInventoryCheckFile)
                .on("FAILED").end()
                .on("COMPLETED").to(stepForAmzDailyInventory)
                .from(stepForAmzDailyInventory).on("COMPLETED").to(stepForAmzDailyInventoryDealFile).end()
                .build();
    }


    /**
     * 注册job监听器
     *
     * @return
     */
    @Bean
    public CsvJobListenerForAmzDailyInventory csvJobListenerForAmzDailyInventory() {
        return new CsvJobListenerForAmzDailyInventory();
    }


    /**
     * 获取CSV文件信息处理入库
     * step定义：步骤包括ItemReader->ItemProcessor->ItemWriter 即读取数据->处理校验数据->写入数据
     *
     * @param stepBuilderFactoryForAmzDailyInventory
     * @param readerForAmzDailyInventory
     * @param writerForAmzDailyInventory
     * @param processorForAmzDailyInventory
     * @return
     */
    @Bean
    public Step stepForAmzDailyInventory(StepBuilderFactory stepBuilderFactoryForAmzDailyInventory, ItemReader<AmzVcDailyInventory> readerForAmzDailyInventory,
                                         ItemWriter<AmzVcDailyInventory> writerForAmzDailyInventory, ItemProcessor<AmzVcDailyInventory, AmzVcDailyInventory> processorForAmzDailyInventory) {
        DefaultTransactionAttribute attribute = new DefaultTransactionAttribute();
        attribute.setPropagationBehavior(Propagation.REQUIRED.value());
        attribute.setIsolationLevel(Isolation.DEFAULT.value());
        attribute.setTimeout(300);
        return stepBuilderFactoryForAmzDailyInventory
                .get("stepForAmzDailyInventory")
                .<AmzVcDailyInventory, AmzVcDailyInventory>chunk(10000) // Chunk的机制(即每次读取一条数据，再处理一条数据，累积到一定数量后再一次性交给writer进行写入操作)
                .reader(readerForAmzDailyInventory)
                .processor(processorForAmzDailyInventory)
                .writer(writerForAmzDailyInventory)
                .transactionAttribute(attribute)
                .build();
    }

    @Bean
    public Step stepForAmzDailyInventoryDealFile() {
        return stepBuilderFactory.get("stepForAmzDailyInventoryDealFile")
                .tasklet((contribution, context) -> {
                    // 获取前面Steps参数
                    ExecutionContext jobContext = context.getStepContext().getStepExecution().getJobExecution().getExecutionContext();
                    //Gets the data here
                    Map<String, Object> paramMaps = (Map<String, Object>) jobContext.get("paramMaps");
                    String viewingDate= paramMaps.get("viewing").toString().split("-")[0];
                    viewingDate=DateUtil.format(DateUtil.parse(viewingDate,DateFormat.YEAR_MONTH_DAY_MMddyy1 ), DateFormat.YEAR_MONTH_DAY_yyyyMMdd);
                    // 文件重命名
                    FileUtil.rename(new File(filePath), StrUtil.concat(true, fileName, "-", StrUtil.toString(paramMaps.get("reportingRange")), "-", viewingDate, "-", IdUtil.simpleUUID()), true, false);
                    log.info("stepForAmzDailyInventoryDealFile params: "+paramMaps.toString());
                    return RepeatStatus.FINISHED;
                }).build();
    }
     
    @Bean
    public Step stepForAmzDailyInventoryCheckFile() {
        return stepBuilderFactory.get("stepForAmzDailyInventoryCheckFile")
                .tasklet((StepContribution contribution, ChunkContext context) -> {
                    // 文件存在检查
                    if (FileUtil.exist(filePath)) {
                        // 文件头预处理
                        Map<String, Object> paramMaps = stepForAmzDailyInventoryPrepare(filePath);
                        ExecutionContext jobContext = context.getStepContext().getStepExecution().getJobExecution().getExecutionContext();
                        jobContext.put("paramMaps", paramMaps);

                        return RepeatStatus.FINISHED;
                    } else {
                        throw new FileNotFoundException("File Not Found : " + filePath);
                    }
                }).build();
    }

    /**
     * 预先读取文件处理
     *
     * @return
     */
    public Map<String, Object> stepForAmzDailyInventoryPrepare(String filePath) {
        Map<String, Object> resultMap = new HashMap<>();
        if (FileUtil.exist(filePath)) {
            // 读取文件第一行
            List<List<String>> csvRowList = CSVUtils.readCSVAdv(filePath, 0, 1, 9);
            // 获取报表维度及时间
            String reportingRange = csvRowList.get(0).get(5);
            String viewing = csvRowList.get(0).get(6);
            if (log.isInfoEnabled()) {
                log.info("reportingRange:" + reportingRange);
                log.info("viewing:" + viewing);
            }
            resultMap.put("reportingRange", reportingRange.substring(reportingRange.indexOf("[") + 1, reportingRange.indexOf("]")));
            resultMap.put("viewing", viewing.substring(viewing.indexOf("[") + 1, viewing.indexOf("]")));

            return resultMap;
        } else {
            return null;
        }
    }


}