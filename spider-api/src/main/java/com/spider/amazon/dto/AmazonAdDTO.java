package com.spider.amazon.dto;

import com.spider.amazon.cons.AmazonAdNodeType;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
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
@NoArgsConstructor
@AllArgsConstructor
public class AmazonAdDTO {
    private AmazonAdNodeType type;
    private String title;
    private String brand;
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
                ", brand='" + brand + '\'' +
                ", settingId='" + settingId + '\'' +
                '}';
    }
}
