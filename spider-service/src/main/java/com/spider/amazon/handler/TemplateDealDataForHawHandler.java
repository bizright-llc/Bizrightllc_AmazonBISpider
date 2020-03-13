package com.spider.amazon.handler;


import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.spider.amazon.cons.TemplateHawNameCons;
import com.spider.amazon.cons.TemplateHawTransNameCons;
import com.spider.amazon.cons.TemplateTypeEnum;
import com.spider.amazon.cusinterface.TemplateDealDataType;
import com.spider.amazon.handler.abs.AbstractTemplateDealDataHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Slf4j
@Component
@TemplateDealDataType(TemplateTypeEnum.HAW_FILE_TEMPLATE)
public class TemplateDealDataForHawHandler extends AbstractTemplateDealDataHandler {

    private final static int MIN_BULLET_POINT_NUM=3;
    private final static String PRODUCT_COMP_CERT_DEFAULT="Not Applicable";
    private final static String MAN_CAUTION_STATE_DEFAULT="No Warning Applicable";
    private final static String DANGER_GOODS_REGULER_DEFAULT="Not Applicable";
    private final static String INCHES="in";
    private final static String INCHES_UNIT="Inches";
    private final static String POUNDS="lb";
    private final static String POUNDS_UNIT="Pounds";
    private final static String COUNTRY_OF_ORIGIN_DEFAULT="United States";
    private final static String COLOR_DEFAULT="natural";
    private final static String IN_PACK_PER_DEFAULT="1";
    private final static String NUMBER_OF_BOXES_DEFAULT="1";
    private final static String POWER_SOURCE_DEFAULT="Not Applicable";
    private final static String ITEMS_PER_INNER_PACK_DEFAULT="1";
    private final static String NUMBER_OF_ITEMS_DEFAULT="1";
    private final static String IS_ASSEMBLY_REQUIRED_DEFAULT="No";

    /**
     * Haw文件模版数据处理返回
     * @param typeEnum
     * @param name
     * @param value
     * @return
     */
    @Override
    public Map<String, Object> templateDataDeal(TemplateTypeEnum typeEnum, String name, Object value,Map<String,Object> concludeMap) {
        Map<String,Object> resultMap=new HashMap<>();

        switch (name) {
            case TemplateHawTransNameCons.PRODUCT_TITLE: // title字段
                dealProductTitle(name,value,resultMap,concludeMap);
                break;
            case TemplateHawTransNameCons.PRODUCT_INTRODUCE: // 简介字段
                dealProductIntroduce(name,value,resultMap,concludeMap);
                break;
            case TemplateHawTransNameCons.PRODUCT_COMP_CERT: // PRODUCT COMP CERT
                dealProductCompCert(name,value,resultMap,concludeMap);
                break;
            case TemplateHawTransNameCons.MAN_CAUTION_STATE: // MAN CAUTION STATE
                dealManCautionState(name,value,resultMap,concludeMap);
                break;
            case TemplateHawTransNameCons.DANGER_GOODS_REGULER: // DANGER GOODS REGULER
                dealDangerGoodsReguler(name,value,resultMap,concludeMap);
                break;
            case TemplateHawTransNameCons.LENGTH: // LENGTH
                dealItemLength(name,value,resultMap,concludeMap);
                break;
            case TemplateHawTransNameCons.WIDTH: // WIDTH
                dealItemWidth(name,value,resultMap,concludeMap);
                break;
            case TemplateHawTransNameCons.HEIGHT: // HEIGHT
                dealItemHeight(name,value,resultMap,concludeMap);
                break;
            case TemplateHawTransNameCons.COST_PRICE: // COST PRICE
                dealItemCostPrice(name,value,resultMap,concludeMap);
                break;
            case TemplateHawTransNameCons.VENDOR_CODE: // VENDOR CODE
                dealItemVendorCode(name,value,resultMap,concludeMap);
                break;
            case TemplateHawTransNameCons.VENDOR_SKU: // VENDOR SKU
                dealItemVendorSku(name,value,resultMap,concludeMap);
                break;
            case TemplateHawTransNameCons.COUNTRY_OF_ORIGIN: // Country of Origin
                dealItemCountryOfOrigin(name,value,resultMap,concludeMap);
                break;
            case TemplateHawTransNameCons.CONTAINS_LIQUID_CONTENTS: // Country of Origin
                dealItemContainsLiquidContents(name,value,resultMap,concludeMap);
                break;
            case TemplateHawTransNameCons.COLOR: // COLOR
                dealItemColor(name,value,resultMap,concludeMap);
                break;
            case TemplateHawTransNameCons.PRODUCT_BRANDS: // PRODUCT BRANDS
                dealItemProductBrands(name,value,resultMap,concludeMap);
                break;
            case TemplateHawTransNameCons.IN_PACK_PER_MAST_PACK: // Inner Packs Per Master Pack
                dealInPackPer(name,value,resultMap,concludeMap);
                break;
            case TemplateHawTransNameCons.WEIGHT: // WEIGHT
                dealItemWeight(name,value,resultMap,concludeMap);
                break;
            case TemplateHawTransNameCons.NUMBER_OF_BOXES: // NUMBER OF BOXES
                dealNumberOfBoxes(name,value,resultMap,concludeMap);
                break;
            case TemplateHawTransNameCons.POWER_SOURCE: // POWER SOURCE
                dealPowerSource(name,value,resultMap,concludeMap);
                break;
            case TemplateHawTransNameCons.ITEMS_PER_INNER_PACK: // ITEMS PER INNER PACK
                dealItemsPerInnerPack(name,value,resultMap,concludeMap);
                break;
            case TemplateHawTransNameCons.LIST_PRICE: // LIST PRICE
                dealItemListPrice(name,value,resultMap,concludeMap);
                break;
            case TemplateHawTransNameCons.NUMBER_OF_ITEMS: // NUMBER OF ITEMS
                dealNubmerOfItems(name,value,resultMap,concludeMap);
                break;
            case TemplateHawTransNameCons.IS_ASSEMBLY_REQUIRED: // IS ASSEMBLY REQUIRED
                dealIsAssemblyRequired(name,value,resultMap,concludeMap);
                break;
            default:

        }

        return resultMap;
    }


