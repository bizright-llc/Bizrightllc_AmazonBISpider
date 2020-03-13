package com.spider.amazon.handler.processor;

import cn.hutool.core.lang.ClassScaner;
import com.google.common.collect.Maps;
import com.spider.amazon.cons.UpdateBzyTaskRuleOperaTypeEnum;
import com.spider.amazon.cusinterface.SpiderUpdType;
import com.spider.amazon.handler.context.UpdSpiderHandlerContext;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

@Component
public class UpdSpiderHandlerProcessor implements BeanFactoryPostProcessor {
    private static final String HANDLER_PACKAGE="com.spider.amazon.handler";

    /**
     * 扫描 @SpiderUpdType ,初始化 UpdSpiderHandlerContext,将其注册到spring容器
     * @param configurableListableBeanFactory
     * @throws BeansException
     */
    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory configurableListableBeanFactory) throws BeansException {
        Map<UpdateBzyTaskRuleOperaTypeEnum,Class> handlerMap = Maps.newHashMapWithExpectedSize(3);
        Set<Class<?>> handleSet = ClassScaner.scanPackageByAnnotation(HANDLER_PACKAGE,SpiderUpdType.class);
        for (Class<?> clazz :handleSet){
            // 获取注解中的类型值
            UpdateBzyTaskRuleOperaTypeEnum type=clazz.getAnnotation(SpiderUpdType.class).value();
            // 将注解中的类型值作为key，对应的类作为value，保存在map中
            handlerMap.put(type,clazz);
        }
        // 初始化
        UpdSpiderHandlerContext context=new UpdSpiderHandlerContext(handlerMap);
        configurableListableBeanFactory.registerSingleton(UpdSpiderHandlerContext.class.getName(),context);
    }
}
