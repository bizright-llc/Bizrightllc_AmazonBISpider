package com.spider.amazon.vo;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @ClassName BzyGetTokenRepVO
 * @Description 八爪鱼获取令牌请求实体
 */
@Builder(toBuilder = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BzyGetTokenRepVO {
    private String access_token;
    private String token_type;
    private Long expires_in;
    private String refresh_token;
}
