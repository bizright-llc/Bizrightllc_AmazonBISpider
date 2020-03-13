package com.spider.amazon.model;


import lombok.Builder;
import lombok.Data;
import lombok.ToString;

import java.util.Date;

@Builder(toBuilder = true)
@ToString
@Data
public class VendorPODetailInfoDO {
    private String po;

    private String vendor;

    private String shipToLocation;

    private String modelNumber;

    private String asin;

    private String sku;

    private String title;

    private String status;

    private String shipWindowStart;

    private String shipWindowEnd;

    private String deliveryWindowStart;

    private String deliveryWindowEnd;

    private String backorder;

    private String expectedShipDate;

    private String quantitySubmitted;

    private String acceptedQuantity;

    private String quantityReceived;

    private String quantityOutstanding;

    private String unitCost;

    private String totalCost;

    private Date inserttime;


}