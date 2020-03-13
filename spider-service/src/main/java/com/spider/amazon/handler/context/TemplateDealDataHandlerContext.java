package com.spider.amazon.handler.context;

import com.spider.amazon.cons.TemplateTypeEnum;
import com.spider.amazon.handler.abs.AbstractTemplateDealDataHandler;
import com.spider.amazon.utils.SpringContextUtils;

import java.util.Map;

/**
 * 文件模版数据处理
 */
public class TemplateDealDataHandlerContext {

    private Map<TemplateTypeEnum,Class> handlerMap;

    public TemplateDealDataHandlerContext(Map<TemplateTypeEnum, Class> handlerMap){
        this.handlerMap=handlerMap;
    }

    public AbstractTemplateDealDataHandler getInstance(TemplateTypeEnum typeEnum) {
        Class clazz = handlerMap.get(typeEnum);
        if (clazz == null) {
            throw new IllegalArgumentException("not found handler for type:"+typeEnum);
        }
        return (AbstractTemplateDealDataHandler) SpringContextUtils.getBean(clazz);
    }

}
