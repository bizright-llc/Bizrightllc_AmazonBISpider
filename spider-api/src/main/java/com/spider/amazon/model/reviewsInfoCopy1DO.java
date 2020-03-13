package com.spider.amazon.model;

import lombok.*;

import java.util.Date;

@Builder(toBuilder = true)
@ToString
@Data
@AllArgsConstructor
@NoArgsConstructor
public class reviewsInfoCopy1DO {
    private String asin;

    private String reviewsid;

    private String star;

    private Date date;

    private String customername;

    private String property;

    private String verifiedpurchase;

    private String helpefulnum;

    private String nothelpfulnum;

    private String reviewcontent;

    private String commentnum;

    private String reviewtitle;

    private String reviewimageurl;

    private String customerurl;

    private Date inserttime;

}