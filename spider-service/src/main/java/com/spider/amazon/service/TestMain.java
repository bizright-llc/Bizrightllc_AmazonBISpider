package com.spider.amazon.service;

import cn.hutool.core.util.StrUtil;
import org.springframework.stereotype.Service;

/**
 * Created by zhucan on 19/10/30.
 */
@Service
public class TestMain {

    public void testService() {
        System.out.println("测试调用内部服务");
    }

    private String myname="haha";

    public static void main(String[] args){
        String example="https://sellercentral.amazon.com/gp/ssof/reports/documents/_GET_FBA_MYI_UNSUPPRESSED_INVENTORY_DATA__17683055807018222.txt?ie=UTF8&contentType=text%2Fcsv";
        String filename= StrUtil.subBetween(example,"DATA__",".txt");
        System.out.println(" filename : "+filename);
    }

}
