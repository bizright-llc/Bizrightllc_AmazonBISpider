package com.spider.amazon.model;

import lombok.*;

@Builder(toBuilder = true)
@ToString
@Data
@AllArgsConstructor
@NoArgsConstructor
public class VcPromotionInfoDO {
    private String crawId;

    private String createdOn;

    private String promotionId;

    private String status;

    private String name;

    private String startDate;

    private String endDate;

    private String type;

    private String heroProduct;

    private String vendorCode;

    private String marketPlace;

    private String billingContact;

    private String fundingAgreement;

    private String merchandisingFee;

    private String crawFlg;

}