package com.spider.amazon.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @ClassName PromotionList
 * @Description Promotion列表类
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PromotionList {
    private String promotionId;
    private String promotionDetailUrl;
}