package com.spider.amazon.dto;

import lombok.Builder;
import lombok.Data;

/**
 * @ClassName BzyUpdateTopDTO
 * @Description 八爪鱼标记数据为已导出状态请求实体
 */
@Builder(toBuilder = true)
@Data
public class BzyUpdateTopDTO {
    private String taskId;
}
