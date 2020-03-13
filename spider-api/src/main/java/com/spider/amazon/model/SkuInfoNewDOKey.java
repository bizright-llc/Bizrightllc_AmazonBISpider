package com.spider.amazon.model;

import lombok.Data;
import lombok.ToString;

@ToString
@Data
public class SkuInfoNewDOKey {
    private String asin;

    private Long userid;

    public String getAsin() {
        return asin;
    }

    public void setAsin(String asin) {
        this.asin = asin;
    }

    public Long getUserid() {
        return userid;
    }

    public void setUserid(Long userid) {
        this.userid = userid;
    }
}