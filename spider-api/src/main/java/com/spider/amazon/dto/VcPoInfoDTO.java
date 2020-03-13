package com.spider.amazon.dto;

import lombok.Builder;
import lombok.Data;

@Builder(toBuilder = true)
@Data
/**
 * @ClassName VcPoInfoDTO
 * @Description BOP获取VC PO信息请求实体
 */
public class VcPoInfoDTO {
    private int pageNo;
    private int pageSize;
    private String enterDate;
    private String lastUpdate;
    private String asin;
    private String poNum;
    private String vendor;
}
