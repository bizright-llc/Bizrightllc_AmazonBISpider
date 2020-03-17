package com.config;

import com.spider.amazon.utils.CSVUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Configuration
@Slf4j
public class MyConfig {


    private Map<String, Object> paramMaps;

    private final static String filePath = "C:\\Users\\paulin.f\\Downloads\\Inventory Health_US-1.csv";

    @Bean
    public void bean1(){
        paramMaps = stepForAmzDailyInventoryPrepare(filePath);
        log.info ("paramMaps:"+paramMaps) ;
        return;
    }



    /**
     * 预先读取文件处理
     *
     * @return
     */
    public Map<String, Object> stepForAmzDailyInventoryPrepare(String filePath) {
        Map<String, Object> resultMap = new HashMap<>();
        // 读取文件第一行
        List<List<String>> csvRowList = CSVUtils.readCSVAdv(filePath, 0, 1, 9);
        // 获取报表维度及时间
        String reportingRange = csvRowList.get(0).get(5);
        String viewing = csvRowList.get(0).get(6);
        if (log.isInfoEnabled()) {
            log.info("reportingRange:" + reportingRange);
            log.info("viewing:" + viewing);
        }
        resultMap.put("reportingRange", reportingRange.substring(reportingRange.indexOf("[") + 1, reportingRange.indexOf("]")));
        resultMap.put("viewing", viewing.substring(viewing.indexOf("[") + 1, viewing.indexOf("]")));

        return resultMap;
    }


}
