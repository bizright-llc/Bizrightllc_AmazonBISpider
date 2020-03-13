package com.spider.amazon.dto;

import lombok.Builder;
import lombok.Data;

/**
 * @ClassName AmazonAdIndexDTO
 * @Description 亚马逊主页广告位置实体
 */
@Builder(toBuilder = true)
@Data
public class AmazonAdIndexDTO {
    private String dataAsin;
    private String dataIndex;
}
