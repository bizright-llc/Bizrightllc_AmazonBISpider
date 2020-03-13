package com.spider.amazon.cusinterface;

import com.spider.amazon.cons.GetDataOfTaskByOffsetOperaTypeEnum;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface SpiderGetDataType {
    GetDataOfTaskByOffsetOperaTypeEnum value();
}
