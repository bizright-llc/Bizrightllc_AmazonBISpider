package com.spider.amazon.handler.context;

import com.spider.amazon.cons.GetDataOfTaskByOffsetOperaTypeEnum;
import com.spider.amazon.handler.abs.AbstractGetDataByOffsetHandler;
import com.spider.amazon.utils.SpringContextUtils;

import java.util.Map;

/**
 * 偏移量获取八爪鱼数据数据
 */
public class GetSpiderDataHandlerContext {

    private Map<GetDataOfTaskByOffsetOperaTypeEnum,Class> handlerMap;

    public GetSpiderDataHandlerContext(Map<GetDataOfTaskByOffsetOperaTypeEnum, Class> handlerMap){
        this.handlerMap=handlerMap;
    }

    public AbstractGetDataByOffsetHandler getInstance(GetDataOfTaskByOffsetOperaTypeEnum typeEnum) {
        Class clazz = handlerMap.get(typeEnum);
        if (clazz == null) {
            throw new IllegalArgumentException("not found handler for type:"+typeEnum);
        }
        return (AbstractGetDataByOffsetHandler) SpringContextUtils.getBean(clazz);
    }

}
