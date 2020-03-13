package com.spider.amazon.handler.processor;

import cn.hutool.core.lang.ClassScaner;
import com.google.common.collect.Maps;
import com.spider.amazon.cons.GetDataOfTaskByOffsetOperaTypeEnum;
import com.spider.amazon.cusinterface.SpiderGetDataType;
import com.spider.amazon.handler.context.GetSpiderDataHandlerContext;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

@Component
public class GetSpiderDataHandlerProcessor implements BeanFactoryPostProcessor {
    private static final String HANDLER_PACKAGE="com.spider.amazon.handler";

    /**
     * 扫描 @SpiderGetDataType ,初始化 GetSpiderDataHandlerContext,将其注册到spring容器
     * @param configurableListableBeanFactory
     * @throws BeansException
     */
    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory configurableListableBeanFactory) throws BeansException {
        Map<GetDataOfTaskByOffsetOperaTypeEnum, Class> handlerMap = Maps.newHashMapWithExpectedSize(3);
        Set<Class<?>> handleSet = ClassScaner.scanPackageByAnnotation(HANDLER_PACKAGE,SpiderGetDataType.class);
        for (Class<?> clazz :handleSet){
            // 获取注解中的类型值
            GetDataOfTaskByOffsetOperaTypeEnum type=clazz.getAnnotation(SpiderGetDataType.class).value();
            // 将注解中的类型值作为key，对应的类作为value，保存在map中
            handlerMap.put(type,clazz);
        }
        // 初始化
        GetSpiderDataHandlerContext context=new GetSpiderDataHandlerContext(handlerMap);
        configurableListableBeanFactory.registerSingleton(GetSpiderDataHandlerContext.class.getName(),context);
    }
}
