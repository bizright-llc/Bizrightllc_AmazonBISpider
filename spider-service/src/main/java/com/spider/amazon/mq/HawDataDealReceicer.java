package com.spider.amazon.mq;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelUtil;
import com.spider.amazon.cons.TaskSts;
import com.spider.amazon.cons.TemplateHawNameCons;
import com.spider.amazon.cons.TemplateHawTransNameCons;
import com.spider.amazon.cons.TemplateTypeEnum;
import com.spider.amazon.handler.TemplateDealDataForHawHandler;
import com.spider.amazon.mapper.HawSrapySkuInfoDOMapper;
import com.spider.amazon.mapper.HawSrapySkuPropertyInfoDOMapper;
import com.spider.amazon.mapper.SkuScrapyTaskDOMapper;
import com.spider.amazon.model.HawSrapySkuInfoDO;
import com.spider.amazon.model.HawSrapySkuPropertyInfoDO;
import com.spider.amazon.model.SkuScrapyTaskDO;
import com.spider.amazon.utils.CSVUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

@Component
@Slf4j
public class HawDataDealReceicer {

    @Autowired
    private SkuScrapyTaskDOMapper skuScrapyTaskDOMapper;

    @Autowired
    private HawSrapySkuInfoDOMapper hawSrapySkuInfoDOMapper;

    @Autowired
    private HawSrapySkuPropertyInfoDOMapper hawSrapySkuPropertyInfoDOMapper;

    @Autowired
    private TemplateDealDataForHawHandler templateDealDataForHawHandler;

    private static final int START_COL=0;
    private static final int END_COL=200;
    private static final String SHEET_NAME= "Template-OUTDOOR_LIVING";

    /**
     * 基本信息固定映射
     */
    private static final String PRODUCT_ID_MAP_COMLUMN="Merchant Suggested Asin" ;


    @RabbitListener(queues = "${haw.dealdata.queue.name}", containerFactory = "singleListenerContainer")
    public void consumeHawQueue(Message message) {
        log.info("TASK：[Haw处理抓取数据]  message=>[{}]", StrUtil.str(message.getBody(), message.getMessageProperties().getContentEncoding()));
        Map<String, Object> params = JSONUtil.parseObj(StrUtil.str(message.getBody(), message.getMessageProperties().getContentEncoding()));

//    @RabbitListener(queues = "${haw.dealdata.queue.name}", containerFactory = "singleListenerContainer")
//    public void consumeHawQueue(Message message) {
//        log.info("测试消费者");
//    }
//
//    public void consumeHawQueue1() {
//        Map<String, Object> params = new HashMap<>();
//        params.put("taskId", "03ee6a7ddb7c4aebbceab020d9323f91");

        // 0.查询任务信息
        log.info("查询任务信息  params=>[{}]", params.toString());
        SkuScrapyTaskDO skuScrapyTaskDO = skuScrapyTaskDOMapper.queryItemInfoByTaskId(params);
        log.info("任务信息  skuScrapyTaskDO=>[{}]", skuScrapyTaskDO);

        // 1.查询商品主信息
        log.info("查询商品主信息  params=>[{}]", params.toString());
        List<HawSrapySkuInfoDO> hawSrapySkuInfoList = hawSrapySkuInfoDOMapper.queryInfoByTaskId(params);
        log.info("商品主信息  hawSrapySkuInfoList=>[{}]", hawSrapySkuInfoList);

        // 2.查询商品列表信息
        log.info("查询商品列表  params=>[{}]", params.toString());
        List<HawSrapySkuPropertyInfoDO> hawSrapySkuPropertyInfoList = hawSrapySkuPropertyInfoDOMapper.queryItemListByTaskId(params);
        log.info("商品列表  hawSrapySkuPropertyInfoList=>[{}]", hawSrapySkuPropertyInfoList);

        // 3.桶装筛选数据
        // 3.1商品主属性Map组装
        log.info("商品主属性Map组装=> [{}] ",hawSrapySkuInfoList);
        Map<String,Object>  itemInfoMap=buildItemInfo(hawSrapySkuInfoList);

        // 3.2商品属性列表Map组装
        log.info("商品属性列表Map组装=> [{}] ",hawSrapySkuPropertyInfoList);
        Map<String,Object>  propertyMap=buildPropertyItemInfo(hawSrapySkuPropertyInfoList);

        // 3.3当前文件属性列表名收集,有序map集合
        log.info("当前文件属性列表名收集");
        List<String> allColumnNameList=new ArrayList<>();
        Map<String,Object>  columnNameMap=buildColumnName(StrUtil.concat(true,skuScrapyTaskDO.getUploadFilePath(),skuScrapyTaskDO.getUploadFileName()),SHEET_NAME,2,allColumnNameList);

        // 4.处理现有数据
        log.info("数据处理");
        Map<String,Object> allNameValueMap= dealInfoData(itemInfoMap,propertyMap);

        // 5.填充及生成报表，传入任务主要信息实体，列名Map，收集到的数据匹配填充MAP
        log.info("报表生成");
        String newFileName=matchDataAndGenerate(skuScrapyTaskDO,columnNameMap,allNameValueMap);
        if (ObjectUtil.isEmpty(newFileName)) {
            log.error("报表生成失败");
            return;
        }


        // 6.更新任务状态及信息
        log.info("更新任务状态及信息");
        Map<String, Object> updResultParams = new HashMap<>();
        updResultParams.put("taskId",skuScrapyTaskDO.getTaskId());
        updResultParams.put("oldTaskSts", TaskSts.TASK_SCRAPYSUCC);
        updResultParams.put("taskSts", TaskSts.TASK_SUCCESS);
        updResultParams.put("downloadFilePath", skuScrapyTaskDO.getUploadFilePath());
        updResultParams.put("downloadFileName", newFileName);
        skuScrapyTaskDOMapper.updateByTaskStsAndTaskId(updResultParams);

    }

