package com.spider.amazon.model;

import lombok.*;

import java.util.Date;

@Builder(toBuilder = true)
@ToString
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MAsinToVskuMapDO {
    private String merchantSuggestedAsin;

    private String vendorSku;

    private String effFlg;

    private Date insertTime;
}