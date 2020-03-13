package com.spider.amazon.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelUtil;
import com.spider.amazon.cons.RespErrorEnum;
import com.spider.amazon.cons.RespResult;
import com.spider.amazon.cons.TemplateHawNameCons;
import com.spider.amazon.mapper.*;
import com.spider.amazon.model.*;
import com.spider.amazon.service.HawProductService;
import com.spider.amazon.utils.CSVUtils;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.mybatis.spring.SqlSessionTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Haw商品服务实现类
 */
@Service("HawProductServiceImpl")
public class HawProductServiceImpl implements HawProductService {
    /**
     * 日志打印组件
     */
    private final Logger log = LoggerFactory.getLogger(HawProductServiceImpl.class);

    private final static int BATCHCOUNT=100;

    private final static String sheetName="Template-OUTDOOR_LIVING";

    private final static String sheetMatchName="ASIN-SKU Match";

    private final static int TOP_ROW=0;

    @Autowired
    private SqlSessionTemplate sqlSessionTemplate;

    @Autowired
    private SkuScrapyTaskDOMapper skuScrapyTaskDOMapper;

    @Autowired
    private VskuToPidMapDOMapper vskuToPidMapDOMapper;

    @Autowired
    private MAsinToVskuMapDOMapper mAsinToVskuMapDOMapper;

    @Autowired
    private SkuScrapyTaskVSkuListDOMapper skuScrapyTaskVSkuListDOMapper;

    @Autowired
    private HawSrapySkuPropertyInfoDOMapper hawSrapySkuPropertyInfoDOMapper;

    @Override
    public int taskFileVendorSkuIntoDB(Map<String, Object> paramMap) {

        log.info("任务文件VendorSku入库");
        String taskId= StrUtil.toString(paramMap.get("taskId"));

        // 查询获取该任务信息
        SkuScrapyTaskDO skuScrapyTaskDO= skuScrapyTaskDOMapper.queryItemInfoByTaskId(paramMap);
        log.debug("任务信息查询返回结果 SkuScrapyTaskDO: [{}]" ,skuScrapyTaskDO.toString());
        String fullFilePath =skuScrapyTaskDO.getUploadFilePath()+skuScrapyTaskDO.getUploadFileName() ;


        log.debug("获取sheet工作页 fullFilePath [{}]  taskId [{}] ",fullFilePath,taskId);
        ExcelReader reader = ExcelUtil.getReader(fullFilePath);
        Workbook workbook = reader.getWorkbook();
        Sheet sheet = workbook.getSheet(sheetName); // 获取产品页
        Sheet sheetMatch = workbook.getSheet(sheetMatchName); // 获取产品页


        log.info("处理Map数据入库");
        Map<String,Object> params = new HashMap<>();
        params.put("taskId",taskId);
        params.put("fullFilePath",fullFilePath);
        matchMapInDB(sheetMatch,0,0,params);


        log.info("处理vskulist数据入库");
        int startrow=4;
        int endrow=0;
        log.debug("sheet参数转换成数组 sheet=>[{}] startrow=>[{}] endrow=>[{}]  params=>[{}] ",sheet,startrow,endrow,params);
        List<SkuScrapyTaskVSkuListDO> resultList= transSheet2Do(sheet,startrow,endrow,params);
        log.debug("批量插入数据库 resultList=>[{}]",resultList);
        dealResult(resultList);


        log.info("整体更新表中的VendorSku");
        skuScrapyTaskVSkuListDOMapper.updateVendorSkuByTaskId(params);


        return RespResult.SUCC_OOM;
    }

