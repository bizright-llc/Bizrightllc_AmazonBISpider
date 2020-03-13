package com.spider.amazon.cusinterface;

import com.spider.amazon.cons.UpdateBzyTaskRuleOperaTypeEnum;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface SpiderUpdType {
    UpdateBzyTaskRuleOperaTypeEnum value();
}