    /**
     * 文件标题，切割成Item Type Name和Model Name两个列
     * @param name
     * @param value
     * @param resultMap
     */
    private void dealProductTitle(String name, Object value ,Map<String,Object> resultMap,Map<String,Object> concludeMap) {
        List <Object> resultList1=new ArrayList();
        resultList1.add(StrUtil.subPre(String.valueOf(value),3));
        List <Object> resultList2=new ArrayList();
        resultList2.add(StrUtil.subSuf(String.valueOf(value),3));
        // itemTypeName
        resultMap.put(TemplateHawNameCons.ITEM_NAME_MAP_COMLUMN,resultList1);
        concludeMap.put(TemplateHawNameCons.ITEM_NAME_MAP_COMLUMN,resultList1);
        // modelName
        resultMap.put(TemplateHawNameCons.MODEL_NAME_MAP_COMLUMN,resultList2);
        concludeMap.put(TemplateHawNameCons.MODEL_NAME_MAP_COMLUMN,resultList2);
        return;
    }

    /**
     * 文件简介，转换成Product Description，Bullet Point两个列
     * @param name
     * @param value
     * @param resultMap
     */
    private void dealProductIntroduce(String name, Object value ,Map<String,Object> resultMap,Map<String,Object> concludeMap) {
        // Product Description
        List <Object> resultList1=new ArrayList();
        resultList1.add(String.valueOf(value));
        resultMap.put(TemplateHawNameCons.PRODUCT_DESC_MAP_COMLUMN,resultList1);
        concludeMap.put(TemplateHawNameCons.PRODUCT_DESC_MAP_COMLUMN,resultList1);
        // Bullet Point
        /**
         * 切分字段值，分割卖点，然后进行特殊规则补充
         * 前三个BulletPoint必须有值
         */
        List<Object> valueList=new ArrayList<>();
        // TODO 自动摘要算法待确定
        String [] bPoint = String.valueOf(value).split("\\.");
        if (bPoint.length<MIN_BULLET_POINT_NUM) {
            String bPointStr="";
            for(int bPointIndex=0;bPointIndex<MIN_BULLET_POINT_NUM;bPointIndex++) {
                if (bPointIndex<bPoint.length) {
                    valueList.add(bPoint[bPointIndex]);
                    bPointStr=bPoint[bPointIndex];
                } else {
                    valueList.add(bPointStr);
                }

            }
        } else {
            for(int bPointIndex=0;bPointIndex<bPoint.length;bPointIndex++) {
                valueList.add(bPoint[bPointIndex]);
            }
        }
        resultMap.put(TemplateHawNameCons.B_POINT_MAP_COMLUMN,valueList);
        concludeMap.put(TemplateHawNameCons.B_POINT_MAP_COMLUMN,valueList);

        return;
    }

