package com.spider.amazon.service;

import com.spider.amazon.dto.ProxyDTO;

/**
 * Rest service
 */
public interface RestService {

    boolean testProxy(ProxyDTO proxy) throws Exception;

    String getPlainJSON(String url) throws Exception;

    String getPlainJSON(String url, ProxyDTO proxy) throws Exception;

    <T extends Object> T[] get(String url, Class<T> type);

    <T extends Object> T[] get(String url, Class<T> type, ProxyDTO proxy);

}
