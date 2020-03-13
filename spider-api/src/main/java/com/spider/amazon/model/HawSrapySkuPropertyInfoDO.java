package com.spider.amazon.model;

import lombok.*;

import java.util.Date;

@Builder(toBuilder = true)
@ToString
@Data
@AllArgsConstructor
@NoArgsConstructor
public class HawSrapySkuPropertyInfoDO {
    private String taskId;

    private String productId;

    private String productSimpleId;

    private String propertyName;

    private String propertyValue;

    private Date insertTime;

    private String vendorSku;

    private String merchantSuggestedAsin;
}