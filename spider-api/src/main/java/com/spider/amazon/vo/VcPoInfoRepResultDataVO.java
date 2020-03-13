package com.spider.amazon.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * @ClassName VcPoInfoRepResultDataVO
 * @Description VC Po信息单条记录
 */
@Builder(toBuilder = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class VcPoInfoRepResultDataVO {
    private String PONum;
    private String ModelNumber;
    private String ASIN;
    private String SKU;
    private String Title;
    private String Status;
    private String ShipWindowStart;
    private String ShipWindowEnd;
    private String ExpectedShipDate;
    private String ConfirmedShipDate;
    private String QtySubmitted;
    private String QtyAccepted;
    private String QtyReceived;
    private String QtyOutStanding;
    private String UnitCost;
    private String TotalCost;
    private String EnterDate;
    private String LastUpdate;
    private String ShipToLocation;
    private String TransferStatus;
    private String Vendor;
    private String OrderNum;
    private String ShipFromCompany;
    private String ShipFromContact;
    private String ShipFromAddress1;
    private String ShipFromAddress2;
    private String ShipFromCity;
    private String ShipFromPostalCode;
    private String ShipFromState;
    private String ShipFromCountry;
}
