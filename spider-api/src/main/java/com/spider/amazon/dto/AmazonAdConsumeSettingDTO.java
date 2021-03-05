package com.spider.amazon.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AmazonAdConsumeSettingDTO {

    private Long id;

    private String name;

    private String description;

    private String searchWords;

    private Boolean active;

    private List<AmazonAdConsumeItemDTO> items;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private String createdBy;

    private String updatedBy;

    private Boolean removed;

}