    /**
     * 收集商品主要信息
     * @param hawSrapySkuInfoList
     * @return
     */
    private Map<String,Object> buildItemInfo(List<HawSrapySkuInfoDO> hawSrapySkuInfoList) {
        Map<String,Object> resutlMap=new HashMap<>();

        for (HawSrapySkuInfoDO hawSrapySkuInfoDO:hawSrapySkuInfoList) {
            resutlMap.put(hawSrapySkuInfoDO.getMerchantSuggestedAsin(),hawSrapySkuInfoDO);
        }

        return resutlMap;
    }

    /**
     * 收集商品属性列表信息
     * @param hawSrapySkuPropertyInfoList
     * @return
     */
    private Map<String,Object> buildPropertyItemInfo(List<HawSrapySkuPropertyInfoDO> hawSrapySkuPropertyInfoList) {
        Map<String,Object> resutlMap=new HashMap<>();
        for (HawSrapySkuPropertyInfoDO hawSrapySkuPropertyInfoDO:hawSrapySkuPropertyInfoList) {
            if (resutlMap.containsKey(hawSrapySkuPropertyInfoDO.getMerchantSuggestedAsin())) {
                Map<String,Object> propertyMap = (LinkedHashMap<String, Object>) resutlMap.get(hawSrapySkuPropertyInfoDO.getMerchantSuggestedAsin());
                propertyMap.put(hawSrapySkuPropertyInfoDO.getPropertyName(),hawSrapySkuPropertyInfoDO.getPropertyValue());
            } else {
                Map<String,Object> propertyMap =new LinkedHashMap<>();
                propertyMap.put(hawSrapySkuPropertyInfoDO.getPropertyName(),hawSrapySkuPropertyInfoDO.getPropertyValue());
                resutlMap.put( hawSrapySkuPropertyInfoDO.getMerchantSuggestedAsin(),propertyMap);
            }
        }
        return resutlMap;
    }

    /**
     * 收集列名
     * @param filePath
     * @param sheetName
     * @param colNameRow
     * @return
     */
    private Map<String,Object> buildColumnName(String filePath,String sheetName,int colNameRow,List<String> allColumnNameList) {
        return CSVUtils.readCSVBuildMap(filePath,sheetName,colNameRow,colNameRow+1,START_COL,END_COL,allColumnNameList);
    }

