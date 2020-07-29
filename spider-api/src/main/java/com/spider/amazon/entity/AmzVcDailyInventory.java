package com.spider.amazon.entity;

import lombok.Data;

import java.io.Serializable;

/**
 * @ClassName AmzVcDailyInventory
 * @Description 供应商中心每日抓取库存报表
 */
@Data
public class AmzVcDailyInventory implements Serializable {
    private final long serialVersionUID = 1L;

    private String asin;
    private String productTitle;
    private String netReceived;
    private String netReceivedUnits;
    private String sellThroughRate;
    private String openPurchaseOrderQuantity;
    private String sellableOnHandInventory;
    private String sellableOnHandInventoryTrailing30DayAverage;
    private String sellableOnHandUnits;
    private String unsellableOnHandInventory;
    private String unsellableOnHandInventoryTrailing30DayAverage;
    private String unsellableOnHandUnits;
    private String aged90DaysSellableInventory;
    private String aged90DaysSellableInventoryTrailing30DayAverage;
    private String aged90DaysSellableUnits;
    private String replenishmentCategory;
    private String availableInventory;
    private String unhealthyInventory;
    private String unhealthyInventoryTrailing30dayAverage;
    private String unhealthyUnits;
    private String viewingDate;
    private String insertTime;
    private String distributeView;

}
