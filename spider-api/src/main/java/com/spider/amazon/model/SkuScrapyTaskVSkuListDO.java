package com.spider.amazon.model;

import lombok.*;

import java.util.Date;

@Builder(toBuilder = true)
@ToString
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SkuScrapyTaskVSkuListDO {
    private String taskId;

    private String vendorSku;

    private Date insertTime;

    private String merchantSuggestedAsin;
    
}