    /**
     * Product Compliance Certificate 默认设值
     * @param name
     * @param value
     * @param resultMap
     */
    private void dealProductCompCert(String name, Object value ,Map<String,Object> resultMap,Map<String,Object> concludeMap) {
        List <Object> resultList=new ArrayList();
        resultList.add(PRODUCT_COMP_CERT_DEFAULT);
        // Product Compliance Certificate
        resultMap.put(TemplateHawNameCons.PRODUCT_COMP_CERT_COMLUMN,resultList);
        concludeMap.put(TemplateHawNameCons.PRODUCT_COMP_CERT_COMLUMN,resultList);
        return;
    }

    /**
     * Mandatory Cautionary Statement 默认设值
     * @param name
     * @param value
     * @param resultMap
     */
    private void dealManCautionState(String name, Object value ,Map<String,Object> resultMap,Map<String,Object> concludeMap) {
        List <Object> resultList=new ArrayList();
        resultList.add(MAN_CAUTION_STATE_DEFAULT);
        // Mandatory Cautionary Statement
        resultMap.put(TemplateHawNameCons.MAN_CAUTION_STATE_COMLUMN,resultList);
        concludeMap.put(TemplateHawNameCons.MAN_CAUTION_STATE_COMLUMN,resultList);
        return;
    }

    /**
     * Dangerous Goods Regulations 默认设值
     * @param name
     * @param value
     * @param resultMap
     */
    private void dealDangerGoodsReguler(String name, Object value ,Map<String,Object> resultMap,Map<String,Object> concludeMap) {
        List <Object> resultList=new ArrayList();
        resultList.add(DANGER_GOODS_REGULER_DEFAULT);
        // Dangerous Goods Regulations
        resultMap.put(TemplateHawNameCons.DANGER_GOODS_REGULER_COMLUMN,resultList);
        concludeMap.put(TemplateHawNameCons.DANGER_GOODS_REGULER_COMLUMN,resultList);
        return;
    }

    /**
     * Length (in): 拆分成 Item Length 和 Item Length Unit ，Item Package Length和Package Length Unit
     * @param name
     * @param value
     * @param resultMap
     */
    private void dealItemLength(String name, Object value ,Map<String,Object> resultMap,Map<String,Object> concludeMap) {
        // Item Length，Item Package Length
        List <Object> resultList1=new ArrayList();
        resultList1.add(value);
        resultMap.put(TemplateHawNameCons.ITEM_LENGTH_COMLUMN,resultList1);
        concludeMap.put(TemplateHawNameCons.ITEM_LENGTH_COMLUMN,resultList1);
        resultMap.put(TemplateHawNameCons.ITEM_PACKAGE_LENGTH_COMLUMN,resultList1);
        concludeMap.put(TemplateHawNameCons.ITEM_PACKAGE_LENGTH_COMLUMN,resultList1);
        // Item Length Unit，Package Length Unit
        List <Object> resultList2=new ArrayList();
        resultList2.add(StrUtil.subBetween(name,"(",")").equals(INCHES)?INCHES_UNIT:"");
        resultMap.put(TemplateHawNameCons.ITEM_LENGTH_UNIT_COMLUMN,resultList2);
        concludeMap.put(TemplateHawNameCons.ITEM_LENGTH_UNIT_COMLUMN,resultList2);
        resultMap.put(TemplateHawNameCons.PACKAGE_LENGTH_UNIT_COMLUMN,resultList2);
        concludeMap.put(TemplateHawNameCons.PACKAGE_LENGTH_UNIT_COMLUMN,resultList2);
        return;
    }

