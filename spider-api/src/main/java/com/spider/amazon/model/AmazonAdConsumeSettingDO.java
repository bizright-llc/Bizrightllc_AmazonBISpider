package com.spider.amazon.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * This class save the setting of the amazon ad consume
 *
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AmazonAdConsumeSettingDO {

    private Long id;

    private String name;

    private String description;

    private String searchWords;

    private Boolean active;

    private List<AmazonAdConsumeItemDO> items;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private String createdBy;

    private String updatedBy;

    private Boolean removed;

}
