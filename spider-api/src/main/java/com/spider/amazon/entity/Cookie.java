package com.spider.amazon.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;


/**
 * @ClassName Cookie
 * @Description Cookie类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Cookie {
    private String name;
    private String value;
    private String path;
    private String domain;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd@HH:mm:ss.SSSZ")
    private Date expiry;
    private Boolean isSecure;
    private Boolean isHttpOnly;
}