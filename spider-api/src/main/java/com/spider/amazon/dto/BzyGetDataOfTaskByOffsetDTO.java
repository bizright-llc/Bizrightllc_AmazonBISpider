package com.spider.amazon.dto;


import lombok.Builder;
import lombok.Data;

/**
 * @ClassName BzyGetDataOfTaskByOffsetDTO
 * @Description 八爪鱼根据偏移量获取数据请求实体
 */
@Builder(toBuilder = true)
@Data
public class BzyGetDataOfTaskByOffsetDTO {
    private String taskId;
    private String offset;
    private String size;
    private String token;
}
