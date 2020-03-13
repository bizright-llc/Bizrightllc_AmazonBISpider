package com.spider.amazon.vo;


import cn.hutool.json.JSONObject;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @ClassName BzyGetDataOfTaskByOffsetRepVO
 * @Description 八爪鱼根据偏移量获取数据返回实体
 */
@Builder(toBuilder = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BzyGetDataOfTaskByOffsetRepVO {
    private String error;
    private String error_Description;
    private JSONObject data;
}
