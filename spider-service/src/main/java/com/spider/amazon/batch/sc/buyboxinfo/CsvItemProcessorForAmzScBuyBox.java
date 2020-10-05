package com.spider.amazon.batch.sc.buyboxinfo;

import cn.hutool.core.date.DateUtil;
import com.spider.amazon.cons.DateFormat;
import com.spider.amazon.entity.AmzScBuyBox;
import com.spider.amazon.utils.ConvertUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.validator.ValidatingItemProcessor;

import javax.validation.ValidationException;
import java.util.Date;
import java.util.Map;

/**
 * @description
 * CSV文件数据处理及校验
 * 只需要实现ItemProcessor接口，重写process方法，输入的参数是从ItemReader读取到的数据，返回的数据给ItemWriter
 */
@Slf4j
public class CsvItemProcessorForAmzScBuyBox extends ValidatingItemProcessor<AmzScBuyBox> {

    private Map<String, Object> paramMaps;

    public CsvItemProcessorForAmzScBuyBox(Map<String, Object> paramMaps) {
        this.paramMaps = paramMaps;
    }

    @Override
    public AmzScBuyBox process(AmzScBuyBox item) throws ValidationException {
        // 执行super.process()才能调用自定义的校验器
//        log.info("processor start validating...");
        super.process(item);

        String viewingDate = paramMaps.getOrDefault(CsvBatchConfigForAmzScBuyBox.PARAM_MAPS_VIEWING_DATE, "").toString();
        Date date = DateUtil.parse(viewingDate, DateFormat.YEAR_MONTH_DAY);

        item.setFromDate(viewingDate);
        item.setToDate(viewingDate);

        item.setSessionsNum(Math.round(ConvertUtils.convertNumberStrToFloat(item.getSessions())));
        item.setSessionPercentageNum(ConvertUtils.convertNumberStrToFloat(item.getSessionPercentage()));
        item.setPageViewsNum(Math.round(ConvertUtils.convertNumberStrToFloat(item.getPageViews())));
        item.setPageViewsPercentageNum(ConvertUtils.convertNumberStrToFloat(item.getPageViewsPercentage()));
        item.setBuyBoxPercentageNum(ConvertUtils.convertNumberStrToFloat(item.getBuyBoxPercentage()));
        item.setUnitsOrderedNum(Math.round(ConvertUtils.convertNumberStrToFloat(item.getUnitsOrdered())));
        item.setUnitsOrderedB2BNum(Math.round(ConvertUtils.convertNumberStrToFloat(item.getUnitsOrderedB2B())));
        item.setUnitSessionPercentageNum(ConvertUtils.convertNumberStrToFloat(item.getUnitSessionPercentage()));
        item.setUnitSessionPercentageB2BNum(ConvertUtils.convertNumberStrToFloat(item.getUnitSessionPercentageB2B()));
        item.setOrderedProductSalesNum(ConvertUtils.convertNumberStrToFloat(item.getOrderedProductSales()));
        item.setOrderedProductSalesB2BNum(ConvertUtils.convertNumberStrToFloat(item.getOrderedProductSalesB2B()));
        item.setTotalOrderItemsNum(Math.round(ConvertUtils.convertNumberStrToFloat(item.getTotalOrderItems())));
        item.setTotalOrderItemsB2BNum(Math.round(ConvertUtils.convertNumberStrToFloat(item.getTotalOrderItemsB2B())));

//        log.info("processor end validating...");
        return item;
    }
}