package com.spider.amazon.batch.scbuyboxinfo;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.spider.amazon.config.DruidConfiguration;
import com.spider.amazon.config.SpiderConfig;
import com.spider.amazon.entity.AmzScBuyBox;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;


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
public class CsvBatchConfigForAmzScBuyBox {

    public final static String FILE_PATH_CONTEXT = "filepath";
    public final static String PARAM_MAPS_CONTEXT = "paramMaps";
    public final static String PARAM_MAPS_VIEWING_DATE = "viewingDate";
    public final static String FILE_NAME = "BusinessReport";
    public final static String COMPLETE_MARK = "Processed";

//    private final static String filePath = "C:\\Users\\paulin.f\\Downloads\\BusinessReport";
    private final static int OFF_SET = 0;

    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    @Autowired
    private SpiderConfig spiderConfig;

    @Autowired
    private SqlSessionFactory sqlSessionFactory;


    /**
     * ItemReader定义：读取文件数据+entirty映射
     *
     * @return
     */
//    @Bean
//    public ItemReader<AmzScBuyBox> readerForAmzScBuyBox() {
//        // 使用FlatFileItemReader去读cvs文件，一行即一条数据
//        FlatFileItemReader<AmzScBuyBox> reader = new FlatFileItemReader<>();
//        // 设置文件处在路径
//        // reader.setResource(new ClassPathResource("Inventory Health_US.csv"));
//        // 文件路径 示例：BusinessReport-11-20-19 今天下载文件名日期是上一天的日期
//        String fullFilePath=StrUtil.concat(true,filePath,"-", DateUtil.format(DateUtil.offsetDay(DateUtil.date(),OFF_SET), DateFormat.YEAR_MONTH_DAY_Mdyy),".csv");
//        reader.setResource(new FileSystemResource(fullFilePath));
//        reader.setLinesToSkip(1); // 跳过头两行
//        // entity与csv数据做映射
//        reader.setLineMapper(new DefaultLineMapper<AmzScBuyBox>() {
//            {
//                setLineTokenizer(new DelimitedLineTokenizer() {
//                    {
//                        // availableInventory与sellableOnHandUnits一样
//                        setNames(new String[]{"parentAsin", "childAsin", "title", "sessions", "sessionPercentage",
//                                "pageViews", "pageViewsPercentage", "buyBoxPercentage",
//                                "unitsOrdered", "unitsOrderedB2B", "unitSessionPercentage",
//                                "unitSessionPercentageB2B", "orderedProductSales", "orderedProductSalesB2B",
//                                "totalOrderItems", "totalOrderItemsB2B"});
//                    }
//
//                });
//                setFieldSetMapper(new BeanWrapperFieldSetMapper<AmzScBuyBox>() {
//                    {
//                        setTargetType(AmzScBuyBox.class);
//                    }
//                });
//            }
//        });
//        return reader;
//    }

    @Bean
    @StepScope
    public FlatFileItemReader<AmzScBuyBox> readerForAmzScBuyBox(@Value("#{jobExecutionContext[filepath]}") String filepath,
                                                                              @Value("#{jobExecutionContext[paramMaps]}") Map<String, Object> paramMaps) throws IOException {

        return fileReader(filepath);

    }

    private FlatFileItemReader<AmzScBuyBox> fileReader(String filePath) throws IOException {
        FlatFileItemReader<AmzScBuyBox> itemReader = new FlatFileItemReader<>();

        String tableHeader = Files.readAllLines(Paths.get(filePath)).get(0);

        itemReader.setResource(new FileSystemResource(filePath));
        itemReader.setLineMapper(lineMapper(tableHeader));
        itemReader.setLinesToSkip(1);
        itemReader.setStrict(true);

        return itemReader;
    }

