package com.spider.amazon.model;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;

import java.util.Date;

@Builder(toBuilder = true)
@ToString
@Data
public class LaPoInfoDO {
    private String ponum;

    private String vendorname;

    private String vendorcompany;

    private String asin;

    private String itemnum;

    private String status;

    private String unitprice;

    private String poqty;

    private Date podate;

    private Date estimatereceivedate;

    private Date insertTime;

    private String biPoRef;

    private String warehouse;

    private String porefnum;

}