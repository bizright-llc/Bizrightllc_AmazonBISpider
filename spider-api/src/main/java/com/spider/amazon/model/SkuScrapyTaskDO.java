package com.spider.amazon.model;

import lombok.*;

import java.util.Date;

@Builder(toBuilder = true)
@ToString
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SkuScrapyTaskDO {
    private String taskId;

    private String taskName;

    private String uploadFilePath;

    private String uploadFileName;

    private String downloadFilePath;

    private String downloadFileName;

    private String taskSts;

    private Date insertTime;

}