    /**
     * match数据生成report
     * @param skuScrapyTaskDO
     * @param columnNameMap
     * @param allNameValueMap
     */
    private String matchDataAndGenerate(SkuScrapyTaskDO skuScrapyTaskDO,Map<String,Object>  columnNameMap, Map<String,Object>  allNameValueMap) {

        // 行读取
        ExcelReader reader = ExcelUtil.getReader(StrUtil.concat(true,skuScrapyTaskDO.getUploadFilePath(),skuScrapyTaskDO.getUploadFileName()));
        Workbook workbook = reader.getWorkbook();
        Sheet sheet = workbook.getSheet(SHEET_NAME); // 获取第二个工作簿

        /**
         * 部分固定列下标先固定
         */
        int ItemTypeNameIndex=12;
        int ItemTypeKeywordIndex=11;
        int ModelNameIndex=13;
        int ItemNameIndex=3;


        /**
         * 表头行处理
         * 收集不同列名对应的下标值
         */
        int topRowIndex=2;
        Row topRow=sheet.getRow(topRowIndex);
        int idIndex=-1;
        for (int topRowColIndex=0;topRowColIndex<topRow.getLastCellNum();topRowColIndex++) {
            if ( CSVUtils.getCellValueByCell(topRow.getCell(topRowColIndex)).equals(PRODUCT_ID_MAP_COMLUMN)) {
                idIndex=topRowColIndex; // 记录id映射坐标
            }
            // 登记出现列名的下标
            if (columnNameMap.containsKey(CSVUtils.getCellValueByCell(topRow.getCell(topRowColIndex)))) {
                String topRowStr=String.valueOf(columnNameMap.get(CSVUtils.getCellValueByCell(topRow.getCell(topRowColIndex))));
                columnNameMap.put((CSVUtils.getCellValueByCell(topRow.getCell(topRowColIndex))),StrUtil.concat(true,topRowStr,String.valueOf(topRowColIndex),"|"));
            }
        }

        /**
         * 数据行处理
         */
        int rowIndex=0;
        int startRowNum=4;
        for (;rowIndex<= sheet.getLastRowNum();rowIndex++) {
            // 起始行
            if (rowIndex < startRowNum) {
                continue;
            }
            Row row= sheet.getRow(rowIndex);

            /**
             * 1.直接遍历待填充map，进行填充
             * 获取每一行的主键进行映射
             * 1）第一层，Map<String,Object> 通过vendorSku获取对应的实体
             * 2）第二层，Map<String,Object> 通过列名获取对应的待填充列值
             * 3）第三层，ArrayList<Objecct> 该列值可能有很多同名列，是一个列表值
             */

            String priKey=CSVUtils.getCellValueByCell(row.getCell(idIndex));
            if (ObjectUtil.isNotEmpty(allNameValueMap.get(priKey))) { // 待填充Map实体非空，进行填充操作
                Map<String,Object> singleIdMap= (HashMap<String, Object>) allNameValueMap.get(priKey);


                String key = null;
                List<Object> value = null;
                Iterator singleIdMapIte = singleIdMap.entrySet().iterator();
                while (singleIdMapIte.hasNext()) {
                    Map.Entry entry = (Map.Entry) singleIdMapIte.next();
                    key = (String) entry.getKey();
                    value = (ArrayList<Object>) entry.getValue();
                    for (int listIndex=0;listIndex<value.size();listIndex++) {
                        if (ObjectUtil.isNotEmpty(columnNameMap.get(key))) { // 文件列名中存在key对应列名
                            String [] nameIndexStr=String.valueOf(columnNameMap.get(key)).split("\\|");
                            for (int arrayIndex=0;arrayIndex<value.size();arrayIndex++) {
                                if (arrayIndex<nameIndexStr.length) {
                                    if (ObjectUtil.isEmpty(row.getCell(Integer.valueOf(nameIndexStr[arrayIndex])))) {
                                        Cell cell= row.createCell(Integer.valueOf(nameIndexStr[arrayIndex]));
                                        setCellValueByColumnName(cell,key,value.get(arrayIndex));
                                    } else {
                                        Cell cell= row.getCell(Integer.valueOf(nameIndexStr[arrayIndex]));
                                        setCellValueByColumnName(cell,key,value.get(arrayIndex));
                                    }
                                }
                            }
                        }
                    }
                }

                // 其它装填行处理
                // Item Type Name
                if (ObjectUtil.isEmpty(row.getCell(ItemTypeNameIndex))) {
                    Cell cell1= row.createCell(ItemTypeNameIndex);
                    setCellValueByColumnName(cell1,TemplateHawNameCons.ITEM_TYPE_NAME_COMLUMN,CSVUtils.getCellValueByCell(row.getCell(ItemTypeKeywordIndex)));
                } else {
                    Cell cell1= row.getCell(ItemTypeNameIndex);
                    setCellValueByColumnName(cell1,TemplateHawNameCons.ITEM_TYPE_NAME_COMLUMN,CSVUtils.getCellValueByCell(row.getCell(ItemTypeKeywordIndex)));
                }
                // Model Name
                if (ObjectUtil.isEmpty(row.getCell(ModelNameIndex))) {
                    Cell cell2= row.createCell(ModelNameIndex);
                    setCellValueByColumnName(cell2,TemplateHawNameCons.MODEL_NAME_COMLUMN,CSVUtils.getCellValueByCell(row.getCell(ItemNameIndex)));
                } else {
                    Cell cell2= row.getCell(ModelNameIndex);
                    setCellValueByColumnName(cell2,TemplateHawNameCons.MODEL_NAME_COMLUMN,CSVUtils.getCellValueByCell(row.getCell(ItemNameIndex)));
                }

            } else { // 本行不需要进行填充，继续
                continue;
            }
        }

        FileOutputStream out=null;
        String nowFileName=skuScrapyTaskDO.getUploadFileName();
        String newFileName=StrUtil.concat(true,StrUtil.subPre(nowFileName,nowFileName.lastIndexOf(".")), "-",IdUtil.simpleUUID(),StrUtil.subSuf(nowFileName,nowFileName.lastIndexOf(".")));
        try{
            out = new FileOutputStream(StrUtil.concat(true,skuScrapyTaskDO.getUploadFilePath(),newFileName));
            workbook.write(out);
        }catch(IOException e){
            log.info(e.toString());
        }finally{
            try {
                out.close();
            }catch(IOException e){
                log.info(e.toString());
            }
        }
        return newFileName;

    }