    private LineMapper<AmzScBuyBox> lineMapper(String head) {

        DefaultLineMapper<AmzScBuyBox> lineMapper = new DefaultLineMapper<>();
        DelimitedLineTokenizer lineTokenizer = new DelimitedLineTokenizer();
        lineTokenizer.setDelimiter(",");
        lineTokenizer.setStrict(true);

        head = head.replaceAll("[^a-zA-Z0-9,]", "");

        String[] heads = head.split(",");
        List<String> names = new ArrayList<>();

        for (String h : heads) {

            String clearH = h.trim();

            String propertyName = null;

            for (Field f : AmzScBuyBox.class.getDeclaredFields()) {
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

        lineMapper.setFieldSetMapper(new BeanWrapperFieldSetMapper<AmzScBuyBox>() {{
            setTargetType(AmzScBuyBox.class);
        }});

        return lineMapper;
    }


    /**
     * 注册ItemProcessor: 处理数据+校验数据
     *
     * @return
     */
    @Bean
    @StepScope
    public ItemProcessor<AmzScBuyBox, AmzScBuyBox> processorForAmzScBuyBox(@Value("#{jobExecutionContext[paramMaps]}") Map<String, Object> paramMaps) {
        CsvItemProcessorForAmzScBuyBox csvItemProcessorForAmzScBuyBox = new CsvItemProcessorForAmzScBuyBox(paramMaps);
        // 设置校验器
        csvItemProcessorForAmzScBuyBox.setValidator(csvBeanValidatorForAmzScBuyBox());
        return csvItemProcessorForAmzScBuyBox;
    }

    /**
     * 注册校验器
     *
     * @return
     */
    @Bean
    public CsvBeanValidatorForAmzScBuyBox csvBeanValidatorForAmzScBuyBox() {
        return new CsvBeanValidatorForAmzScBuyBox<AmzScBuyBox>();
    }

    /**
     * ItemWriter定义：指定datasource，设置批量插入sql语句，写入数据库
     *
     * @param dataSource
     * @return
     */
//    @Bean
//    public ItemWriter<AmzScBuyBox> writerForAmzScBuyBox(DataSource dataSource) {
//        // 使用jdbcBcatchItemWrite写数据到数据库中
//        JdbcBatchItemWriter<AmzScBuyBox> writer = new JdbcBatchItemWriter<>();
//        // 设置有参数的sql语句
//        writer.setItemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<AmzScBuyBox>());
//        // 预处理头部数据
////        String fullFilePath=StrUtil.concat(true,filePath,"-", DateUtil.format(DateUtil.offsetDay(DateUtil.date(),OFF_SET), DateFormat.YEAR_MONTH_DAY_Mdyy),".csv");
//        // 起始结束日期
//        String fromDate = DateUtil.format(UsDateUtils.beginOfWeek(DateUtil.lastWeek()), DateFormat.YEAR_MONTH_DAY_yyyyMMdd);
//        String toDate = DateUtil.format(UsDateUtils.endOfWeek(DateUtil.lastWeek()), DateFormat.YEAR_MONTH_DAY_yyyyMMdd);
//        String sql = "";
//        if (FileUtil.exist(fullFilePath)) {
//            // 每日库存报表插入sql
//            sql = "INSERT INTO [dbo].[BusinessReport]([parent_asin], [child_asin], [title]," +
//                    " [sessions], [session_percentage], [page_views], " +
//                    "[page_views_percentage], [buy_box_percentage], [units_ordered], " +
//                    "[units_ordered-B2B], [unit_session_percentage], [unit_session_percentage-B2B], " +
//                    "[ordered_product_sales], [ordered_product_sales-B2B], [total_order_items], " +
//                    "[total_order_items-B2B], [date_from], [date_to]) " +
//                    "VALUES (:parentAsin, :childAsin, :title, " +
//                    ":sessions, :sessionPercentage, :pageViews, " +
//                    ":pageViewsPercentage, :buyBoxPercentage, :unitsOrdered, " +
//                    ":unitsOrderedB2B, :unitSessionPercentage, :unitSessionPercentageB2B, " +
//                    ":orderedProductSales, :orderedProductSalesB2B, :totalOrderItems, " +
//                    ":totalOrderItemsB2B,'"+fromDate+"','"+toDate+"');";
//        } else {
//            sql = "INSERT INTO [dbo].[BATCH_JOB_EXEC](EXEC_ID) VALUES (:child_asin)";
//        }
//        writer.setSql(sql);
//        writer.setDataSource(dataSource);
//
//        return writer;
//    }

    /**
     * MyBatis batch writer
     *
     * @return
     */
    @Bean
    public MyBatisBatchItemWriter<AmzScBuyBox> AmzScBuyBoxBatchItemWriter() {
        final MyBatisBatchItemWriter<AmzScBuyBox> writer = new MyBatisBatchItemWriter<>();

        writer.setSqlSessionFactory(this.sqlSessionFactory);
        writer.setStatementId("com.spider.amazon.mapper.AmzScBuyBoxMapper.insert");

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
    public JobRepository cvsJobRepositoryForAmzScBuyBox(DataSource dataSource, PlatformTransactionManager transactionManager) throws Exception {
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
    public SimpleJobLauncher csvJobLauncherForAmzScBuyBox(DataSource dataSource, PlatformTransactionManager transactionManager) throws Exception {
        SimpleJobLauncher jobLauncher = new SimpleJobLauncher();
        // 设置jobRepository
        jobLauncher.setJobRepository(cvsJobRepositoryForAmzScBuyBox(dataSource, transactionManager));
        return jobLauncher;
    }

    /**
     * 定义job
     * @param jobs
     * @param stepForAmzScBuyBoxCheckFile
     * @param stepForAmzScBuyBox
     * @param stepForAmzScBuyBoxDealFile
     * @return
     */
    @Bean
    public Job importJobForAmzScBuyBox(JobBuilderFactory jobs, Step stepForAmzScBuyBoxCheckFile, Step stepForAmzScBuyBox, Step stepForAmzScBuyBoxDealFile) {
        return jobs.get("importJobForAmzScBuyBox")
                .incrementer(new RunIdIncrementer())
                .listener(csvJobListenerForAmzScBuyBox())
                .start(stepForAmzScBuyBoxCheckFile)
                .on("FAILED").end()
                .on("COMPLETED").to(stepForAmzScBuyBox)
                .from(stepForAmzScBuyBox).on("COMPLETED").to(stepForAmzScBuyBoxDealFile).end()
                .build();
    }


    /**
     * 注册job监听器
     *
     * @return
     */
    @Bean
    public CsvJobListenerForAmzScBuyBox csvJobListenerForAmzScBuyBox() {
        return new CsvJobListenerForAmzScBuyBox();
    }


    /**
     * 获取CSV文件信息处理入库
     * step定义：步骤包括ItemReader->ItemProcessor->ItemWriter 即读取数据->处理校验数据->写入数据
     *
     * @param stepBuilderFactoryForAmzScBuyBox
     * @param readerForAmzScBuyBox
     * @param AmzScBuyBoxBatchItemWriter
     * @param processorForAmzScBuyBox
     * @return
     */
    @Bean
    public Step stepForAmzScBuyBox(StepBuilderFactory stepBuilderFactoryForAmzScBuyBox, ItemReader<AmzScBuyBox> readerForAmzScBuyBox,
                                   MyBatisBatchItemWriter<AmzScBuyBox> AmzScBuyBoxBatchItemWriter, ItemProcessor<AmzScBuyBox, AmzScBuyBox> processorForAmzScBuyBox) {
        DefaultTransactionAttribute attribute = new DefaultTransactionAttribute();
        attribute.setPropagationBehavior(Propagation.REQUIRED.value());
        attribute.setIsolationLevel(Isolation.DEFAULT.value());
        attribute.setTimeout(300);
        return stepBuilderFactoryForAmzScBuyBox
                .get("stepForAmzScBuyBox")
                .<AmzScBuyBox, AmzScBuyBox>chunk(10000) // Chunk的机制(即每次读取一条数据，再处理一条数据，累积到一定数量后再一次性交给writer进行写入操作)
                .reader(readerForAmzScBuyBox)
                .processor(processorForAmzScBuyBox)
                .writer(AmzScBuyBoxBatchItemWriter)
                .transactionAttribute(attribute)
                .build();
    }

    /**
     * Change file name after dealing with the file
     * @return
     */
    @Bean
    public Step stepForAmzScBuyBoxDealFile() {
        return stepBuilderFactory.get("stepForAmzScBuyBoxDealFile")
                .tasklet((contribution, context) -> {

                    // 获取前面Steps参数
                    ExecutionContext jobContext = context.getStepContext().getStepExecution().getJobExecution().getExecutionContext();

                    String filepath = jobContext.get(FILE_PATH_CONTEXT).toString();

                    Path oldFilepath = Paths.get(filepath);

                    //Gets the data here
                    Map<String, Object> paramMaps = (Map<String, Object>) jobContext.get("paramMaps");

                    String reportDateStr = paramMaps.get(PARAM_MAPS_VIEWING_DATE).toString();

                    // 文件重命名
                    String newFilename = StrUtil.concat(true,FILE_NAME,"-", reportDateStr, "-", COMPLETE_MARK, "-", IdUtil.simpleUUID());
                    Files.move(oldFilepath, oldFilepath.resolveSibling(newFilename+".csv"));
                    log.info("[stepForAmzScBuyBoxDealFile] Rename file: {} to file name: {}", filepath, newFilename);
                    return RepeatStatus.FINISHED;
                }).build();
    }

    /**
     * Check the file existed or not
     * @return
     */
    @Bean
    public Step stepForAmzScBuyBoxCheckFile() {
        return stepBuilderFactory.get("stepForAmzScBuyBoxCheckFile")
                .tasklet((StepContribution contribution, ChunkContext context) -> {

                    class MyFileFilter implements FileFilter {

                        public boolean accept(File f) {

                            final String regex = "BusinessReport-\\d{8}.csv";

                            final Pattern pattern = Pattern.compile(regex, Pattern.MULTILINE);

                            String fileName = f.getName();

                            Matcher matcher = pattern.matcher(fileName);

                            if (f.getName().contains(FILE_NAME) && !f.getName().contains(COMPLETE_MARK) && matcher.find()) {
                                return true;
                            }
                            return false;
                        }
                    }

                    MyFileFilter filter = new MyFileFilter();

                    File[] files = FileUtils.getFileFromDir(spiderConfig.getScBuyBoxDownloadPath(), filter);

                    File file = files.length > 0 ? files[0] : null;

                    // 文件存在检查
                    if (file != null && FileUtil.exist(file.getPath())) {

                        log.info("[Pre handle file information]");

                        // 文件头预处理
                        Map<String, Object> paramMaps = new HashMap<>();
                        ExecutionContext jobContext = context.getStepContext().getStepExecution().getJobExecution().getExecutionContext();

                        String orgFilename = file.getName();

                        // file date is the report date
                        String viewingDate = orgFilename.substring(orgFilename.indexOf("-")+1, orgFilename.indexOf("."));

                        paramMaps.put(PARAM_MAPS_VIEWING_DATE, viewingDate);

                        jobContext.put(PARAM_MAPS_CONTEXT, paramMaps);

                        jobContext.put(FILE_PATH_CONTEXT, file.getPath());

                        return RepeatStatus.FINISHED;
                    } else {
                        throw new FileNotFoundException("File Not Found : " + file.getPath());
                    }
                }).build();
    }

}