    /**
     * Width (in): 拆分成 Item Width 和 Item Width Unit,Item Package Width和Package Width Unit
     * @param name
     * @param value
     * @param resultMap
     */
    private void dealItemWidth(String name, Object value ,Map<String,Object> resultMap,Map<String,Object> concludeMap) {
        // Item Width,Item Package Width
        List <Object> resultList1=new ArrayList();
        resultList1.add(value);
        resultMap.put(TemplateHawNameCons.ITEM_WIDTH_COMLUMN,resultList1);
        concludeMap.put(TemplateHawNameCons.ITEM_WIDTH_COMLUMN,resultList1);
        resultMap.put(TemplateHawNameCons.ITEM_PACKAGE_WIDTH_COMLUMN,resultList1);
        concludeMap.put(TemplateHawNameCons.ITEM_PACKAGE_WIDTH_COMLUMN,resultList1);
        // Item Width Unit，Package Width Unit
        List <Object> resultList2=new ArrayList();
        resultList2.add(StrUtil.subBetween(name,"(",")").equals(INCHES)?INCHES_UNIT:"");
        resultMap.put(TemplateHawNameCons.ITEM_WIDTH_UNIT_COMLUMN,resultList2);
        concludeMap.put(TemplateHawNameCons.ITEM_WIDTH_UNIT_COMLUMN,resultList2);
        resultMap.put(TemplateHawNameCons.PACKAGE_WIDTH_UNIT_COMLUMN,resultList2);
        concludeMap.put(TemplateHawNameCons.PACKAGE_WIDTH_UNIT_COMLUMN,resultList2);
        return;
    }

    /**
     * Height (in): 拆分成 Item Height 和 Item Height Unit，Item Package Height和Package Height Unit
     * @param name
     * @param value
     * @param resultMap
     */
    private void dealItemHeight(String name, Object value ,Map<String,Object> resultMap,Map<String,Object> concludeMap) {
        // Item Height,Item Package Height
        List <Object> resultList1=new ArrayList();
        resultList1.add(value);
        resultMap.put(TemplateHawNameCons.ITEM_HEIGHT_COMLUMN,resultList1);
        concludeMap.put(TemplateHawNameCons.ITEM_HEIGHT_COMLUMN,resultList1);
        resultMap.put(TemplateHawNameCons.ITEM_PACKAGE_HEIGHT_COMLUMN,resultList1);
        concludeMap.put(TemplateHawNameCons.ITEM_PACKAGE_HEIGHT_COMLUMN,resultList1);
        // Item Height Unit,Package Height Unit
        List <Object> resultList2=new ArrayList();
        resultList2.add(StrUtil.subBetween(name,"(",")").equals(INCHES)?INCHES_UNIT:"");
        resultMap.put(TemplateHawNameCons.ITEM_HEIGHT_UNIT_COMLUMN,resultList2);
        concludeMap.put(TemplateHawNameCons.ITEM_HEIGHT_UNIT_COMLUMN,resultList2);
        resultMap.put(TemplateHawNameCons.PACKAGE_HEIGHT_UNIT_COMLUMN,resultList2);
        concludeMap.put(TemplateHawNameCons.PACKAGE_HEIGHT_UNIT_COMLUMN,resultList2);
        return;
    }

    /**
     * COST PRICE
     * @param name
     * @param value
     * @param resultMap
     */
    private void dealItemCostPrice(String name, Object value ,Map<String,Object> resultMap,Map<String,Object> concludeMap) {
        // COST PRICE
        List <Object> resultList=new ArrayList();
        resultList.add(value);
        resultMap.put(TemplateHawNameCons.COST_PRICE_COMLUMN,resultList);
        concludeMap.put(TemplateHawNameCons.COST_PRICE_COMLUMN,resultList);
        return;
    }

    /**
     * VendorCode
     * @param name
     * @param value
     * @param resultMap
     */
    private void dealItemVendorCode(String name, Object value ,Map<String,Object> resultMap,Map<String,Object> concludeMap) {
        // VendorCode
        List <Object> resultList=new ArrayList();
        resultList.add(value);
        resultMap.put(TemplateHawNameCons.VENDOR_CODE_COMLUMN,resultList);
        concludeMap.put(TemplateHawNameCons.VENDOR_CODE_COMLUMN,resultList);
        return;
    }

    /**
     * VendorSku
     * @param name
     * @param value
     * @param resultMap
     */
    private void dealItemVendorSku(String name, Object value ,Map<String,Object> resultMap,Map<String,Object> concludeMap) {
        // VendorSku
        List <Object> resultList=new ArrayList();
        resultList.add(value);
        resultMap.put(TemplateHawNameCons.VENDOR_SKU_COMLUMN,resultList);
        concludeMap.put(TemplateHawNameCons.VENDOR_SKU_COMLUMN,resultList);
        //Model Number
        List <Object> resultList1=new ArrayList();
        resultList1.add(value);
        resultMap.put(TemplateHawNameCons.MODEL_NUMBER_COMLUMN,resultList1);
        concludeMap.put(TemplateHawNameCons.MODEL_NUMBER_COMLUMN,resultList1);
        //Model Number
        List <Object> resultList2=new ArrayList();
        resultList2.add(StrUtil.subSuf((CharSequence) value,4));
        resultMap.put(TemplateHawNameCons.PART_NUMBER_COMLUMN,resultList2);
        concludeMap.put(TemplateHawNameCons.PART_NUMBER_COMLUMN,resultList2);
        return;
    }

