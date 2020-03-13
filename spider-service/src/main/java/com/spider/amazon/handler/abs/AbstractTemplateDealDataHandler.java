package com.spider.amazon.handler.abs;

import com.spider.amazon.cons.TemplateTypeEnum;

import java.util.Map;

public abstract class AbstractTemplateDealDataHandler {
    /**
     * 文件模版数据处理处理
     * @param typeEnum
     * @param name
     * @param value
     * @return
     */
    abstract public Map<String,Object> templateDataDeal(TemplateTypeEnum typeEnum,String name,Object value,Map<String,Object> concludeMap);
}
