package com.spider.amazon.model;

import cn.hutool.core.date.DateTime;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

@Builder(toBuilder = true)
@ToString
@Data
public class FBAInventoryInfoDO {

    private String merchantSku;

    private String fulfillmentNetworkSku;

    private String asin;

    private String title;

    private String condition;

    private String price;

    private String mfnListingExists;

    private String mfnFulfillableQty;

    private String afnListingExists;

    private String afnWarehouseQty;

    private String afnFulfillableQty;

    private String afnUnsellableQty;

    private String afnEncumberedQty;

    private String afnTotalQty;

    private String volume;

    private String afnInboundWorkingQty;

    private String afnInboundShippedQty;

    private String afnInboundReceivingQty;

    private LocalDate inventoryDate;

    private LocalDateTime insertedAt;

}