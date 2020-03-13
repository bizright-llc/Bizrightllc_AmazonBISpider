package com.spider.amazon.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * @ClassName GetPOHeaderRepResultDataVO
 * @Description POHeader信息单条记录
 */
@Builder(toBuilder = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GetPOHeaderRepResultDataVO {
    private String PONum;
    private String VendorName;
    private String VendorCompany;
    private String ASIN;
    private String ItemNum;
    private String Status;
    private String UnitPrice;
    private String POQty;
    private String PODate;
    private String EstimateReceiveDate;
    private String Warehouse;
    private String PORefNum;
}