    /**
     * match数据生成report
     * @param itemInfoMap
     * @param propertyMap
     */
    private Map<String,Object> dealInfoData(Map<String,Object>  itemInfoMap, Map<String,Object>  propertyMap) {

        Map<String,Object> allNameValueMap =new HashMap<String,Object>();

        /**
         * 1.外层为商品主要属性信息
         * 2.内层为商品各属性列表信息
         * 调用策略中心处理数据并返回
         */
        // 键和值
        String key = null;
        HawSrapySkuInfoDO value = null;
        Iterator itemInfoMapIte = itemInfoMap.entrySet().iterator();
        while (itemInfoMapIte.hasNext()) {
            Map.Entry entry = (Map.Entry) itemInfoMapIte.next();
            key = (String) entry.getKey();
            value = (HawSrapySkuInfoDO) entry.getValue();
            // 产品主要属性处理
            itemInfoDeal(key,value,allNameValueMap);

            String proKey=null;
            Map <String,Object> singlePropertyMap=null;
            Iterator propertyMapIte = propertyMap.entrySet().iterator();
            while (propertyMapIte.hasNext()) {
                Map.Entry proEntry = (Map.Entry) propertyMapIte.next();
                proKey = (String) proEntry.getKey();
                singlePropertyMap = (LinkedHashMap<String, Object>) proEntry.getValue();
                // 商品附属属性处理
                itemPropertyInfoDeal(proKey,singlePropertyMap,allNameValueMap);

            }

        }

        return allNameValueMap;
    }

