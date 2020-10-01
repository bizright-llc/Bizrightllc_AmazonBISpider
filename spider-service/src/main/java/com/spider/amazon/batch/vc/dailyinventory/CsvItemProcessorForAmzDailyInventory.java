package com.spider.amazon.batch.vc.dailyinventory;

import cn.hutool.core.date.DateUtil;
import com.spider.amazon.cons.DateFormat;
import com.spider.amazon.entity.AmzVcDailyInventory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.validator.ValidatingItemProcessor;

import javax.validation.ValidationException;
import java.util.HashMap;
import java.util.Map;

/**
 * @description
 * CSV文件数据处理及校验
 * 只需要实现ItemProcessor接口，重写process方法，输入的参数是从ItemReader读取到的数据，返回的数据给ItemWriter
 */
@Slf4j
public class CsvItemProcessorForAmzDailyInventory extends ValidatingItemProcessor<AmzVcDailyInventory> {

    private Map<String, Object> paramMaps;

    public CsvItemProcessorForAmzDailyInventory(Map<String, Object> paramMaps) {
        this.paramMaps = paramMaps;
    }

    @Override
    public AmzVcDailyInventory process(AmzVcDailyInventory item) throws ValidationException {
        // 执行super.process()才能调用自定义的校验器
//        log.info("processor start validating...");
        super.process(item);

        String distributeView = paramMaps.getOrDefault("distributeView", "").toString();
        String reportingRange = paramMaps.getOrDefault("reportingRange", "").toString();
        String viewingDate = paramMaps.get("viewingDate").toString();
        String viewingDateEnd = paramMaps.get("viewingDateEnd").toString();

        viewingDate = DateUtil.format(DateUtil.parse(viewingDate, DateFormat.YEAR_MONTH_DAY_MMddyy1), DateFormat.YEAR_MONTH_DAY_MMddyyyy);
        viewingDateEnd = DateUtil.format(DateUtil.parse(viewingDateEnd, DateFormat.YEAR_MONTH_DAY_MMddyy1), DateFormat.YEAR_MONTH_DAY_MMddyyyy);

        // availableInventory与sellableOnHandUnits一样
        item.setAvailableInventory(item.getSellableOnHandUnits());

        item.setDistributorView(distributeView);

        item.setReportingRange(reportingRange);
        item.setViewingDate(viewingDate);
        item.setViewingDateEnd(viewingDateEnd);

//        log.info("processor end validating...");
        return item;
    }
}