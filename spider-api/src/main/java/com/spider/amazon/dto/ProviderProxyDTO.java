package com.spider.amazon.dto;

import com.spider.amazon.model.ProxyProvider;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Date;

/**
 * This model is the proxy data from the provider
 *
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProviderProxyDTO {

    /**
     * Provider id
     */
    private String proxyId;

    private ProxyProvider provider;

    /**
     * Proxy host
     */
    private String ip;

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

    private LocalDateTime expiredAt;

    private String ipStatus;

    private Boolean selfRotating;

    private LocalDateTime lastUsedAt;

    private LocalDateTime insertedAt;

    private LocalDateTime updatedAt;

}
