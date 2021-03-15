package com.spider.amazon.batch.sc.fba;

import cn.hutool.core.date.DateUtil;
import com.spider.amazon.cons.DateFormat;
import com.spider.amazon.entity.AmzScFbaFee;
import com.spider.amazon.utils.ConvertUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.validator.ValidatingItemProcessor;

import javax.validation.ValidationException;
import java.util.Map;

/**
 * @description
 * CSV文件数据处理及校验
 * 只需要实现ItemProcessor接口，重写process方法，输入的参数是从ItemReader读取到的数据，返回的数据给ItemWriter
 */
@Slf4j
public class CsvItemProcessorForAmzScFbaFee extends ValidatingItemProcessor<AmzScFbaFee> {

    private Map<String, Object> paramMaps;

    public CsvItemProcessorForAmzScFbaFee(Map<String, Object> paramMaps) {
        this.paramMaps = paramMaps;
    }

    @Override
    public AmzScFbaFee process(AmzScFbaFee item) throws ValidationException {
        // 执行super.process()才能调用自定义的校验器

        super.process(item);

        try{
            item.setTransactionDatetime(DateUtil.parse(item.getDatetime(),DateFormat.FBA_Transaction_DATETIME));
        }catch (Exception ex){
            log.error("Parse file datetime failed.");
        }

        item.setProductSalesNum(ConvertUtils.convertNumberStrToFloat(item.getProductSales()));
        item.setProductSalesTaxNum(ConvertUtils.convertNumberStrToFloat(item.getProductSales()));
        item.setShippingCreditsNum(ConvertUtils.convertNumberStrToFloat(item.getShippingCredits()));
        item.setShippingCreditsTaxNum(ConvertUtils.convertNumberStrToFloat(item.getShippingCreditsTax()));
        item.setGiftWrapCreditsNum(ConvertUtils.convertNumberStrToFloat(item.getGiftWrapCredits()));
        item.setGiftWrapCreditsTaxNum(ConvertUtils.convertNumberStrToFloat(item.getGiftWrapCreditsTax()));
        item.setPromotionalRebatesNum(ConvertUtils.convertNumberStrToFloat(item.getPromotionalRebates()));
        item.setPromotionalRebatesTaxNum(ConvertUtils.convertNumberStrToFloat(item.getPromotionalRebatesTax()));
        item.setMarketplaceWithheldTaxNum(ConvertUtils.convertNumberStrToFloat(item.getMarketplaceWithheldTax()));
        item.setSellingFeesNum(ConvertUtils.convertNumberStrToFloat(item.getSellingFees()));
        item.setFbaFeesNum(ConvertUtils.convertNumberStrToFloat(item.getFbaFees()));
        item.setOtherNum(ConvertUtils.convertNumberStrToFloat(item.getOther()));
        item.setTotalNum(ConvertUtils.convertNumberStrToFloat(item.getTotal()));

        return item;
    }
}