package com.spider.amazon.model;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;

import java.util.Date;

@Builder(toBuilder = true)
@ToString
@Data
public class LaInventoryInfoDO {
    private String avcinopenorderqty;

    private Date enterdate;

    private String inopenorderqty;

    private String instockqty;

    private String itemnum;

    private Date lastupdate;

    private Date snapshotdate;

    private String status;

    private String warehouse;

    private String wootinopenorderqty;

    private Date insertTime;

    public String getAvcinopenorderqty() {
        return avcinopenorderqty;
    }

    public void setAvcinopenorderqty(String avcinopenorderqty) {
        this.avcinopenorderqty = avcinopenorderqty;
    }

    public Date getEnterdate() {
        return enterdate;
    }

    public void setEnterdate(Date enterdate) {
        this.enterdate = enterdate;
    }

    public String getInopenorderqty() {
        return inopenorderqty;
    }

    public void setInopenorderqty(String inopenorderqty) {
        this.inopenorderqty = inopenorderqty;
    }

    public String getInstockqty() {
        return instockqty;
    }

    public void setInstockqty(String instockqty) {
        this.instockqty = instockqty;
    }

    public String getItemnum() {
        return itemnum;
    }

    public void setItemnum(String itemnum) {
        this.itemnum = itemnum;
    }

    public Date getLastupdate() {
        return lastupdate;
    }

    public void setLastupdate(Date lastupdate) {
        this.lastupdate = lastupdate;
    }

    public Date getSnapshotdate() {
        return snapshotdate;
    }

    public void setSnapshotdate(Date snapshotdate) {
        this.snapshotdate = snapshotdate;
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

    public String getWootinopenorderqty() {
        return wootinopenorderqty;
    }

    public void setWootinopenorderqty(String wootinopenorderqty) {
        this.wootinopenorderqty = wootinopenorderqty;
    }

    public Date getInsertTime() {
        return insertTime;
    }

    public void setInsertTime(Date insertTime) {
        this.insertTime = insertTime;
    }
}