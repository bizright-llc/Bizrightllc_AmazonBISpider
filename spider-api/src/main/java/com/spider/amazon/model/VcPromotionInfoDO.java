package com.spider.amazon.model;

import lombok.*;

import java.time.LocalDateTime;

@Builder(toBuilder = true)
@ToString
@Data
@AllArgsConstructor
@NoArgsConstructor
public class VcPromotionInfoDO {
    private String crawId;

    private String createdOnStr;

    private String promotionId;

    private String status;

    private String name;

    private String startDateStr;

    private String endDateStr;

    private String type;

    private String heroProduct;

    private String vendorCode;

    private String marketPlace;

    private String billingContact;

    private String fundingAgreement;

    private String merchandisingFee;

    private String crawFlg;

    private LocalDateTime createdOn;

    private LocalDateTime startDate;

    private LocalDateTime endDate;

}