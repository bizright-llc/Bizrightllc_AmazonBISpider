package com.spider.amazon.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * @ClassName GetInvoiceRepVO
 * @Description 获取Invoice信息请求返回实体
 */
@Builder(toBuilder = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GetInvoiceRepVO {
    private ResponseHeader responseHeader;
    private String code;
    private String message;
    private GetInvoiceRepResultVO result;
}
