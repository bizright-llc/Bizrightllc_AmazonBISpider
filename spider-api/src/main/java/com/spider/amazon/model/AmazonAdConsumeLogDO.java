package com.spider.amazon.model;

import com.spider.amazon.cons.AmazonAdNodeType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * This class is the log for the amazon ad consume schedule log the ad consume
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AmazonAdConsumeLogDO {

    private Long id;

    /**
     * This field show the ad consume is trigger by which setting
     */
    private Long settingId;

    private AmazonAdNodeType type;

    private LocalDateTime createdAt;

    /**
     * The updated at should be same as created at
     */
    private LocalDateTime updatedAt;

    /**
     * The log should created by system
     */
    private String createdBy;

    private String updatedBy;

}
