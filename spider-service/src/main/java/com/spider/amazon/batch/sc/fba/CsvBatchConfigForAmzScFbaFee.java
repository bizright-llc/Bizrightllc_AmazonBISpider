package com.spider.amazon.batch.sc.fba;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.spider.amazon.config.DruidConfiguration;
import com.spider.amazon.config.SpiderConfig;
import com.spider.amazon.entity.AmzScFbaFee;
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
public class CsvBatchConfigForAmzScFbaFee {

    public final static String FILE_PATH_CONTEXT = "filepath";
    public final static String PROCESS_UUID = "processUUID";
    public final static String NEW_FILE_NAME = "FbaTransaction";
    public final static String COMPLETE_MARK = "Processed";

//    private final static String filePath = "C:\\Users\\paulin.f\\Downloads\\BusinessReport";
    private final static int OFF_SET = 0;

    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    @Autowired
    private SpiderConfig spiderConfig;

    @Autowired
    private SqlSessionFactory sqlSessionFactory;

    @Bean
    @StepScope
    public FlatFileItemReader<AmzScFbaFee> readerForAmzScFbaFee(@Value("#{jobExecutionContext[filepath]}") String filepath,
                                                                @Value("#{jobExecutionContext[paramMaps]}") Map<String, Object> paramMaps) throws IOException {

        return fileReader(filepath);

    }

    private FlatFileItemReader<AmzScFbaFee> fileReader(String filePath) throws IOException {
        FlatFileItemReader<AmzScFbaFee> itemReader = new FlatFileItemReader<>();

        String tableHeader = Files.readAllLines(Paths.get(filePath)).get(7);

        itemReader.setResource(new FileSystemResource(filePath));
        itemReader.setLineMapper(lineMapper(tableHeader));
        itemReader.setLinesToSkip(7);
        itemReader.setStrict(true);

        return itemReader;
    }

