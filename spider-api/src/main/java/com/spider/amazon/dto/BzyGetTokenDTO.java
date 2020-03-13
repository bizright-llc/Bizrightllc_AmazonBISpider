package com.spider.amazon.dto;

import lombok.Builder;
import lombok.Data;

/**
 * @ClassName BzyGetTokenDTO
 * @Description 八爪鱼获取令牌请求实体
 */
@Builder(toBuilder = true)
@Data
public class BzyGetTokenDTO {
    private String userName;
    private String passWord;
    private String grantType;
}