    /**
     * Country of Origin
     * @param name
     * @param value
     * @param resultMap
     */
    private void dealItemCountryOfOrigin(String name, Object value ,Map<String,Object> resultMap,Map<String,Object> concludeMap) {
        // Country of Origin
        List <Object> resultList=new ArrayList();
        resultList.add(COUNTRY_OF_ORIGIN_DEFAULT);
        resultMap.put(TemplateHawNameCons.COUNTRY_OF_ORIGIN_COMLUMN,resultList);
        concludeMap.put(TemplateHawNameCons.COUNTRY_OF_ORIGIN_COMLUMN,resultList);
        return;
    }

    /**
     * Contains Liquid Contents?
     * @param name
     * @param value
     * @param resultMap
     */
    private void dealItemContainsLiquidContents(String name, Object value ,Map<String,Object> resultMap,Map<String,Object> concludeMap) {
        // Contains Liquid Contents?
        List <Object> resultList=new ArrayList();
        if (StrUtil.containsAnyIgnoreCase((CharSequence) value,"Liquid","Quart","Gallon")) {
            resultList.add("Yes");
        } else {
            resultList.add("No");
        }
        resultMap.put(TemplateHawNameCons.CONTAINS_LIQUID_CONTENTS_COMLUMN,resultList);
        concludeMap.put(TemplateHawNameCons.CONTAINS_LIQUID_CONTENTS_COMLUMN,resultList);
        return;
    }

    /**
     * Color
     * @param name
     * @param value
     * @param resultMap
     */
    private void dealItemColor(String name, Object value ,Map<String,Object> resultMap,Map<String,Object> concludeMap) {
        // Color
        List <Object> resultList=new ArrayList();
        resultList.add(COLOR_DEFAULT);
        resultMap.put(TemplateHawNameCons.COLOR_COMLUMN,resultList);
        concludeMap.put(TemplateHawNameCons.COLOR_COMLUMN,resultList);
        return;
    }

    /**
     * Product Brands
     * @param name
     * @param value
     * @param resultMap
     */
    private void dealItemProductBrands(String name, Object value ,Map<String,Object> resultMap,Map<String,Object> concludeMap) {
        // Manufacturer
        List <Object> resultList=new ArrayList();
        resultList.add(value);
        resultMap.put(TemplateHawNameCons.MANUFACTURER_COMLUMN,resultList);
        concludeMap.put(TemplateHawNameCons.MANUFACTURER_COMLUMN,resultList);
        // BRAND_NAME_COMLUMN
        List <Object> resultList2=new ArrayList();
        resultList2.add(value);
        resultMap.put(TemplateHawNameCons.BRAND_NAME_COMLUMN,resultList2);
        concludeMap.put(TemplateHawNameCons.BRAND_NAME_COMLUMN,resultList2);
        return;
    }

    /**
     * Inner Packs Per Master Pack
     * @param name
     * @param value
     * @param resultMap
     */
    private void dealInPackPer(String name, Object value ,Map<String,Object> resultMap,Map<String,Object> concludeMap) {
        // Inner Packs Per Master Pack
        List <Object> resultList=new ArrayList();
        resultList.add(IN_PACK_PER_DEFAULT);
        resultMap.put(TemplateHawNameCons.IN_PACK_PER_MAST_PACK_COMLUMN,resultList);
        concludeMap.put(TemplateHawNameCons.IN_PACK_PER_MAST_PACK_COMLUMN,resultList);
        return;
    }

    /**
     * Weight (lb): 拆分成 Package Weight 和 Package Weight Unit
     * @param name
     * @param value
     * @param resultMap
     */
    private void dealItemWeight(String name, Object value ,Map<String,Object> resultMap,Map<String,Object> concludeMap) {
        // Package Weight
        List <Object> resultList1=new ArrayList();
        resultList1.add(value);
        resultMap.put(TemplateHawNameCons.PACKAGE_WEIGHT_COMLUMN,resultList1);
        concludeMap.put(TemplateHawNameCons.PACKAGE_WEIGHT_COMLUMN,resultList1);
        // Package Weight Unit
        List <Object> resultList2=new ArrayList();
        resultList2.add(StrUtil.subBetween(name,"(",")").equals(POUNDS)?POUNDS_UNIT:"");
        resultMap.put(TemplateHawNameCons.PACKAGE_WEIGHT_UNIT_COMLUMN,resultList2);
        concludeMap.put(TemplateHawNameCons.PACKAGE_WEIGHT_UNIT_COMLUMN,resultList2);
        return;
    }

