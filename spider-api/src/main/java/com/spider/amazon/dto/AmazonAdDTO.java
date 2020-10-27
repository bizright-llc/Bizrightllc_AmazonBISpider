package com.spider.amazon.dto;

import com.spider.amazon.cons.AmazonAdNodeType;
import org.openqa.selenium.*;
import lombok.Builder;
import lombok.Data;

/**
 * @ClassName AmazonAdIndexDTO
 * @Description 亚马逊主页广告位置实体
 */
@Builder(toBuilder = true)
@Data
public class AmazonAdDTO {
    private AmazonAdNodeType type;
    private String title;
    private String asin;
    private String index;

    private Long settingId;

    private String xpath;
    private WebElement webElement;

    @Override
    public String toString() {
        return "AmazonAdDTO{" +
                "type=" + type +
                ", title='" + title + '\'' +
                ", asin='" + asin + '\'' +
                ", index='" + index + '\'' +
                '}';
    }
}
