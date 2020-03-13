package com.spider.amazon.entity;

import lombok.Data;

import java.util.Date;


/**
 * @ClassName Cookie
 * @Description Cookie类
 */
@Data
public class Cookie {
    private String name;
    private String value;
    private String path;
    private String domain;
    private Date expiry;
    private Boolean isSecure;
    private Boolean isHttpOnly;


}