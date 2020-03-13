package com.spider.amazon.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * @ClassName GetPOHeaderRepVO
 * @Description 获取PO信息请求返回实体
 */
@Builder(toBuilder = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GetPOHeaderRepVO {
    private ResponseHeader responseHeader;
    private String code;
    private String message;
    private GetPOHeaderRepResultVO result;
}
