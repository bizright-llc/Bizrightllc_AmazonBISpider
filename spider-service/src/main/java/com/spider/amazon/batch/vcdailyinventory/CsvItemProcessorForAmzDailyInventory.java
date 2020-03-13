package com.spider.amazon.batch.vcdailyinventory;

import com.spider.amazon.entity.AmzVcDailyInventory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.validator.ValidatingItemProcessor;

import javax.validation.ValidationException;

/**
 * @description
 * CSV文件数据处理及校验
 * 只需要实现ItemProcessor接口，重写process方法，输入的参数是从ItemReader读取到的数据，返回的数据给ItemWriter
 */
@Slf4j
public class CsvItemProcessorForAmzDailyInventory extends ValidatingItemProcessor<AmzVcDailyInventory> {

    @Override
    public AmzVcDailyInventory process(AmzVcDailyInventory item) throws ValidationException {
        // 执行super.process()才能调用自定义的校验器
        log.info("processor start validating...");
        super.process(item);

        // availableInventory与sellableOnHandUnits一样
        item.setAvailableInventory(item.getSellableOnHandUnits());


        log.info("processor end validating...");
        return item;
    }
}