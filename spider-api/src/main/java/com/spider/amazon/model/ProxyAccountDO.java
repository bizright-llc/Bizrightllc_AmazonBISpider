package com.spider.amazon.model;

import com.sun.org.apache.xpath.internal.operations.Bool;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProxyAccountDO {

    private Long id;

    /**
     * provider
     */
    private ProxyProvider provider;

    /**
     * account username
     */
    private String username;

    /**
     * account password
     *
     * may be null cause account using api token
     */
    private String password;

    /**
     * account token
     */
    private String token;

    /**
     * account is active or not
     */
    private Boolean active;

    /**
     * Expire time
     */
    private LocalDateTime expireAt;

    /**
     * Inserted time
     */
    private LocalDateTime insertedAt;

    /**
     * Updated time
     */
    private LocalDateTime updatedAt;

    /**
     * The account is delete or not
     */
    private Boolean remove;

}
