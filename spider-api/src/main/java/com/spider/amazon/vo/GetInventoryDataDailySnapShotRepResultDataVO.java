package com.spider.amazon.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * @ClassName GetInventoryDataDailySnapShotRepResultDataVO
 * @Description InventoryDataDailySnapShot信息单条记录
 */
@Builder(toBuilder = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GetInventoryDataDailySnapShotRepResultDataVO {
    private String AVCInOpenOrderQty;
    private String EnterDate;
    private String InOpenOrderQty;
    private String InStockQty;
    private String ItemNum;
    private String LastUpdate;
    private String SnapShotDate;
    private String Status;
    private String Warehouse;
    private String WootInOpenOrderQty;
    private String insert_time;
}
