package com.spider.amazon.model;

import lombok.*;

@Builder(toBuilder = true)
@ToString
@Data
@AllArgsConstructor
@NoArgsConstructor
public class VcPromotionProductInfoDO {
    private String crawId;

    private String promotionId;

    private String pname;

    private String upc;

    private String amazonPrice;

    private String websitePrice;

    private String funding;

    private String likelyPrice;

    private String unitsSold;

    private String amountSpent;

    private String revenue;

    private String crawFlg;

    private String asin;

}