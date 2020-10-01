package com.spider.amazon.entity;

import lombok.Data;

import java.io.Serializable;

/**
 * @ClassName AmzScBuyBox
 * @Description 卖家中心Buy Box信息
 */
@Data
public class AmzScBuyBox implements Serializable {
    private final long serialVersionUID = 1L;

    private String parentAsin;
    private String childAsin;
    private String title;
    private String sessions;
    private String sessionPercentage;
    private String pageViews;
    private String pageViewsPercentage;
    private String buyBoxPercentage;
    private String unitsOrdered;
    private String unitsOrderedB2B;
    private String unitSessionPercentage;
    private String unitSessionPercentageB2B;
    private String orderedProductSales;
    private String orderedProductSalesB2B;
    private String totalOrderItems;
    private String totalOrderItemsB2B;

    // file information
    private String fromDate;
    private String toDate;

}
