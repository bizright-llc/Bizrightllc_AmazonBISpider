package com.spider.amazon.batch.sc.buyboxinfo;

import cn.hutool.core.date.DateUtil;
import com.spider.amazon.cons.DateFormat;
import com.spider.amazon.entity.AmzScBuyBox;
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

//        log.info("processor end validating...");
        return item;
    }
}