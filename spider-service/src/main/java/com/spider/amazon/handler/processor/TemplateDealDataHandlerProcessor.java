package com.spider.amazon.handler.processor;

import cn.hutool.core.lang.ClassScaner;
import com.google.common.collect.Maps;
import com.spider.amazon.cons.TemplateTypeEnum;
import com.spider.amazon.cusinterface.TemplateDealDataType;
import com.spider.amazon.handler.context.TemplateDealDataHandlerContext;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

@Component
public class TemplateDealDataHandlerProcessor implements BeanFactoryPostProcessor {
    private static final String HANDLER_PACKAGE="com.spider.amazon.handler";

    /**
     * 扫描 @TemplateTypeEnum ,初始化 TemplateDealDataHandlerContext,将其注册到spring容器
     * @param configurableListableBeanFactory
     * @throws BeansException
     */
    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory configurableListableBeanFactory) throws BeansException {
        Map<TemplateTypeEnum, Class> handlerMap = Maps.newHashMapWithExpectedSize(3);
        Set<Class<?>> handleSet = ClassScaner.scanPackageByAnnotation(HANDLER_PACKAGE,TemplateDealDataType.class);
        for (Class<?> clazz :handleSet){
            // 获取注解中的类型值
            TemplateTypeEnum type=clazz.getAnnotation(TemplateDealDataType.class).value();
            // 将注解中的类型值作为key，对应的类作为value，保存在map中
            handlerMap.put(type,clazz);
        }
        // 初始化
        TemplateDealDataHandlerContext context=new TemplateDealDataHandlerContext(handlerMap);
        configurableListableBeanFactory.registerSingleton(TemplateDealDataHandlerContext.class.getName(),context);
    }
}
