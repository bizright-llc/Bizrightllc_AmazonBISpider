package com.spider.amazon.entity;

import cn.hutool.core.date.DateTime;
import lombok.Data;

import java.io.Serializable;

/**
 * @ClassName AmzScBuyBox
 * @Description 卖家中心Buy Box信息
 */
@Data
public class AmzScFbaFee implements Serializable {
    private final long serialVersionUID = 1L;

    /**
     * File name, let user check the file if data have problem
     */
    private String filename;

    private String datetime;
    private String settlementId;
    private String orderId;
    private String sku;
    private String description;
    private String quantity;
    private String marketplace;
    private String accountType;
    private String fulfillment;
    private String orderCity;
    private String orderState;
    private String orderPostal;
    private String taxCollectionModel;
    private String productSales;
    private String productSalesTax;
    private String shippingCredits;
    private String shippingCreditsTax;
    private String giftWrapCredits;
    private String giftWrapCreditsTax;
    private String promotionalRebates;
    private String promotionalRebatesTax;
    private String marketplaceWithheldTax;
    private String sellingFees;
    private String fbaFees;
    private String otherTransactionFees;
    private String other;
    private String total;

    /**
     * Transform data
     */
    private DateTime transactionDatetime;
    private Float productSalesNum;
    private Float productSalesTaxNum;
    private Float shippingCreditsNum;
    private Float shippingCreditsTaxNum;
    private Float giftWrapCreditsNum;
    private Float giftWrapCreditsTaxNum;
    private Float promotionalRebatesNum;
    private Float promotionalRebatesTaxNum;
    private Float marketplaceWithheldTaxNum;
    private Float sellingFeesNum;
    private Float fbaFeesNum;
    private Float otherTransactionFeesNum;
    private Float otherNum;
    private Float totalNum;

    private DateTime InsertedAt;

}