    private int matchMapInDB( Sheet sheet,int startrow,int endrow ,Map<String,Object> params) {

        log.info("步骤 [matchMapInDB] sheet[{}] startrow[{}] endrow[{}] params[{}]",sheet,startrow,endrow,params);
        if (ObjectUtil.isEmpty(sheet)) { // sheet不存在，则不用进行match入库
            return RespResult.NO_RECORD;
        }

        log.info("进行match");
        String fullFilePath=String.valueOf(params.get("fullFilePath"));
        List<List<String>> matchList=CSVUtils.readCSVBySheetName(fullFilePath,sheetMatchName,startrow,endrow,0,5);

        log.info("matchList 遍历数据处理 [{}]",matchList);
        // 表头列
        List<String> topRowList=new ArrayList<>();
        Map<String,Integer> topRowMap=new HashMap<>(); // 记录列值下标
        // 当前数据列
        List<String> nowRowList=new ArrayList<>();
        for(int index =0;index<matchList.size();index++) {

            if (isTopRow(index)) {
                // 收集表头
                topRowList=matchList.get(index);
                topRowMap=buildTopRowMap(topRowList);
            } else {
                nowRowList.clear();
                nowRowList=matchList.get(index);
                // 入库MasinToVsku数据
                inDbForMasinToVskuData(nowRowList,topRowMap);
                // 入库VskuToPid数据
                inDbForVskuToPidData(nowRowList,topRowMap);
                // 入库SkuScrapyTaskVSkuList数据
                inDbPropertyData(params,nowRowList,topRowMap);
            }

        }

        return RespResult.SUCC_OOM;
    }

    /**
     * 是否首行
     * @param index
     * @return
     */
    private boolean isTopRow(int index) {
        return index==TOP_ROW?true:false;
    }

    /**
     * 构建表头map索引
     * @param topRowList
     * @return
     */
    private Map<String,Integer> buildTopRowMap(List<String> topRowList) {
        Map<String,Integer> resultMap=new HashMap<>();
        for(int index=0;index<topRowList.size();index++) {
            if (!resultMap.containsKey(topRowList.get(index))) {
                resultMap.put(topRowList.get(index),index);
            }
        }
        return resultMap;
    }

    /**
     * 入库MasinToVsku数据（存在更新，不存在插入）
     * @param nowRowList
     * @param topRowMap
     */
    private void inDbForMasinToVskuData(List<String> nowRowList ,Map<String,Integer> topRowMap) {
        mAsinToVskuMapDOMapper.insertOrUpdateRecord(
                MAsinToVskuMapDO.builder()
                        .vendorSku(nowRowList.get(topRowMap.get(TemplateHawNameCons.VENDOR_SKU_COMLUMN)))
                        .merchantSuggestedAsin(nowRowList.get(topRowMap.get(TemplateHawNameCons.MERCHANT_SUGGESTED_ASIN_COMLUMN)))
                        .effFlg("Y")
                        .insertTime(DateUtil.date())
                        .build()
        );
    }


    /**
     * 入库VskuToPid数据（存在更新，不存在插入）
     * @param nowRowList
     * @param topRowMap
     */
    private void inDbForVskuToPidData(List<String> nowRowList,Map<String,Integer> topRowMap) {
        vskuToPidMapDOMapper.insertOrUpdateRecord(VskuToPidMapDO.builder()
                .vendorSku(nowRowList.get(topRowMap.get(TemplateHawNameCons.VENDOR_SKU_COMLUMN)))
                .productId(StrUtil.subSuf(nowRowList.get(topRowMap.get(TemplateHawNameCons.VENDOR_SKU_COMLUMN)),4))
                .effFlg("Y")
                .insertTime(DateUtil.date())
                .build());
    }


    /**
     * 抽取数据入库
     * @param params
     * @param nowRowList
     * @param topRowMap
     */
    private void inDbPropertyData(Map<String,Object> params,List<String> nowRowList,Map<String,Integer> topRowMap)  {

        String entryKey=null;
        Object entryValue=null;
        Iterator topRowMapIte = topRowMap.entrySet().iterator();
        while (topRowMapIte.hasNext()) {
            Map.Entry entry = (Map.Entry) topRowMapIte.next();
            entryKey = (String) entry.getKey();
            entryValue =  entry.getValue();
            if (!StrUtil.equalsAny(entryKey,TemplateHawNameCons.MERCHANT_SUGGESTED_ASIN_COMLUMN)) { // 非id列
                // 非id列，填充进属性列表
                hawSrapySkuPropertyInfoDOMapper.insertSelective(
                        HawSrapySkuPropertyInfoDO.builder()
                            .taskId(String.valueOf(params.get("taskId")))
                            .insertTime(DateUtil.date())
                            .propertyValue(nowRowList.get((Integer) entryValue))
                            .propertyName(entryKey)
                            .productSimpleId(StrUtil.subSuf(nowRowList.get(topRowMap.get(TemplateHawNameCons.VENDOR_SKU_COMLUMN)),4))
                            .productId("")
                            .vendorSku(nowRowList.get(topRowMap.get(TemplateHawNameCons.VENDOR_SKU_COMLUMN)))
                                .merchantSuggestedAsin(nowRowList.get(topRowMap.get(TemplateHawNameCons.MERCHANT_SUGGESTED_ASIN_COMLUMN)))
                            .build()
                );
            }
        }
    }


