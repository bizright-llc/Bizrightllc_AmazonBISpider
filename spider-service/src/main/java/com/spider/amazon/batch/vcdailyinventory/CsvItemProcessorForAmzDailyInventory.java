package com.spider.amazon.batch.vcdailyinventory;

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
        log.info("processor start validating...");
        super.process(item);

        String distributeView = paramMaps.get("distributeView").toString();
        String viewingDate = paramMaps.get("viewingDate").toString();

        // availableInventory与sellableOnHandUnits一样
        item.setAvailableInventory(item.getSellableOnHandUnits());

        item.setDistributeView(distributeView);

        item.setViewingDate(viewingDate);

        log.info("processor end validating...");
        return item;
    }
}