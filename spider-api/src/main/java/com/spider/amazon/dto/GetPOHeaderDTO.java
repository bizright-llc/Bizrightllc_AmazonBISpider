package com.spider.amazon.dto;

import lombok.Builder;
import lombok.Data;

@Builder(toBuilder = true)
@Data
/**
 * @ClassName GetPOHeaderDTO
 * @Description BOP获取POHeader信息请求实体
 */
public class GetPOHeaderDTO {
    private int pageNo;
    private int pageSize;
    private String asin;
    private String poDate;
}
