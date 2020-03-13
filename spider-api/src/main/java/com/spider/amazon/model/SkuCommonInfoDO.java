package com.spider.amazon.model;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;

import java.util.Date;

@Builder(toBuilder = true)
@ToString
@Data
public class SkuCommonInfoDO {
    private String asin;

    private String skuProducttitle;

    private String skuProperty;

    private String mainAvastar;

    private String mainOnestarnum;

    private String mainTwostarnum;

    private String mainThreestarnum;

    private String mainFourstarnum;

    private String mainFivestarnum;

    private String skuPrice;

    private String reviewurl;

    private String skuImageurl;

    private String mainRwnum;

    private String mainAwnum;

    private String skuIncreasedrwnum;

    private String skuAvailability;

    private String skuOnestarnum;

    private String skuTwostarnum;

    private String skuThreestarnum;

    private String skuFourstarnum;

    private String skuFivestarnum;

    private Integer skuRwnum;

    private Date inserttime;

    private String skuSoldby;

    private String skuShipsfrom;

    private String skuIsprime;

    private String skuOffersnum;

    private String citycode;

    private String cityname;

    private String brand;

}