    /**
     * 处理请求结果
     *
     * @param members 1.数据校验（暂无传输安全机制）
     *                2.数据处理
     *                3.数据入库 （支持数据快速入库）
     */
    private void dealResult(List<SkuScrapyTaskVSkuListDO> members) {

        // 1.数据校验
        if (ObjectUtil.isEmpty(members)) {
            if (log.isInfoEnabled()){
                log.info("服务器无返回数据");
            }
            return ;
        }

        // 2.数据处理
        int result = 1;
        SqlSession batchSqlSession = null;
        try {
            batchSqlSession = this.sqlSessionTemplate
                    .getSqlSessionFactory()
                    .openSession(ExecutorType.BATCH, false);// 获取批量方式的sqlsession
            //通过新的session获取mapper
            SkuScrapyTaskVSkuListDOMapper mapper = batchSqlSession.getMapper(SkuScrapyTaskVSkuListDOMapper.class);

            int batchCount = BATCHCOUNT;// 每批commit的个数
            int batchLastIndex = batchCount;// 每批最后一个的下标

            for (int index = 0; index < members.size(); ) {
                if (batchLastIndex >= members.size()) {
                    batchLastIndex = members.size();

                    result = result + mapper.insertBatch(members.subList(index, batchLastIndex));
                    batchSqlSession.commit();
                    //清理缓存，防止溢出
                    batchSqlSession.clearCache();
                    if (log.isInfoEnabled()) {
                        log.info("index:" + index + " batchLastIndex:" + batchLastIndex);
                    }
                    break;// 数据插入完毕，退出循环
                } else {

                    result = result + mapper.insertBatch(members.subList(index, batchLastIndex));
                    batchSqlSession.commit();
                    //清理缓存，防止溢出
                    batchSqlSession.clearCache();
                    if (log.isInfoEnabled()) {
                        log.info("index:" + index + " batchLastIndex:" + batchLastIndex);
                    }
                    index = batchLastIndex;// 设置下一批下标
                    batchLastIndex = index + (batchCount - 1);
                }
                if (log.isInfoEnabled()) {
                    log.info("=============>result=[" + result + "] begin=[" + index + "] end=[" + batchLastIndex + "]");
                }
            }
            batchSqlSession.commit();
        } catch (Exception e) {
            throw new RuntimeException(RespErrorEnum.SERVICE_DATA_EXPC.getSubStatusMsg());
        } finally {
            batchSqlSession.close();
        }

        return;
    }

    /**
     * 转换列表对象
     * @param sheet
     * @return
     */
    public List<SkuScrapyTaskVSkuListDO> transSheet2Do( Sheet sheet,int startrow,int endrow ,Map<String,Object> params) {
        List<SkuScrapyTaskVSkuListDO> reulstList = new ArrayList<>();
        int rowindex=startrow;
        if (endrow==0) {
            endrow=sheet.getLastRowNum();
        }
        for (;rowindex<=endrow;++rowindex) {
            Row row= sheet.getRow(rowindex);
            reulstList.add(SkuScrapyTaskVSkuListDO.builder()
                    .taskId(StrUtil.toString(params.get("taskId")))
                    .vendorSku("")
                    .merchantSuggestedAsin(row.getCell(7).getStringCellValue())
                    .insertTime(DateUtil.date()).build());
        }

        return reulstList;
    }

    public static void main(String[] args) {
//        ExcelReader reader = ExcelUtil.getReader("/Users/zhucan/Downloads/BIUploadFile/Template.xlsm");
//        Workbook workbook = reader.getWorkbook();
//        Sheet sheet = workbook.getSheet("Template-OUTDOOR_LIVING"); // 获取第二个工作簿
//        Row row= sheet.getRow(4);
//        System.out.println(" row："+row.getCell(1).getStringCellValue());
//        System.out.println(sheet.getLastRowNum());
    }

}