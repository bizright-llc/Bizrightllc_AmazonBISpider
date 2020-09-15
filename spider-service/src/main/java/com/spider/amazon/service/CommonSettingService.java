package com.spider.amazon.service;

import com.spider.amazon.entity.Cookie;

import java.util.List;

/**
 * Connect with db of the common setting value
 */
public interface CommonSettingService {

    /**
     * Get the value by name
     *
     * @param name
     * @return
     */
    String getValue(String name);

    /**
     * Set the value of common setting,
     * if the value exist, it will udpate the
     * value
     *
     * @param name
     * @param value
     * @return
     */
    void setValue(String name, String value, String userId);

    List<Cookie> getAmazonVCCookies();

}
