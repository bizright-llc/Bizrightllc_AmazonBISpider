package com.spider.amazon.vo;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @ClassName BzyUpdateTopRepVO
 * @Description 八爪鱼标记数据为已导出状态返回实体
 */
@Builder(toBuilder = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BzyUpdateTopRepVO {
    private String error;
    private String error_Description;
}