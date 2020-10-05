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

    // Original string value from file
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

    // Converted Value
    private Integer sessionsNum;
    private Float sessionPercentageNum;
    private Integer pageViewsNum;
    private Float pageViewsPercentageNum;
    private Float buyBoxPercentageNum;
    private Integer unitsOrderedNum;
    private Integer unitsOrderedB2BNum;
    private Float unitSessionPercentageNum;
    private Float unitSessionPercentageB2BNum;
    private Float orderedProductSalesNum;
    private Float orderedProductSalesB2BNum;
    private Integer totalOrderItemsNum;
    private Integer totalOrderItemsB2BNum;

    // file information
    private String fromDate;
    private String toDate;

}
