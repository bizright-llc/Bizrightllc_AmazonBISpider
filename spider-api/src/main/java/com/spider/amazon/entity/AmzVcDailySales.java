package com.spider.amazon.entity;

import lombok.Data;
import java.io.Serializable;

/**
 * @ClassName AmzVcDailySales
 * @Description 供应商中心每日抓取销量报表
 */
@Data
public class AmzVcDailySales implements Serializable {
    private final long serialVersionUID = 1L;

    private String asin;
    private String productTitle;
    private String shippedCogs;
    private String shippedCogsOfTotal;
    private String shippedCogsPriorPeriod;
    private String shippedCogsLastyear;
    private String shippedUnits;
    private String shippedUnitsOfTotal;
    private String shippedUnitsPriorPeriod;
    private String shippedUnitsLastYear;
    private String OrderedUnits;
    private String OrderedUnitsofTotal;
    private String OrderedUnitsPriorPeriod;
    private String OrderedUnitsLastYear;
    private String customerReturns;
    private String freeReplacements;
    private String SubcategorySalesRank;
    private String SubcategoryBetterWorse;
    private String AverageSalesPrice;
    private String AverageSalesPricePriorPeriod;
    private String ChangeinGlanceViewPriorPeriod;
    private String ChangeinGVLastYear;
    private String RepOOS;
    private String RepOOSofTotal;
    private String RepOOSPriorPeriod;
    private String LBBPrice;
    private String viewingDate;
}
