package com.spider.amazon.model;

import lombok.*;

import java.math.BigDecimal;

@Builder(toBuilder = true)
@ToString
@Data
@AllArgsConstructor
@NoArgsConstructor
public class VcPromotionProductInfoDO {
    private String crawId;

    private String promotionId;

    private String productName;

    private String upc;

    private String amazonPriceStr;

    private BigDecimal amazonPrice;

    private String websitePriceStr;

    private BigDecimal websitePrice;

    private String fundingStr;

    private BigDecimal funding;

    private String likelyPriceStr;

    private BigDecimal likelyPrice;

    private String unitsSoldStr;

    private Integer unitsSold;

    private String amountSpentStr;

    private BigDecimal amountSpent;

    private String revenueStr;

    private BigDecimal revenue;

    private String crawFlg;

    private String asin;

}