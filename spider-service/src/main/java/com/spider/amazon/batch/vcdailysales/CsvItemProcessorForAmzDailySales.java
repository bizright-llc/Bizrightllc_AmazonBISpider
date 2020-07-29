package com.spider.amazon.batch.vcdailysales;

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
        // 执行super.process()才能调用自定义的校验器
        log.info("processor start validating...");
        super.process(item);

        String viewingDate = paramMaps.get("viewingDate").toString();
        String distributorView = paramMaps.get("distributorView").toString();

        item.setViewingDate(viewingDate);
        item.setDistributorView(distributorView);

        log.info("processor end validating...");
        return item;
    }
}