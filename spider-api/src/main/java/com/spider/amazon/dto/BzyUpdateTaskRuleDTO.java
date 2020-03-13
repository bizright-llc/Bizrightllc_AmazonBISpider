package com.spider.amazon.dto;


import lombok.Builder;
import lombok.Data;

/**
 * @ClassName BzyUpdateTaskRuleDTO
 * @Description 八爪鱼更新任务流程参数请求实体
 */
@Builder(toBuilder = true)
@Data
public class BzyUpdateTaskRuleDTO {
    private String taskId;
    private String name;
    private String value;
    private String token;
}
