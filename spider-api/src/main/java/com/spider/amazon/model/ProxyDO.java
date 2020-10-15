package com.spider.amazon.model;

import lombok.*;

import java.time.LocalDateTime;
import java.util.Date;

/**
 * The proxy ip from the ip provider
 *
 */
@Builder(toBuilder = true)
@ToString
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProxyDO {

    /**
     * Internal id
     */
    private Long id;

    /**
     * Provider id
     */
    private String proxyId;

    private String provider;

    /**
     * Proxy host
     */
    private String ip;

    /**
     * Proxy port
     */
    private String port;

    private String username;

    private String password;

    /**
     * HTTP, SOCK
     */
    private String ipType;

    /**
     * Public, Resident
     */
    private String secretType;

    private String location;

    private String country;

    private String responeSp;

    private Date lastCheckTime;

    private LocalDateTime expiredAt;

    private String ipStatus;

    private Boolean active;

    private LocalDateTime expireAt;

    private LocalDateTime lastUsedAt;

    private LocalDateTime insertedAt;

    private LocalDateTime updatedAt;

}