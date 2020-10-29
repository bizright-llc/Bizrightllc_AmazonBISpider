package com.spider.amazon.dto;

import com.spider.amazon.cons.AmazonAdNodeType;
import org.openqa.selenium.*;
import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName AmazonAdIndexDTO
 * @Description Amazon ad object
 */
@Builder(toBuilder = true)
@Data
public class AmazonAdDTO {
    private AmazonAdNodeType type;
    private String title;
    private String brand;
    private String asin;
    private String index;

    private List<Long> settingIds;

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
