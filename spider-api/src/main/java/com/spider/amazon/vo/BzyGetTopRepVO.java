package com.spider.amazon.vo;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @ClassName BzyGetTopRepVO
 * @Description 八爪鱼导出一批任务数据返回实体
 */
@Builder(toBuilder = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BzyGetTopRepVO {
    private String error;
    private String error_Description;
}
