package com.spider.amazon.cusinterface;

import com.spider.amazon.cons.TemplateTypeEnum;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface TemplateDealDataType {
    TemplateTypeEnum value();
}
