package com.spider.amazon.model;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;

import java.util.Date;

@Builder(toBuilder = true)
@ToString
@Data
public class InvoiceInfoInfoDO {
    private String invoicenum;

    private String itemnum;

    private String asin;

    private String orderqty;

    private String lineamt;

    private Date invoicedate;

    private String channel;

    private String unitprice;

    private String buyeruserid;

    private String shipcity;

    private String shipaddr1;

    private Date paymentdate;

    private String status;

    private String shipstate;

    private String shipcountry;

    private Integer linenum;

    private String comboitemnum;

    private Integer isfromcombo;

    public String getInvoicenum() {
        return invoicenum;
    }

    public void setInvoicenum(String invoicenum) {
        this.invoicenum = invoicenum;
    }

    public String getItemnum() {
        return itemnum;
    }

    public void setItemnum(String itemnum) {
        this.itemnum = itemnum;
    }

    public String getAsin() {
        return asin;
    }

    public void setAsin(String asin) {
        this.asin = asin;
    }

    public String getOrderqty() {
        return orderqty;
    }

    public void setOrderqty(String orderqty) {
        this.orderqty = orderqty;
    }

    public String getLineamt() {
        return lineamt;
    }

    public void setLineamt(String lineamt) {
        this.lineamt = lineamt;
    }

    public Date getInvoicedate() {
        return invoicedate;
    }

    public void setInvoicedate(Date invoicedate) {
        this.invoicedate = invoicedate;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getUnitprice() {
        return unitprice;
    }

    public void setUnitprice(String unitprice) {
        this.unitprice = unitprice;
    }

    public String getBuyeruserid() {
        return buyeruserid;
    }

    public void setBuyeruserid(String buyeruserid) {
        this.buyeruserid = buyeruserid;
    }

    public String getShipcity() {
        return shipcity;
    }

    public void setShipcity(String shipcity) {
        this.shipcity = shipcity;
    }

    public String getShipaddr1() {
        return shipaddr1;
    }

    public void setShipaddr1(String shipaddr1) {
        this.shipaddr1 = shipaddr1;
    }

    public Date getPaymentdate() {
        return paymentdate;
    }

    public void setPaymentdate(Date paymentdate) {
        this.paymentdate = paymentdate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getShipstate() {
        return shipstate;
    }

    public void setShipstate(String shipstate) {
        this.shipstate = shipstate;
    }

    public String getShipcountry() {
        return shipcountry;
    }

    public void setShipcountry(String shipcountry) {
        this.shipcountry = shipcountry;
    }

    public Integer getLinenum() {
        return linenum;
    }

    public void setLinenum(Integer linenum) {
        this.linenum = linenum;
    }

    public String getComboitemnum() {
        return comboitemnum;
    }

    public void setComboitemnum(String comboitemnum) {
        this.comboitemnum = comboitemnum;
    }

    public Integer getIsfromcombo() {
        return isfromcombo;
    }

    public void setIsfromcombo(Integer isfromcombo) {
        this.isfromcombo = isfromcombo;
    }
}