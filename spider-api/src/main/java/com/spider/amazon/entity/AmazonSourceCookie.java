package com.spider.amazon.entity;

import lombok.Data;

import java.util.Date;

/**
 * @ClassName AmazonSourceCookie
 * @Description Cookie类
 */
@Data
public class AmazonSourceCookie {
    private String name;
    private String value;
    private String path;
    private String domain;
    private Date expirationDate;
    private Boolean hostOnly;
    private Boolean httpOnly;
    private String sameSite;
    private Boolean secure;
    private Boolean session;
    private String storeId;
    private Long id;

}