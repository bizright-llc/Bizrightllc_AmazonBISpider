package com.spider.amazon.model;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class VcPromotionProcessorConfig {

    private List<String> asins;

    private LocalDate endDateBefore;

    /**
     * Skip exist promotion in database
     */
    private boolean skipExist;

}
