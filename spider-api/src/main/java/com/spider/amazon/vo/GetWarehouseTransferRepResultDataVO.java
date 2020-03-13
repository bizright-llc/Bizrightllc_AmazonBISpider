package com.spider.amazon.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * @ClassName GetWarehouseTransferRepResultDataVO
 * @Description WarehouseTransfer信息单条记录
 */
@Builder(toBuilder = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GetWarehouseTransferRepResultDataVO {
    private String WTRefNum;
    private String SendWarehouse;
    private String ReceiveWarehouse;
    private String EstSendDate;
    private String SendDate;
    private String EstReceiveDate;
    private String ReceiveDate;
    private String ItemNum;
    private String ASIN;
    private String Warehouse;
    private String Status;
    private String ReceiveQty;
    private String PendingQty;
    private String SentQty;
}
