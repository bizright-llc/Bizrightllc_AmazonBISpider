package com.spider.amazon.dto;

import lombok.Builder;
import lombok.Data;

@Builder(toBuilder = true)
@Data
/**
 * @ClassName GetInventoryDataDailySnapShotDTO
 * @Description BOP获取InventoryData信息请求实体
 */
public class GetInventoryDataDailySnapShotDTO {
    private int pageNo;
    private int pageSize;
    private String getType; // 获取当前，获取所有
}
