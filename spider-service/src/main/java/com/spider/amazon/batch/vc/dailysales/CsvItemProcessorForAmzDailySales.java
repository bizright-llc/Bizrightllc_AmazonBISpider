package com.spider.amazon.batch.vc.dailysales;

import com.spider.amazon.entity.AmzVcDailySales;
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
public class CsvItemProcessorForAmzDailySales extends ValidatingItemProcessor<AmzVcDailySales> {

    private Map<String, Object> paramMaps;

    public CsvItemProcessorForAmzDailySales(Map<String, Object> paramMaps) {
        this.paramMaps = paramMaps;
    }

    @Override
    public AmzVcDailySales process(AmzVcDailySales item) throws ValidationException {
        super.process(item);

        String viewingDate = paramMaps.get("viewingDate").toString();
        String viewingDateEnd = paramMaps.get("viewingDateEnd").toString();
        String distributorView = paramMaps.get("distributorView").toString();
        String salesView = paramMaps.get("salesView").toString();
        String reportingRange = paramMaps.getOrDefault("reportingRange", "").toString();

        item.setReportingRange(reportingRange);
        item.setViewingDate(viewingDate);
        item.setViewingDateEnd(viewingDateEnd);
        item.setDistributorView(distributorView);
        item.setSalesView(salesView);

        return item;
    }
}