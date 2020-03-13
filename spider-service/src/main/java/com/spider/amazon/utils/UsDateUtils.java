package com.spider.amazon.utils;


import cn.hutool.core.date.DateUtil;
import com.common.exception.ServiceException;
import com.spider.amazon.cons.RespErrorEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * 国外的时间计算类
 */
@Component
@Slf4j
public class UsDateUtils {
    public static Date beginOfWeek(Date date) {
        int dayOfWeek= DateUtil.dayOfWeek(date);
        Date resultDate=null;
        if( dayOfWeek<=7 && dayOfWeek>=1) {
            resultDate=DateUtil.offsetDay(date,-(dayOfWeek-1));
        } else {
            throw new ServiceException(RespErrorEnum.SERVICE_DATA_EXPC.getSubStatusCode(),RespErrorEnum.SERVICE_DATA_EXPC.getSubStatusMsg());
        }
        return resultDate;
    }

    public static Date endOfWeek(Date date) {
        int dayOfWeek= DateUtil.dayOfWeek(date);
        Date resultDate=null;
        if( dayOfWeek<=7 && dayOfWeek>=1) {
            resultDate=DateUtil.offsetDay(date,-(dayOfWeek-7));
        } else {
            throw new ServiceException(RespErrorEnum.SERVICE_DATA_EXPC.getSubStatusCode(),RespErrorEnum.SERVICE_DATA_EXPC.getSubStatusMsg());
        }
        return resultDate;
    }
}
