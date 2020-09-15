package com.spider.amazon.model;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CommonSettingDO {

    private Long id;

    private String name;

    private String value;

    private String createdBy;

    private String updatedBy;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

}
