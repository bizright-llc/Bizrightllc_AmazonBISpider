package com.spider.amazon.model;

import lombok.*;

import java.util.Date;

@Builder(toBuilder = true)
@ToString
@Data
@AllArgsConstructor
@NoArgsConstructor
public class HawSrapySkuInfoDO {
    private String taskId;

    private String productId;

    private String productSimpleId;

    private String productTitle;

    private String productTitleElse;

    private String productPrice;

    private String imgUrl;

    private String productIntroduce;

    private String pageUrl;

    private Date insertTime;

    private String vendorSku;

    private String merchantSuggestedAsin;

    private String productBrands;
}