package com.spider.amazon.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * @ClassName GetWarehouseTransferRepVO
 * @Description 获取WarehouseTransfer信息请求返回实体
 */
@Builder(toBuilder = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GetWarehouseTransferRepVO {
    private ResponseHeader responseHeader;
    private String code;
    private String message;
    private GetWarehouseTransferRepResultVO result;
}
