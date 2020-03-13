package com.spider.amazon.handler.context;

import com.spider.amazon.cons.UpdateBzyTaskRuleOperaTypeEnum;
import com.spider.amazon.handler.abs.AbstractUpdSpiderHandler;
import com.spider.amazon.utils.SpringContextUtils;

import java.util.Map;

/**
 * 更新八爪鱼爬虫任务处理内容类
 */
public class UpdSpiderHandlerContext {

    private Map<UpdateBzyTaskRuleOperaTypeEnum,Class> handlerMap;

    public UpdSpiderHandlerContext(Map<UpdateBzyTaskRuleOperaTypeEnum,Class> handlerMap){
        this.handlerMap=handlerMap;
    }

    public AbstractUpdSpiderHandler getInstance(UpdateBzyTaskRuleOperaTypeEnum typeEnum) {
        Class clazz = handlerMap.get(typeEnum);
        if (clazz == null) {
            throw new IllegalArgumentException("not found handler for type:"+typeEnum);
        }
        return (AbstractUpdSpiderHandler) SpringContextUtils.getBean(clazz);
    }

}
