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

    private ProxyProvider provider;

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

    /**
     * Proxy country
     */
    private String location;

    private String responeSp;

    /**
     * Last time check proxy valid or not
     */
    private LocalDateTime lastCheckTime;

    /**
     *
     */
    private String ipStatus;

    /**
     * Self rotating proxy host
     */
    private Boolean selfRotating;

    /**
     * This proxy is active or not
     */
    private Boolean active;

    /**
     * This proxy expire time
     */
    private LocalDateTime expireAt;

    /**
     * Last used time
     */
    private LocalDateTime lastUsedAt;

    /**
     * Insert time
     */
    private LocalDateTime insertedAt;

    /**
     * Last update time
     */
    private LocalDateTime updatedAt;

}