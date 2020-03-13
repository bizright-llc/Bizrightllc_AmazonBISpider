package com.spider.amazon.model;

import lombok.*;

import java.util.Date;

@Builder(toBuilder = true)
@ToString
@Data
@AllArgsConstructor
@NoArgsConstructor
public class VskuToPidMapDO {
    private String vendorSku;

    private String productId;

    private String effFlg;

    private Date insertTime;

}