    private void itemInfoDeal(String key,HawSrapySkuInfoDO valueObj,Map<String,Object> allNameValueMap) {
        Map<String,Object> singleIdMap;
        if (ObjectUtil.isNotEmpty(allNameValueMap.get(key))) {
            singleIdMap= (HashMap<String, Object>) allNameValueMap.get(key);
        } else {
            singleIdMap= new HashMap<String, Object>();
            allNameValueMap.put(key,singleIdMap);
        }

        // 产品标题
        templateDealDataForHawHandler.templateDataDeal(TemplateTypeEnum.HAW_FILE_TEMPLATE, TemplateHawTransNameCons.PRODUCT_TITLE,valueObj.getProductTitle(),singleIdMap);

        // 产品简介
        templateDealDataForHawHandler.templateDataDeal(TemplateTypeEnum.HAW_FILE_TEMPLATE, TemplateHawTransNameCons.PRODUCT_INTRODUCE,valueObj.getProductIntroduce(),singleIdMap);

        // MAN_CAUTION_STATE
        templateDealDataForHawHandler.templateDataDeal(TemplateTypeEnum.HAW_FILE_TEMPLATE, TemplateHawTransNameCons.MAN_CAUTION_STATE,"",singleIdMap);

        // DANGER_GOODS_REGULER
        templateDealDataForHawHandler.templateDataDeal(TemplateTypeEnum.HAW_FILE_TEMPLATE, TemplateHawTransNameCons.DANGER_GOODS_REGULER,"",singleIdMap);

        // PRODUCT_COMP_CERT
        templateDealDataForHawHandler.templateDataDeal(TemplateTypeEnum.HAW_FILE_TEMPLATE, TemplateHawTransNameCons.PRODUCT_COMP_CERT,"",singleIdMap);

        // Country of Origin
        templateDealDataForHawHandler.templateDataDeal(TemplateTypeEnum.HAW_FILE_TEMPLATE, TemplateHawTransNameCons.COUNTRY_OF_ORIGIN,"",singleIdMap);

        // Contains Liquid Contents?
        templateDealDataForHawHandler.templateDataDeal(TemplateTypeEnum.HAW_FILE_TEMPLATE, TemplateHawTransNameCons.CONTAINS_LIQUID_CONTENTS,valueObj.getProductTitle(),singleIdMap);

        // Product Brands
        templateDealDataForHawHandler.templateDataDeal(TemplateTypeEnum.HAW_FILE_TEMPLATE, TemplateHawTransNameCons.PRODUCT_BRANDS,valueObj.getProductBrands(),singleIdMap);

        // COLOR
        templateDealDataForHawHandler.templateDataDeal(TemplateTypeEnum.HAW_FILE_TEMPLATE, TemplateHawTransNameCons.COLOR,"",singleIdMap);

        // IN_PACK_PER_MAST_PACK
        templateDealDataForHawHandler.templateDataDeal(TemplateTypeEnum.HAW_FILE_TEMPLATE, TemplateHawTransNameCons.IN_PACK_PER_MAST_PACK,"",singleIdMap);

        // NUMBER_OF_BOXES
        templateDealDataForHawHandler.templateDataDeal(TemplateTypeEnum.HAW_FILE_TEMPLATE, TemplateHawTransNameCons.NUMBER_OF_BOXES,"",singleIdMap);

        // POWER_SOURCE
        templateDealDataForHawHandler.templateDataDeal(TemplateTypeEnum.HAW_FILE_TEMPLATE, TemplateHawTransNameCons.POWER_SOURCE,"",singleIdMap);

        // ITEMS_PER_INNER_PACK
        templateDealDataForHawHandler.templateDataDeal(TemplateTypeEnum.HAW_FILE_TEMPLATE, TemplateHawTransNameCons.ITEMS_PER_INNER_PACK,"",singleIdMap);


        return;
    }

    private void itemPropertyInfoDeal(String key,Map <String,Object> singlePropertyMap,Map<String,Object> allNameValueMap) {
        Map<String,Object> singleIdMap;
        if (ObjectUtil.isNotEmpty(allNameValueMap.get(key))) {
            singleIdMap= (HashMap<String, Object>) allNameValueMap.get(key);
        } else {
            singleIdMap= new HashMap<String, Object>();
            allNameValueMap.put(key,singleIdMap);
        }

        String entryKey=null;
        Object entryValue=null;
        Iterator singlePropertyMapIte = singlePropertyMap.entrySet().iterator();
        while (singlePropertyMapIte.hasNext()) {
            Map.Entry entry = (Map.Entry) singlePropertyMapIte.next();
            entryKey = (String) entry.getKey();
            entryValue =  entry.getValue();
            // 以propertyName查找策略匹配组装
            templateDealDataForHawHandler.templateDataDeal(TemplateTypeEnum.HAW_FILE_TEMPLATE, entryKey,entryValue,singleIdMap);
        }

        return;
    }

    /**
     * 单元格设值
     * @param cell
     * @param columnName
     * @param value
     */
    private void setCellValueByColumnName(Cell cell , String columnName , Object value) {
        if (ObjectUtil.isNotEmpty(value) && ObjectUtil.isNotEmpty(cell)) {
            if (StrUtil.equalsAnyIgnoreCase(columnName, TemplateHawNameCons.COST_PRICE_COMLUMN,TemplateHawNameCons.IN_PACK_PER_MAST_PACK_COMLUMN,
                    TemplateHawNameCons.ITEM_LENGTH_COMLUMN,TemplateHawNameCons.ITEM_WIDTH_COMLUMN,TemplateHawNameCons.ITEM_HEIGHT_COMLUMN,
                    TemplateHawNameCons.ITEM_PACKAGE_HEIGHT_COMLUMN,TemplateHawNameCons.ITEM_PACKAGE_LENGTH_COMLUMN,TemplateHawNameCons.ITEM_PACKAGE_WIDTH_COMLUMN,
                    TemplateHawNameCons.PACKAGE_WEIGHT_COMLUMN,TemplateHawNameCons.NUMBER_OF_BOXES_COMLUMN
                    )) {
                cell.setCellValue(new BigDecimal(String.valueOf(value)).doubleValue());
            } else {
                cell.setCellValue(String.valueOf(value));
            }
        } else {
            return;
        }
    }

    public static void main(String [] args) {

//        String str="1|2|3|";
//        System.out.println(str.split("\\|").length);

    }

}
