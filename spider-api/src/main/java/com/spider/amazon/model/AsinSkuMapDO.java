package com.spider.amazon.model;

import lombok.*;

import java.time.LocalDateTime;

@Builder(toBuilder = true)
@ToString
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AsinSkuMapDO {

    private String asin;

    private String sku;

    private String pm;

    private String createdBy;

    private String updatedBy;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

}
