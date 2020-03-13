package com.spider.amazon.model;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;

import java.util.Date;

@Builder(toBuilder = true)
@ToString
@Data
public class IpPoolDO {
    private String ip;

    private String port;

    private String ipType;

    private String secreType;

    private String location;

    private String responeSp;

    private Date lastCheckTime;

    private String ipStatus;

}