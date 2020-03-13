package com.spider.amazon.vo;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @ClassName BzyUpdateTaskRuleRepVO
 * @Description 八爪鱼更新任务流程参数返回实体
 */
@Builder(toBuilder = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BzyUpdateTaskRuleRepVO {
    private String error;
    private String error_Description;
}
