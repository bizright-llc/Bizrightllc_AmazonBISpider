package com.spider.amazon.model;

import lombok.Data;

import java.util.Date;

@Data
public class SkuInfoNewDO extends SkuInfoNewDOKey {
    private String ourcode;

    private String rootcategory;

    private Date inserttime;

    private String groupleadsku;

    private String name;

    private String productmanager;

    private Integer productmanagerId;

    private String subcategory;

    private String ownbrand;

    private String productline;

}