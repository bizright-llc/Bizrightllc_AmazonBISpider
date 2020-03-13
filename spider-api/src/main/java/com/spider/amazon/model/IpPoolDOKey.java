package com.spider.amazon.model;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;

@Builder(toBuilder = true)
@ToString
@Data
public class IpPoolDOKey {
    private String ip;

    private String port;

    private String ipType;

}