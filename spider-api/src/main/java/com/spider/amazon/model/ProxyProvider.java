package com.spider.amazon.model;

import com.fasterxml.jackson.annotation.JsonValue;

public enum ProxyProvider {

    LUMINATI("luminati"),
    SMARTPROXIES("smartproxies"),
    TEST("test");

    private String value;

    ProxyProvider(String topic){
        this.value = topic;
    }

    @JsonValue
    public String getValue(){
        return value.toLowerCase();
    }

    public static ProxyProvider fromString(String topic) {
        for (ProxyProvider proxyProvider: ProxyProvider.values()) {
            if (proxyProvider.getValue().toLowerCase().equals(topic.toLowerCase())){
                return proxyProvider;
            }
        }

        return null;
    }

    public static void main(String[] args) {
        for (ProxyProvider proxyProvider: ProxyProvider.values()) {
            System.out.println(proxyProvider.getValue());
        }
    }

}
