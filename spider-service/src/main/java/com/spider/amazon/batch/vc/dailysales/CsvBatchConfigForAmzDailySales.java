package com.spider.amazon.batch.vc.dailysales;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.common.exception.ServiceException;
import com.spider.amazon.config.DruidConfiguration;
import com.spider.amazon.config.SpiderConfig;
import com.spider.amazon.cons.DateFormat;
import com.spider.amazon.cons.RespErrorEnum;
import com.spider.amazon.entity.AmzVcDailySales;
import com.spider.amazon.utils.CSVUtils;
import com.spider.amazon.utils.FileUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.batch.MyBatisBatchItemWriter;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.launch.support.SimpleJobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.support.JobRepositoryFactoryBean;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.LineMapper;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
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

    //    private final static String filePath = "C:\\Users\\paulin.f\\Downloads\\Sales Diagnostic_Detail View_US.csv";
    private final static String fileName = "Sales Diagnostic_Detail View_US";

    private final String FINISHED_FILE_MARK = "FINISHED";

    @Autowired
    private SpiderConfig spiderConfig;

    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    @Autowired
    private SqlSessionFactory sqlSessionFactory;

    private String head;

    /**
     * ItemReader定义：读取文件数据+entirty映射
     *
     * @return
     */
    @Bean
    @StepScope
    public FlatFileItemReader<AmzVcDailySales> readerForAmzDailySales(@Value("#{jobExecutionContext[filePath]}") String filePath,
                                                                      @Value("#{jobExecutionContext[paramMaps]}") Map<String, Object> paramMaps) throws IOException {

        String distributorView = paramMaps.get("distributorView").toString();

        return reader(filePath);

//        if (distributorView.equals("Sourcing")) {
//            return getSourcingDistributorReader(filePath);
//        } else if (distributorView.equals("Manufacturing")) {
//            return getManufacturingDistributorReader(filePath);
//        } else {
//            log.info("File {} distributor view is not support", filePath);
//            throw new ServiceException(RespErrorEnum.TASK_DEAL_ERROR.toString(), String.format("File %d distributor view is not support", filePath));
//        }

    }


    /**
     * 注册ItemProcessor: 处理数据+校验数据
     *
     * @return
     */
    @Bean
    @StepScope
    public ItemProcessor<AmzVcDailySales, AmzVcDailySales> processorForAmzDailySales(@Value("#{jobExecutionContext[paramMaps]}") Map<String, Object> paramMaps) {
        CsvItemProcessorForAmzDailySales csvItemProcessorForAmzDailySales = new CsvItemProcessorForAmzDailySales(paramMaps);
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

//    /**
//     * ItemWriter定义：指定datasource，设置批量插入sql语句，写入数据库
//     *
//     * @param dataSource
//     * @return
//     */
//    @Bean
//    @StepScope
//    public ItemWriter<AmzVcDailySales> writerForAmzDailySales(@Value("#{jobExecutionContext[filePath]}") String filePath,
//                                                              @Value("#{jobExecutionContext[paramMaps]}") Map<String, Object> paramMaps,
//                                                              DataSource dataSource) {
//        // 使用jdbcBcatchItemWrite写数据到数据库中
//        JdbcBatchItemWriter<AmzVcDailySales> writer = new JdbcBatchItemWriter<>();
//
//        // 设置有参数的sql语句
//        writer.setItemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<AmzVcDailySales>());
////        // 判断是销量日报还是周报
//        // 不需判
//        String sql = "";
//
//        log.info("filePath:" + filePath);
//        log.info("paramMaps:" + CollUtil.isEmpty(paramMaps));
//        if (CollUtil.isNotEmpty(paramMaps)) {
//            // Check distributor view type
//            String distributorView = paramMaps.get("distributorView").toString();
//
//            if (distributorView.equals("Sourcing")) {
//
//                sql = String.format("INSERT INTO [vendorSalesInfo]([asin], [product_title], [shipped_cogs], " +
//                        "[shipped_cogs_of_total], [shipped_cogs_prior_period], [shipped_cogs_last_year], " +
//                        "[shipped_units], [shipped_units_of_total], [shipped_units_prior_period], " +
//                        "[shipped_units_last_year], [customer_returns], [free_replacements], " +
//                        "[viewing_date], [DistributorView]) VALUES (:asin,:productTitle,:shippedCogs,:shippedCogsOfTotal,:shippedCogsPriorPeriod," +
//                        ":shippedCogsLastYear,:shippedUnits,:shippedUnitsOfTotal,:shippedUnitsPriorPeriod,:shippedUnitsLastYear," +
//                        ":customerReturns,:freeReplacements, :viewingDate, %s)", distributorView);
//
//            } else if (distributorView.equals("Manufacturing")) {
//
////                sql = "INSERT INTO [vendorSalesInfo]([asin], [product_title], [shipped_cogs]," +
////                        "[shipped_cogs_of_total], [shipped_cogs_prior_period], [shipped_cogs_last_year]," +
////                        "[shipped_units],[shipped_units_of_total], [shipped_units_prior_period]," +
////                        "[shipped_units_last_year],[customer_returns],[free_replacements],[subcategory_sales_rank]," +
////                        "[average_sales_price],[average_sales_price_prior_period]," +
////                        "[change_in_glance_view_prior_period],[change_in_glance_view_last_year],[rep_oos]," +
////                        "[rep_oos_of_total],[rep_oos_prior_period],[lbb_price],[viewing_date]) " +
////                        " VALUES (" +
////                        ":asin,:productTitle,:shippedCogs,:shippedCogsOfTotal,:shippedCogsPriorPeriod," +
////                        ":shippedCogsLastYear,:shippedUnits,:shippedUnitsOfTotal,:shippedUnitsPriorPeriod,:shippedUnitsLastYear," +
////                        ":customerReturns,:freeReplacements," +
////                        ":SubcategorySalesRank, :AverageSalesPrice," +
////                        ":AverageSalesPricePriorPeriod, :ChangeInGlanceViewPriorPeriod, " +
////                        ":ChangeInGVLastYear, :RepOOS, :RepOOSofTotal, :RepOOSPriorPeriod," +
////                        ":LBBPrice, :viewingDate" +
////                        ")";
//
////                sql = String.format("INSERT INTO [vendorSalesInfo]([asin], [product_title], [shipped_cogs]," +
////                        "[shipped_cogs_of_total], [shipped_cogs_prior_period], [shipped_cogs_last_year]," +
////                        "[shipped_units],[shipped_units_of_total], [shipped_units_prior_period]," +
////                        "[shipped_units_last_year],[customer_returns],[free_replacements],[subcategory_sales_rank]," +
////                        "[average_sales_price],[average_sales_price_prior_period]," +
////                        "[change_in_glance_view_prior_period],[change_in_glance_view_last_year],[rep_oos]," +
////                        "[rep_oos_of_total],[rep_oos_prior_period],[lbb_price],[viewing_date], [DistributorView]) " +
////                        " VALUES (" +
////                        ":asin,:productTitle,:shippedCogs,:shippedCogsOfTotal,:shippedCogsPriorPeriod," +
////                        ":shippedCogsLastYear,:shippedUnits,:shippedUnitsOfTotal,:shippedUnitsPriorPeriod,:shippedUnitsLastYear," +
////                        ":customerReturns,:freeReplacements," +
////                        ":SubcategorySalesRank, :AverageSalesPrice," +
////                        ":AverageSalesPricePriorPeriod, :ChangeInGlanceViewPriorPeriod, " +
////                        ":ChangeInGVLastYear, :RepOOS, :RepOOSofTotal, :RepOOSPriorPeriod," +
////                        ":LBBPrice, :viewingDate, '%s'" +
////                        ")", distributorView);
//
//                sql = String.format("INSERT INTO [vendorSalesInfo](asin, subcategory_better_worse) VALUES (:asin, 'test')");
//
//            }
//
//        } else {
//            sql = "INSERT INTO [dbo].[BATCH_JOB_EXEC](EXEC_ID) VALUES (:asin)";
//        }
//        writer.setSql(sql);
//        writer.setDataSource(dataSource);
//
//        return writer;
//    }

    @Bean
    public MyBatisBatchItemWriter<AmzVcDailySales> AmzVcDailySalesBatchItemWriter() {
        final MyBatisBatchItemWriter<AmzVcDailySales> writer = new MyBatisBatchItemWriter<>();

        writer.setSqlSessionFactory(this.sqlSessionFactory);
        writer.setStatementId("com.spider.amazon.mapper.AmzVcDailySalesMapper.insert");

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
     * @param AmzVcDailyInventoryBatchItemWriter
     * @param processorForAmzDailySales
     * @return
     */
    @Bean
    public Step stepForAmzDailySales(StepBuilderFactory stepBuilderFactoryForAmzDailySales, ItemReader<AmzVcDailySales> readerForAmzDailySales,
                                     MyBatisBatchItemWriter<AmzVcDailySales> AmzVcDailyInventoryBatchItemWriter, ItemProcessor<AmzVcDailySales, AmzVcDailySales> processorForAmzDailySales) {
        DefaultTransactionAttribute attribute = new DefaultTransactionAttribute();
        attribute.setPropagationBehavior(Propagation.REQUIRED.value());
        attribute.setIsolationLevel(Isolation.DEFAULT.value());
        attribute.setTimeout(300);
        return stepBuilderFactoryForAmzDailySales
                .get("stepForAmzDailySales")
                .<AmzVcDailySales, AmzVcDailySales>chunk(10000) // Chunk的机制(即每次读取一条数据，再处理一条数据，累积到一定数量后再一次性交给writer进行写入操作)
                .reader(readerForAmzDailySales)
                .processor(processorForAmzDailySales)
                .writer(AmzVcDailyInventoryBatchItemWriter)
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
                    String filePath = jobContext.get("filePath").toString();
                    String viewingDate = paramMaps.get("viewingDate").toString();
                    String reportingRange = paramMaps.get("reportingRange").toString();
                    viewingDate = DateUtil.format(DateUtil.parse(viewingDate, DateFormat.YEAR_MONTH_DAY_MMddyyyy), DateFormat.YEAR_MONTH_DAY_yyyyMMdd);

                    // 文件重命名
                    // 周销量报表和日销量报表名字区别
                    if (FileUtil.exist(filePath)) {

                        File finishedFile = new File(filePath);

                        Path oldFilePath = Paths.get(finishedFile.getPath());

                        try {

                            String newFileName = StrUtil.concat(true, fileName, "-", reportingRange, "-", viewingDate, "-", IdUtil.simpleUUID());

                            //make sure file path doesn't have '/'
                            Files.move(oldFilePath, oldFilePath.resolveSibling(newFileName + ".csv"));

                            log.info("File rename {}", newFileName);

                        } catch (Exception ex) {
                            log.info("File {} rename failed", oldFilePath, ex);
                        }

                    }

                    return RepeatStatus.FINISHED;
                }).build();
    }

    @Bean
    public Step stepForAmzDailySalesCheckFile() {
        return stepBuilderFactory.get("stepForAmzDailySalesCheckFile")
                .tasklet((StepContribution contribution, ChunkContext context) -> {

                    //Filter getting the file of daily sales
                    class MyFileFilter implements FileFilter {

                        public boolean accept(File f) {
                            // Check if the file name doesn't have the finished tag
                            if (f.getName().contains(fileName) && !f.getName().contains("FINISHED")) {
                                return true;
                            }
                            return false;
                        }
                    }

                    MyFileFilter filter = new MyFileFilter();

                    File[] files = FileUtils.getFileFromDir(spiderConfig.getVcDailySalesDownloadPath(), filter);

                    if (files == null || files.length == 0) {
                        log.info("No daily sales file to process");
                        throw new FileNotFoundException("No Daily Sales Files");
                    }

                    File file = files.length > 0 ? files[0] : null;

                    // 文件存在检查
                    if (file != null && FileUtil.exist(file.getPath())) {
                        // 文件头预处理
                        Map<String, Object> paramMaps = stepForAmzDailySalesPrepare(file.getPath());
                        ExecutionContext jobContext = context.getStepContext().getStepExecution().getJobExecution().getExecutionContext();
                        jobContext.put("paramMaps", paramMaps);
                        jobContext.put("filePath", file.getAbsolutePath());

                        return RepeatStatus.FINISHED;
                    } else {
                        throw new FileNotFoundException("File Not Found : " + file == null ? "" : file.getAbsolutePath());
                    }
                }).build();
    }


    /**
     * Pre handle the file need to batch insert
     * 预先读取文件处理
     *
     * @return
     */
    public Map<String, Object> stepForAmzDailySalesPrepare(String filePath) {

        log.info("[stepForAmzDailySalesPrepare]");

        Map<String, Object> resultMap = new HashMap<>();
        if (FileUtil.exist(filePath)) {
            // 读取文件第一行
            List<List<String>> csvRowList = CSVUtils.readCSVAdv(filePath, 0, 1, 11);

            // 获取报表维度及时间
            String distributorView = csvRowList.get(0).get(1);
            String salesView = csvRowList.get(0).get(2);
            String reportingRange = csvRowList.get(0).get(7);
            String viewing = csvRowList.get(0).get(8);

            distributorView = distributorView.substring(distributorView.indexOf("[") + 1, distributorView.indexOf("]"));
            salesView = salesView.substring(salesView.indexOf("[") + 1, salesView.indexOf("]"));

            reportingRange = reportingRange.substring(reportingRange.indexOf("[") + 1, reportingRange.indexOf("]"));

            viewing = viewing.substring(viewing.indexOf("[") + 1, viewing.indexOf("]"));

            String viewingDateStart = viewing.split("-")[0].trim();
            String viewingDateEnd = viewing.split("-")[1].trim();

            viewingDateStart = DateUtil.format(DateUtil.parse(viewingDateStart, DateFormat.YEAR_MONTH_DAY_MMddyy1), DateFormat.YEAR_MONTH_DAY_MMddyyyy);
            viewingDateEnd = DateUtil.format(DateUtil.parse(viewingDateEnd, DateFormat.YEAR_MONTH_DAY_MMddyy1), DateFormat.YEAR_MONTH_DAY_MMddyyyy);

            if (log.isInfoEnabled()) {
                log.info("distributorView: " + distributorView);
                log.info("reportingRange:" + reportingRange);
                log.info("viewingDate:" + viewingDateStart);
                log.info("viewingDateEnd" + viewingDateEnd);
                log.info("salesView", salesView);
            }
            resultMap.put("distributorView", distributorView);
            resultMap.put("reportingRange", reportingRange);
            resultMap.put("viewingDate", viewingDateStart);
            resultMap.put("viewingDateEnd", viewingDateEnd);
            resultMap.put("salesView", salesView);

            if (distributorView.equalsIgnoreCase("Sourcing") && distributorView.equalsIgnoreCase("Manufacturing")) {
                log.info("File {} distributor view {} is not support", filePath, distributorView);
                throw new ServiceException(RespErrorEnum.TASK_DEAL_ERROR.toString(), String.format("File %d distributor view %s is not support", filePath, distributorView));
            }

            return resultMap;
        } else {
            return null;
        }
    }

    private FlatFileItemReader<AmzVcDailySales> reader(String filePath) throws IOException {
        FlatFileItemReader<AmzVcDailySales> itemReader = new FlatFileItemReader<>();

        String tableHeader = Files.readAllLines(Paths.get(filePath)).get(1);

        itemReader.setResource(new FileSystemResource(filePath));
        itemReader.setLineMapper(lineMapper(tableHeader));
        itemReader.setLinesToSkip(2);
        itemReader.setStrict(true);

        return itemReader;
    }

    private LineMapper<AmzVcDailySales> lineMapper(String head) {

        DefaultLineMapper<AmzVcDailySales> lineMapper = new DefaultLineMapper<>();
        DelimitedLineTokenizer lineTokenizer = new DelimitedLineTokenizer();
        lineTokenizer.setDelimiter(",");
        lineTokenizer.setStrict(true);

        head = head.replaceAll("[^a-zA-Z0-9,]", "");

        String[] heads = head.split(",");
        List<String> names = new ArrayList<>();

        for (String h : heads) {

            String clearH = h.trim();

            String propertyName = null;

            for (Field f : AmzVcDailySales.class.getDeclaredFields()) {
                if (f.getName().equalsIgnoreCase(clearH)) {
                    propertyName = f.getName();
                }
            }

            if (propertyName == null) {
                log.info("Property name not found for header {}", clearH);
            } else {
                names.add(propertyName);
            }
        }

        String[] namesArr = new String[names.size()];

        lineTokenizer.setNames(names.toArray(namesArr));

        lineMapper.setLineTokenizer(lineTokenizer);

        lineMapper.setFieldSetMapper(new BeanWrapperFieldSetMapper<AmzVcDailySales>() {{
            setTargetType(AmzVcDailySales.class);
        }});

        return lineMapper;
    }
}