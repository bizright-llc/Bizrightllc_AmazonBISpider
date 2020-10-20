package com.spider.amazon.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ProxySecretType {

    @JsonProperty("residential")
    RESIDENTIAL("residential"),
    @JsonProperty("data_center")
    DATA_CENTER("data_center");

    private String value;

    ProxySecretType(String topic){
        this.value = topic;
    }

    @JsonValue
    public String getValue(){
        return value;
    }

    public static ProxySecretType fromString(String topic) {
        for (ProxySecretType proxySecretType: ProxySecretType.values()) {
            if (proxySecretType.getValue().toLowerCase().equals(topic.toLowerCase())){
                return proxySecretType;
            }
        }

        return null;
    }

}
