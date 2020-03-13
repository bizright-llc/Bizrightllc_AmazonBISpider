package com.spider.amazon.model;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;

import java.util.Date;

@Builder(toBuilder = true)
@ToString
@Data
public class FBAInventoryInfoDO {
    private String merchantSku;

    private String fulfillmentNetworkSku;

    private String asin;

    private String title;

    private String condition;

    private String price;

    private String mfnListingExists;

    private String mfnFulfillableQty;

    private String afnListingExists;

    private String afnWarehouseQty;

    private String afnFulfillableQty;

    private String afnUnsellableQty;

    private String afnEncumberedQty;

    private String afnTotalQty;

    private String volume;

    private String afnInboundWorkingQty;

    private String afnInboundShippedQty;

    private String afnInboundReceivingQty;

    private Date inserttime;

    public String getMerchantSku() {
        return merchantSku;
    }

    public void setMerchantSku(String merchantSku) {
        this.merchantSku = merchantSku;
    }

    public String getFulfillmentNetworkSku() {
        return fulfillmentNetworkSku;
    }

    public void setFulfillmentNetworkSku(String fulfillmentNetworkSku) {
        this.fulfillmentNetworkSku = fulfillmentNetworkSku;
    }

    public String getAsin() {
        return asin;
    }

    public void setAsin(String asin) {
        this.asin = asin;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getMfnListingExists() {
        return mfnListingExists;
    }

    public void setMfnListingExists(String mfnListingExists) {
        this.mfnListingExists = mfnListingExists;
    }

    public String getMfnFulfillableQty() {
        return mfnFulfillableQty;
    }

    public void setMfnFulfillableQty(String mfnFulfillableQty) {
        this.mfnFulfillableQty = mfnFulfillableQty;
    }

    public String getAfnListingExists() {
        return afnListingExists;
    }

    public void setAfnListingExists(String afnListingExists) {
        this.afnListingExists = afnListingExists;
    }

    public String getAfnWarehouseQty() {
        return afnWarehouseQty;
    }

    public void setAfnWarehouseQty(String afnWarehouseQty) {
        this.afnWarehouseQty = afnWarehouseQty;
    }

    public String getAfnFulfillableQty() {
        return afnFulfillableQty;
    }

    public void setAfnFulfillableQty(String afnFulfillableQty) {
        this.afnFulfillableQty = afnFulfillableQty;
    }

    public String getAfnUnsellableQty() {
        return afnUnsellableQty;
    }

    public void setAfnUnsellableQty(String afnUnsellableQty) {
        this.afnUnsellableQty = afnUnsellableQty;
    }

    public String getAfnEncumberedQty() {
        return afnEncumberedQty;
    }

    public void setAfnEncumberedQty(String afnEncumberedQty) {
        this.afnEncumberedQty = afnEncumberedQty;
    }

    public String getAfnTotalQty() {
        return afnTotalQty;
    }

    public void setAfnTotalQty(String afnTotalQty) {
        this.afnTotalQty = afnTotalQty;
    }

    public String getVolume() {
        return volume;
    }

    public void setVolume(String volume) {
        this.volume = volume;
    }

    public String getAfnInboundWorkingQty() {
        return afnInboundWorkingQty;
    }

    public void setAfnInboundWorkingQty(String afnInboundWorkingQty) {
        this.afnInboundWorkingQty = afnInboundWorkingQty;
    }

    public String getAfnInboundShippedQty() {
        return afnInboundShippedQty;
    }

    public void setAfnInboundShippedQty(String afnInboundShippedQty) {
        this.afnInboundShippedQty = afnInboundShippedQty;
    }

    public String getAfnInboundReceivingQty() {
        return afnInboundReceivingQty;
    }

    public void setAfnInboundReceivingQty(String afnInboundReceivingQty) {
        this.afnInboundReceivingQty = afnInboundReceivingQty;
    }

    public Date getInserttime() {
        return inserttime;
    }

    public void setInserttime(Date inserttime) {
        this.inserttime = inserttime;
    }
}