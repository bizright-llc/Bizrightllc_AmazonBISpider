package com.spider.amazon.batch.vcdailysales;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.spider.amazon.config.DruidConfiguration;
import com.spider.amazon.cons.DateFormat;
import com.spider.amazon.entity.AmzVcDailySales;
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
public class CsvBatchConfigForAmzDailySales {

    private Map<String, Object> paramMaps;

    private final static String filePath = "C:\\Users\\paulin.f\\Downloads\\Sales Diagnostic_Detail View_US.csv";
    private final static String fileName = "Sales Diagnostic_Detail View_US";

    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    /**
     * ItemReader定义：读取文件数据+entirty映射
     *
     * @return
     */
    @Bean
    public ItemReader<AmzVcDailySales> readerForAmzDailySales() {
        // 使用FlatFileItemReader去读cvs文件，一行即一条数据
        FlatFileItemReader<AmzVcDailySales> reader = new FlatFileItemReader<>();
        // 设置文件处在路径
//        reader.setResource(new ClassPathResource("Sales Diagnostic_Detail View_US.csv"));
        reader.setResource(new FileSystemResource(filePath));
        reader.setLinesToSkip(2); // 跳过头两行
        // entity与csv数据做映射
        reader.setLineMapper(new DefaultLineMapper<AmzVcDailySales>() {
            {
                setLineTokenizer(new DelimitedLineTokenizer() {
                    {
                        setNames(new String[]{"asin", "productTitle", "shippedCogs", "shippedCogsOfTotal", "shippedCogsPriorPeriod",
                                "shippedCogsLastyear", "shippedUnits", "shippedUnitsOfTotal", "shippedUnitsPriorPeriod", "shippedUnitsLastYear",
                                "customerReturns", "freeReplacements","AverageSalesPrice","AverageSalesPricePriorPeriod"});
                    }
                });
                setFieldSetMapper(new BeanWrapperFieldSetMapper<AmzVcDailySales>() {
                    {
                        setTargetType(AmzVcDailySales.class);
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
    public ItemProcessor<AmzVcDailySales, AmzVcDailySales> processorForAmzDailySales() {
        CsvItemProcessorForAmzDailySales csvItemProcessorForAmzDailySales = new CsvItemProcessorForAmzDailySales();
        // 设置校验器
        csvItemProcessorForAmzDailySales.setValidator(csvBeanValidatorForAmzDailySales());
        return csvItemProcessorForAmzDailySales;
    }

    /**
     * 注册校验器
     *
     * @return
     */
    @Bean
    public CsvBeanValidatorForAmzDailySales csvBeanValidatorForAmzDailySales() {
        return new CsvBeanValidatorForAmzDailySales<AmzVcDailySales>();
    }

    /**
     * ItemWriter定义：指定datasource，设置批量插入sql语句，写入数据库
     *
     * @param dataSource
     * @return
     */
    @Bean
    public ItemWriter<AmzVcDailySales> writerForAmzDailySales(DataSource dataSource) {
        // 使用jdbcBcatchItemWrite写数据到数据库中
        JdbcBatchItemWriter<AmzVcDailySales> writer = new JdbcBatchItemWriter<>();
        // 设置有参数的sql语句
        writer.setItemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<AmzVcDailySales>());
        // 判断是销量日报还是周报
        String sql = "";
        paramMaps=stepForAmzDailySalesPrepare(filePath);

        log.info("filePath:"+filePath);
        log.info("paramMaps:"+CollUtil.isEmpty(paramMaps));
        if (CollUtil.isNotEmpty(paramMaps)) {
            String viewingDate= paramMaps.get("viewing").toString().split("-")[0];
            String viewingDateEnd= paramMaps.get("viewing").toString().split("-")[1];
            viewingDate=DateUtil.format(DateUtil.parse(viewingDate,DateFormat.YEAR_MONTH_DAY_MMddyy1 ), DateFormat.YEAR_MONTH_DAY_yyyyMMdd);
            viewingDateEnd=DateUtil.format(DateUtil.parse(viewingDateEnd,DateFormat.YEAR_MONTH_DAY_MMddyy1 ), DateFormat.YEAR_MONTH_DAY_yyyyMMdd);
            if (paramMaps.get("reportingRange").toString().equals("Weekly")) {
                sql = "INSERT INTO [dbo].[vendorWeeklySalesInfo]([asin], [product_title], [shipped_cogs], " +
                        "[shipped_cogs_of_total], [shipped_cogs_prior_period], [shipped_cogs_last_year], " +
                        "[shipped_units], [shipped_units_of_total], [shipped_units_prior_period], " +
                        "[shipped_units_last_year], [customer_returns], [free_replacements], " +
                        "[viewing_date],[viewing_date_end]) VALUES (:asin,:productTitle,:shippedCogs,:shippedCogsOfTotal,:shippedCogsPriorPeriod," +
                        ":shippedCogsLastyear,:shippedUnits,:shippedUnitsOfTotal,:shippedUnitsPriorPeriod,:shippedUnitsLastYear," +
                        ":customerReturns,:freeReplacements,'" + viewingDate + "','" + viewingDateEnd + "')";
            } else if (paramMaps.get("reportingRange").toString().equals("Daily")) {
                sql = "INSERT INTO [dbo].[vendorSalesInfo]([asin], [product_title], [shipped_cogs], " +
                        "[shipped_cogs_of_total], [shipped_cogs_prior_period], [shipped_cogs_last_year], " +
                        "[shipped_units], [shipped_units_of_total], [shipped_units_prior_period], " +
                        "[shipped_units_last_year], [customer_returns], [free_replacements], " +
                        "[viewing_date]) VALUES (:asin,:productTitle,:shippedCogs,:shippedCogsOfTotal,:shippedCogsPriorPeriod," +
                        ":shippedCogsLastyear,:shippedUnits,:shippedUnitsOfTotal,:shippedUnitsPriorPeriod,:shippedUnitsLastYear," +
                        ":customerReturns,:freeReplacements,'" + viewingDate + "')";
            }
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
    public JobRepository cvsJobRepositoryForAmzDailySales(DataSource dataSource, PlatformTransactionManager transactionManager) throws Exception {
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
    public SimpleJobLauncher csvJobLauncherForAmzDailySales(DataSource dataSource, PlatformTransactionManager transactionManager) throws Exception {
        SimpleJobLauncher jobLauncher = new SimpleJobLauncher();
        // 设置jobRepository
        jobLauncher.setJobRepository(cvsJobRepositoryForAmzDailySales(dataSource, transactionManager));
        return jobLauncher;
    }

    /**
     * 定义job
     *
     * @param jobs
     * @param stepForAmzDailySales
     * @return
     */
    @Bean
    public Job importJobForAmzDailySales(JobBuilderFactory jobs, Step stepForAmzDailySalesCheckFile, Step stepForAmzDailySales, Step stepForAmzDailySalesDealFile) {
        return jobs.get("importJobForAmzDailySales")
                .incrementer(new RunIdIncrementer())
                .listener(csvJobListenerForAmzDailySales())
                .start(stepForAmzDailySalesCheckFile)
                .on("FAILED").end()
                .on("COMPLETED").to(stepForAmzDailySales)
                .from(stepForAmzDailySales).on("COMPLETED").to(stepForAmzDailySalesDealFile).end()
                .build();
    }

    /**
     * 注册job监听器
     *
     * @return
     */
    @Bean
    public CsvJobListenerForAmzDailySales csvJobListenerForAmzDailySales() {
        return new CsvJobListenerForAmzDailySales();
    }


    /**
     * 获取CSV文件信息处理入库
     * step定义：步骤包括ItemReader->ItemProcessor->ItemWriter 即读取数据->处理校验数据->写入数据
     *
     * @param stepBuilderFactoryForAmzDailySales
     * @param readerForAmzDailySales
     * @param writerForAmzDailySales
     * @param processorForAmzDailySales
     * @return
     */
    @Bean
    public Step stepForAmzDailySales(StepBuilderFactory stepBuilderFactoryForAmzDailySales, ItemReader<AmzVcDailySales> readerForAmzDailySales,
                                     ItemWriter<AmzVcDailySales> writerForAmzDailySales, ItemProcessor<AmzVcDailySales, AmzVcDailySales> processorForAmzDailySales) {
        DefaultTransactionAttribute attribute = new DefaultTransactionAttribute();
        attribute.setPropagationBehavior(Propagation.REQUIRED.value());
        attribute.setIsolationLevel(Isolation.DEFAULT.value());
        attribute.setTimeout(300);
        return stepBuilderFactoryForAmzDailySales
                .get("stepForAmzDailySales")
                .<AmzVcDailySales, AmzVcDailySales>chunk(10000) // Chunk的机制(即每次读取一条数据，再处理一条数据，累积到一定数量后再一次性交给writer进行写入操作)
                .reader(readerForAmzDailySales)
                .processor(processorForAmzDailySales)
                .writer(writerForAmzDailySales)
                .transactionAttribute(attribute)
                .build();
    }


    @Bean
    public Step stepForAmzDailySalesDealFile() {
        return stepBuilderFactory.get("stepForAmzDailySalesDealFile")
                .tasklet((contribution, context) -> {
                    // 获取前面Steps参数
                    ExecutionContext jobContext = context.getStepContext().getStepExecution().getJobExecution().getExecutionContext();
                    //Gets the data here
                    Map<String, Object> paramMaps = (Map<String, Object>) jobContext.get("paramMaps");
                    String viewingDate= paramMaps.get("viewing").toString().split("-")[0];
                    viewingDate=DateUtil.format(DateUtil.parse(viewingDate,DateFormat.YEAR_MONTH_DAY_MMddyy1 ), DateFormat.YEAR_MONTH_DAY_yyyyMMdd);
                    // 文件重命名
                    // 周销量报表和日销量报表名字区别
                    FileUtil.rename(new File(filePath), StrUtil.concat(true, fileName, "-", StrUtil.toString(paramMaps.get("reportingRange")), "-",viewingDate, "-", IdUtil.simpleUUID()), true, false);
                    return RepeatStatus.FINISHED;
                }).build();
    }

    @Bean
    public Step stepForAmzDailySalesCheckFile() {
        return stepBuilderFactory.get("stepForAmzDailySalesCheckFile")
                .tasklet((StepContribution contribution, ChunkContext context) -> {
                    // 文件存在检查
                    if (FileUtil.exist(filePath)) {
                        // 文件头预处理
                        Map<String, Object> paramMaps = stepForAmzDailySalesPrepare(filePath);
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
    public Map<String, Object> stepForAmzDailySalesPrepare(String filePath) {
        Map<String, Object> resultMap = new HashMap<>();
        if (FileUtil.exist(filePath)) {
            // 读取文件第一行
            List<List<String>> csvRowList = CSVUtils.readCSVAdv(filePath, 0, 1, 11);
            // 获取报表维度及时间
            String reportingRange = csvRowList.get(0).get(7);
            String viewing = csvRowList.get(0).get(8);
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