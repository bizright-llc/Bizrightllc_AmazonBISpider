package com.spider.amazon.cons;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 *
 * @ClassName AdNodeType
 * @Description 广告节点类型
 */
public enum AmazonAdNodeType {
    SEARCH_RESULT_AD("search_result_ad"),
    SEARCH_RESULT_INNER_AD("search_result_inner_ad"),
    SEARCH_RESULT_INNER_ITEM_AD("search_result_inner_item_ad"),
    DETAIL_AD("detail_ad"),
    DETAIL_AD2("detail_ad2"),
    BUYBOX_AD("buybox_ad"),
    RELATE_BRAND_AD("relate_brand_ad"),
    DETAIL_VERSION_AD("detail_version_ad"),
    SAME_PRODUCT_AD("same_product_ad");

    private String value;

    AmazonAdNodeType(String type){
        this.value = type;
    }

    @JsonValue
    public String getValue(){
        return value;
    }

    public static AmazonAdNodeType fromString(String type) {
        for (AmazonAdNodeType amazonadnodetype: AmazonAdNodeType.values()) {
            if (amazonadnodetype.getValue().toLowerCase().equals(type.toLowerCase())){
                return amazonadnodetype;
            }
        }

        return null;
    }

}
