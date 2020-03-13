package com.spider.amazon.cons;

/**
 * 日期计算方式枚举
 */
public enum CalTimeTypeEnum {
    NOW_LAST_DAY("NOW_LAST_DAY","前一日日期"),
    SPECIFIED_DAY("SPECIFIED_DAY","指定日期"),
    ;

    private String key;
    private String value;

    private CalTimeTypeEnum(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return this.key;
    }

    public String getValue() {
        return this.value;
    }

    public static String getValue(String key) {
        for (CalTimeTypeEnum item : CalTimeTypeEnum.values()) {
            if (item.getKey().equals(key)) {
                return item.getValue();
            }
        }
        return null;
    }
}