    private LineMapper<AmzScFbaFee> lineMapper(String head) {

        DefaultLineMapper<AmzScFbaFee> lineMapper = new DefaultLineMapper<>();
        DelimitedLineTokenizer lineTokenizer = new DelimitedLineTokenizer();
        lineTokenizer.setDelimiter(",");
        lineTokenizer.setStrict(true);

        head = head.replaceAll("[^a-zA-Z0-9,]", "");

        String[] heads = head.split(",");
        List<String> names = new ArrayList<>();

        for (String h : heads) {

            String clearH = h.trim();

            String propertyName = null;

            for (Field f : AmzScFbaFee.class.getDeclaredFields()) {
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

        lineMapper.setFieldSetMapper(new BeanWrapperFieldSetMapper<AmzScFbaFee>() {{
            setTargetType(AmzScFbaFee.class);
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
    public ItemProcessor<AmzScFbaFee, AmzScFbaFee> processorForAmzScFbaFee(@Value("#{jobExecutionContext[paramMaps]}") Map<String, Object> paramMaps) {
        CsvItemProcessorForAmzScFbaFee csvItemProcessorForAmzScFbaFee = new CsvItemProcessorForAmzScFbaFee(paramMaps);
        // 设置校验器
        csvItemProcessorForAmzScFbaFee.setValidator(csvBeanValidatorForAmzScFbaFee());
        return csvItemProcessorForAmzScFbaFee;
    }

    /**
     * 注册校验器
     *
     * @return
     */
    @Bean
    public CsvBeanValidatorForAmzScFbaFee csvBeanValidatorForAmzScFbaFee() {
        return new CsvBeanValidatorForAmzScFbaFee<AmzScFbaFee>();
    }

    /**
     * MyBatis batch writer
     *
     * @return
     */
    @Bean
    public MyBatisBatchItemWriter<AmzScFbaFee> AmzScFbaFeeBatchItemWriter() {
        final MyBatisBatchItemWriter<AmzScFbaFee> writer = new MyBatisBatchItemWriter<>();

        writer.setSqlSessionFactory(this.sqlSessionFactory);
        writer.setStatementId("com.spider.amazon.mapper.AmzScFbaFeeMapper.insert");

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
    public JobRepository cvsJobRepositoryForAmzScFbaFee(DataSource dataSource, PlatformTransactionManager transactionManager) throws Exception {
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
    public SimpleJobLauncher csvJobLauncherForAmzScFbaFee(DataSource dataSource, PlatformTransactionManager transactionManager) throws Exception {
        SimpleJobLauncher jobLauncher = new SimpleJobLauncher();
        // 设置jobRepository
        jobLauncher.setJobRepository(cvsJobRepositoryForAmzScFbaFee(dataSource, transactionManager));
        return jobLauncher;
    }

    /**
     * 定义job
     * @param jobs
     * @param stepForAmzScFbaFeeCheckFile
     * @param stepForAmzScFbaFee
     * @param stepForAmzScFbaFeeDealFile
     * @return
     */
    @Bean
    public Job importJobForAmzScFbaFee(JobBuilderFactory jobs, Step stepForAmzScFbaFeeCheckFile, Step stepForAmzScFbaFee, Step stepForAmzScFbaFeeDealFile) {
        return jobs.get("importJobForAmzScFbaFee")
                .incrementer(new RunIdIncrementer())
                .listener(csvJobListenerForAmzScFbaFee())
                .start(stepForAmzScFbaFeeCheckFile)
                .on("FAILED").end()
                .on("COMPLETED").to(stepForAmzScFbaFee)
                .from(stepForAmzScFbaFee).on("COMPLETED").to(stepForAmzScFbaFeeDealFile).end()
                .build();
    }


    /**
     * 注册job监听器
     *
     * @return
     */
    @Bean
    public CsvJobListenerForAmzScFbaFee csvJobListenerForAmzScFbaFee() {
        return new CsvJobListenerForAmzScFbaFee();
    }


    /**
     * 获取CSV文件信息处理入库
     * step定义：步骤包括ItemReader->ItemProcessor->ItemWriter 即读取数据->处理校验数据->写入数据
     *
     * @param stepBuilderFactoryForAmzScFbaFee
     * @param readerForAmzScFbaFee
     * @param AmzScFbaFeeBatchItemWriter
     * @param processorForAmzScFbaFee
     * @return
     */
    @Bean
    public Step stepForAmzScFbaFee(StepBuilderFactory stepBuilderFactoryForAmzScFbaFee, ItemReader<AmzScFbaFee> readerForAmzScFbaFee,
                                   MyBatisBatchItemWriter<AmzScFbaFee> AmzScFbaFeeBatchItemWriter, ItemProcessor<AmzScFbaFee, AmzScFbaFee> processorForAmzScFbaFee) {
        DefaultTransactionAttribute attribute = new DefaultTransactionAttribute();
        attribute.setPropagationBehavior(Propagation.REQUIRED.value());
        attribute.setIsolationLevel(Isolation.DEFAULT.value());
        attribute.setTimeout(300);
        return stepBuilderFactoryForAmzScFbaFee
                .get("stepForAmzScFbaFee")
                .<AmzScFbaFee, AmzScFbaFee>chunk(1000) // Chunk的机制(即每次读取一条数据，再处理一条数据，累积到一定数量后再一次性交给writer进行写入操作)
                .reader(readerForAmzScFbaFee)
                .processor(processorForAmzScFbaFee)
                .writer(AmzScFbaFeeBatchItemWriter)
                .transactionAttribute(attribute)
                .build();
    }

    /**
     * Change file name after dealing with the file
     * @return
     */
    @Bean
    public Step stepForAmzScFbaFeeDealFile() {
        return stepBuilderFactory.get("stepForAmzScFbaFeeDealFile")
                .tasklet((contribution, context) -> {

                    // 获取前面Steps参数
                    ExecutionContext jobContext = context.getStepContext().getStepExecution().getJobExecution().getExecutionContext();

                    String filepath = jobContext.get(FILE_PATH_CONTEXT).toString();
                    String processUuid = jobContext.get(PROCESS_UUID).toString();

                    Path oldFilepath = Paths.get(filepath);

                    //Gets the data here
                    Map<String, Object> paramMaps = (Map<String, Object>) jobContext.get("paramMaps");

                    // 文件重命名
                    String newFilename = StrUtil.concat(true,NEW_FILE_NAME,"-", processUuid, "-", COMPLETE_MARK);
                    Files.move(oldFilepath, oldFilepath.resolveSibling(newFilename+".csv"));
                    log.info("[stepForAmzScFbaFeeDealFile] Rename file: {} to file name: {}", filepath, newFilename);
                    return RepeatStatus.FINISHED;
                }).build();
    }

    /**
     * Check the file existed or not
     * @return
     */
    @Bean
    public Step stepForAmzScFbaFeeCheckFile() {
        return stepBuilderFactory.get("stepForAmzScFbaFeeFile")
                .tasklet((StepContribution contribution, ChunkContext context) -> {

//                    class MyFileFilter implements FileFilter {
//
//                        public boolean accept(File f) {
//
//                            final String regex = "BusinessReport-\\d{8}.csv";
//
//                            final Pattern pattern = Pattern.compile(regex, Pattern.MULTILINE);
//
//                            String fileName = f.getName();
//
//                            Matcher matcher = pattern.matcher(fileName);
//
//                            if (f.getName().contains(FILE_NAME) && !f.getName().contains(COMPLETE_MARK) && matcher.find()) {
//                                return true;
//                            }
//                            return false;
//                        }
//                    }

//                    MyFileFilter filter = new MyFileFilter();

                    File[] files = FileUtils.getFileFromDir(spiderConfig.getScFbaFeeDownloadPath(), null);

                    File file = files.length > 0 ? files[0] : null;

                    // 文件存在检查
                    if (file != null && FileUtil.exist(file.getPath())) {

                        log.info("[Pre handle file information]");

                        String filename = UUID.randomUUID().toString();

                        // 文件头预处理
                        Map<String, Object> paramMaps = new HashMap<>();
                        ExecutionContext jobContext = context.getStepContext().getStepExecution().getJobExecution().getExecutionContext();

                        jobContext.put(FILE_PATH_CONTEXT, file.getPath());
                        jobContext.put(PROCESS_UUID, UUID.randomUUID().toString());

                        return RepeatStatus.FINISHED;
                    } else {
                        throw new FileNotFoundException("File Not Found : " + file.getPath());
                    }
                }).build();
    }

}