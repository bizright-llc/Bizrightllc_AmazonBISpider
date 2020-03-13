package com.spider.amazon.cons;

/**
 * @ClassName BzyTaskParamEnum
 * @Description 八爪鱼任务参数枚举
 */
public enum BzyTaskParamEnum {
    URL(".Url","URL"),
    URLLIST(".UrlList","URL列表"),
    TEXT(".Text","Text"),
    TEXTLIST(".TextList","Text列表"),
    ;

    private String key;
    private String value;

    private BzyTaskParamEnum(String key, String value) {
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
        for (BzyTaskParamEnum item : BzyTaskParamEnum.values()) {
            if (item.getKey().equals(key)) {
                return item.getValue();
            }
        }
        return null;
    }
}
