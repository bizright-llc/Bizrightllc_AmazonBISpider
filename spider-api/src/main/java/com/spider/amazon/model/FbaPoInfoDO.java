package com.spider.amazon.model;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;

import java.util.Date;

@Builder(toBuilder = true)
@ToString
@Data
public class FbaPoInfoDO {
    private String itemnum;

    private String asin;

    private Date estrcvdate;

    private Date estsenddate;

    private String pendingqty;

    private Date rcvdate;

    private String rcvwarehouse;

    private String receiveqty;

    private Date senddate;

    private String sendwarehouse;

    private String sentqty;

    private String status;

    private String warehouse;

    private Date insertTime;

    private String wtrefnum;

    private String biPoRef;

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

    public Date getEstrcvdate() {
        return estrcvdate;
    }

    public void setEstrcvdate(Date estrcvdate) {
        this.estrcvdate = estrcvdate;
    }

    public Date getEstsenddate() {
        return estsenddate;
    }

    public void setEstsenddate(Date estsenddate) {
        this.estsenddate = estsenddate;
    }

    public String getPendingqty() {
        return pendingqty;
    }

    public void setPendingqty(String pendingqty) {
        this.pendingqty = pendingqty;
    }

    public Date getRcvdate() {
        return rcvdate;
    }

    public void setRcvdate(Date rcvdate) {
        this.rcvdate = rcvdate;
    }

    public String getRcvwarehouse() {
        return rcvwarehouse;
    }

    public void setRcvwarehouse(String rcvwarehouse) {
        this.rcvwarehouse = rcvwarehouse;
    }

    public String getReceiveqty() {
        return receiveqty;
    }

    public void setReceiveqty(String receiveqty) {
        this.receiveqty = receiveqty;
    }

    public Date getSenddate() {
        return senddate;
    }

    public void setSenddate(Date senddate) {
        this.senddate = senddate;
    }

    public String getSendwarehouse() {
        return sendwarehouse;
    }

    public void setSendwarehouse(String sendwarehouse) {
        this.sendwarehouse = sendwarehouse;
    }

    public String getSentqty() {
        return sentqty;
    }

    public void setSentqty(String sentqty) {
        this.sentqty = sentqty;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getWarehouse() {
        return warehouse;
    }

    public void setWarehouse(String warehouse) {
        this.warehouse = warehouse;
    }

    public Date getInsertTime() {
        return insertTime;
    }

    public void setInsertTime(Date insertTime) {
        this.insertTime = insertTime;
    }

    public String getWtrefnum() {
        return wtrefnum;
    }

    public void setWtrefnum(String wtrefnum) {
        this.wtrefnum = wtrefnum;
    }

    public String getBiPoRef() {
        return biPoRef;
    }

    public void setBiPoRef(String biPoRef) {
        this.biPoRef = biPoRef;
    }
}