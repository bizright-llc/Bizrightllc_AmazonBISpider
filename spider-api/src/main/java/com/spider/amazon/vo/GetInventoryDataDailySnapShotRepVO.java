package com.spider.amazon.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * @ClassName GetInventoryDataDailySnapShotRepVO
 * @Description 获取InventoryDataDailySnapShot信息请求返回实体
 */
@Builder(toBuilder = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GetInventoryDataDailySnapShotRepVO {
    private ResponseHeader responseHeader;
    private String code;
    private String message;
    private GetInventoryDataDailySnapShotRepResultVO result;
}
