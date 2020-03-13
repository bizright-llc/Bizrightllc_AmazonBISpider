package com.spider.amazon.dto;

import lombok.Builder;
import lombok.Data;

@Builder(toBuilder = true)
@Data
/**
 * @ClassName GetWarehouseTransferDTO
 * @Description BOP获取WarehouseTransfer信息请求实体
 */
public class GetWarehouseTransferDTO {
    private int pageNo;
    private int pageSize;
}
