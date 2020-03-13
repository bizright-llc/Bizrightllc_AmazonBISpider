package com.spider.amazon.dto;

import lombok.Builder;
import lombok.Data;

/**
 * @ClassName BzyGetTopDTO
 * @Description 八爪鱼导出一批任务数据请求实体
 */
@Builder(toBuilder = true)
@Data
public class BzyGetTopDTO {
    private String taskId;
    private String size;
}
