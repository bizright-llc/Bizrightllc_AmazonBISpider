package com.spider.amazon.dto;

import lombok.Builder;
import lombok.Data;

@Builder(toBuilder = true)
@Data
/**
 * @ClassName GetInvoiceDTO
 * @Description BOP获取Invoice信息请求实体
 */
public class GetInvoiceDTO {
    private int pageNo;
    private int pageSize;
    private String asin;
    private String channel;
    private String invoiceDate;
}
