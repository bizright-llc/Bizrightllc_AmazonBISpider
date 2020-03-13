package com.spider.amazon.utils;

import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.spider.amazon.entity.AmazonSourceCookie;
import com.spider.amazon.entity.Cookie;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class JsonToListUtil {
    private static final String jsonPath = "C:\\Program Files\\Java\\BiSpider\\cookieVc.json";
    private static final String jsonPathSc = "C:\\Program Files\\Java\\BiSpider\\cookieSc.json";

    public static List<AmazonSourceCookie> getList() {
        String jsonString = fileToStr(jsonPath);
        List<AmazonSourceCookie> list = JSONObject.parseArray(jsonString, AmazonSourceCookie.class);
        return list;
    }

    public static List<AmazonSourceCookie> getListByPath(String jsonPath) {
        String jsonString = fileToStr(jsonPath);
        List<AmazonSourceCookie> list = JSONObject.parseArray(jsonString, AmazonSourceCookie.class);
        return list;
    }

    public static List<Cookie> amazonSourceCookieList2CookieList( List<AmazonSourceCookie> amazonSourceCookieList) {
        List<Cookie> list = new ArrayList<>();
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        cal.add(Calendar.YEAR, 10);
        for ( AmazonSourceCookie amazonSourceCookie:amazonSourceCookieList) {
            Cookie cookie=new Cookie();
            cookie.setName(amazonSourceCookie.getName());
            cookie.setValue(amazonSourceCookie.getValue());
            cookie.setPath(amazonSourceCookie.getPath());
            cookie.setDomain(amazonSourceCookie.getDomain());
            cookie.setExpiry( cal.getTime());
            cookie.setIsHttpOnly(amazonSourceCookie.getHttpOnly());
            cookie.setIsSecure(amazonSourceCookie.getSecure());
            list.add(cookie);
        }

        return list;
    }


    public static String fileToStr(String filepath) {
        File file = new File(filepath);
        StringBuffer sb = new StringBuffer();
        InputStreamReader isr = null;
        try {
            isr = new InputStreamReader(new FileInputStream(file), "UTF-8");
            while (isr.ready()) {
                sb.append((char) isr.read());
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                isr.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }

    public static void main(String[] args) {
        List<Cookie> listCookies = amazonSourceCookieList2CookieList(getListByPath(jsonPathSc));

        System.out.println("listCookies:"+ JSONUtil.parseObj(listCookies));

        for (Cookie cookie : listCookies) {
            System.out.println(cookie.getName());
        }

        JSONArray array = (JSONArray) JSONArray.toJSON(listCookies);
        System.out.println(array);
    }
}