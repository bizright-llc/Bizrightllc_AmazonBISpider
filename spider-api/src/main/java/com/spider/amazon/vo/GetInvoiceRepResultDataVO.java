package com.spider.amazon.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * @ClassName GetInvoiceRepResultDataVO
 * @Description Invoice信息单条记录
 */
@Builder(toBuilder = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GetInvoiceRepResultDataVO {
    private String InvoiceNum;
    private String LineNum;
    private String ItemNum;
    private String ComboItemNum;
    private String IsFromCombo;
    private String asin;
    private String OrderQty;
    private String LineAmt;
    private String InvoiceDate;
    private String Channel;
    private String UnitPrice;
    private String BuyerUserID;
    private String ShipCity;
    private String ShipAddr1;
    private String PaymentDate;
    private String Status;
    private String ShipState;
    private String ShipCountry;

}