    /**
     * Power Source
     * @param name
     * @param value
     * @param resultMap
     */
    private void dealPowerSource(String name, Object value ,Map<String,Object> resultMap,Map<String,Object> concludeMap) {
        // Power Source
        List <Object> resultList1=new ArrayList();
        resultList1.add(POWER_SOURCE_DEFAULT);
        resultMap.put(TemplateHawNameCons.POWER_SOURCE_COMLUMN,resultList1);
        concludeMap.put(TemplateHawNameCons.POWER_SOURCE_COMLUMN,resultList1);
        return;
    }

    /**
     * Items per Inner Pack
     * @param name
     * @param value
     * @param resultMap
     */
    private void dealItemsPerInnerPack(String name, Object value ,Map<String,Object> resultMap,Map<String,Object> concludeMap) {
        // Items per Inner Pack
        List <Object> resultList1=new ArrayList();
        resultList1.add(ITEMS_PER_INNER_PACK_DEFAULT);
        resultMap.put(TemplateHawNameCons.ITEMS_PER_INNER_PACK_COMLUMN,resultList1);
        concludeMap.put(TemplateHawNameCons.ITEMS_PER_INNER_PACK_COMLUMN,resultList1);
        return;
    }

    /**
     * Number of Boxes
     * @param name
     * @param value
     * @param resultMap
     */
    private void dealNumberOfBoxes(String name, Object value ,Map<String,Object> resultMap,Map<String,Object> concludeMap) {
        // Number of Boxes
        List <Object> resultList=new ArrayList();
        resultList.add(NUMBER_OF_BOXES_DEFAULT);
        resultMap.put(TemplateHawNameCons.NUMBER_OF_BOXES_COMLUMN,resultList);
        concludeMap.put(TemplateHawNameCons.NUMBER_OF_BOXES_COMLUMN,resultList);
        return;
    }

    /**
     * LIST PRICE
     * @param name
     * @param value
     * @param resultMap
     */
    private void dealItemListPrice(String name, Object value ,Map<String,Object> resultMap,Map<String,Object> concludeMap) {
        // LIST PRICE
        List <Object> resultList=new ArrayList();
        resultList.add(value);
        resultMap.put(TemplateHawNameCons.LIST_PRICE_COMLUMN,resultList);
        concludeMap.put(TemplateHawNameCons.LIST_PRICE_COMLUMN,resultList);
        // COST PRICE
        List <Object> resultList2=new ArrayList();
        resultList2.add(ObjectUtil.isNotEmpty(value)?Double.valueOf(String.valueOf(value)):0);
        resultMap.put(TemplateHawNameCons.COST_PRICE_COMLUMN,resultList2);
        concludeMap.put(TemplateHawNameCons.COST_PRICE_COMLUMN,resultList2);
        return;
    }


    /**
     * Number of Items
     * @param name
     * @param value
     * @param resultMap
     */
    private void dealNubmerOfItems(String name, Object value ,Map<String,Object> resultMap,Map<String,Object> concludeMap) {
        // Number of Items
        List <Object> resultList=new ArrayList();
        resultList.add(NUMBER_OF_ITEMS_DEFAULT);
        resultMap.put(TemplateHawNameCons.NUMBER_OF_ITEMS_COMLUMN,resultList);
        concludeMap.put(TemplateHawNameCons.NUMBER_OF_ITEMS_COMLUMN,resultList);
        return;
    }

    /**
     * IS ASSEMBLY REQUIRED
     * @param name
     * @param value
     * @param resultMap
     */
    private void dealIsAssemblyRequired(String name, Object value ,Map<String,Object> resultMap,Map<String,Object> concludeMap) {
        // IS ASSEMBLY REQUIRED
        List <Object> resultList=new ArrayList();
        resultList.add(IS_ASSEMBLY_REQUIRED_DEFAULT);
        resultMap.put(TemplateHawNameCons.IS_ASSEMBLY_REQUIRED_COMLUMN,resultList);
        concludeMap.put(TemplateHawNameCons.IS_ASSEMBLY_REQUIRED_COMLUMN,resultList);
        return;
    }
}
