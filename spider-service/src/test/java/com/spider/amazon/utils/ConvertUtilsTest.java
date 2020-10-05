package com.spider.amazon.utils;

import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ConvertUtilsTest {

    @Test
    public void testConvertMoneyNumberStringToFloat(){
        Float m = ConvertUtils.convertNumberStrToFloat("$0.00");

        assertEquals(0f,  m);
    }

    @Test
    public void testConvertPercentageNumberStringToFloat(){
        Float m = ConvertUtils.convertNumberStrToFloat("-12.34%");

        assertEquals(Float.valueOf("-12.34"),  m);
    }

    @Test
    public void testConvertOverThoudsandNumberStringToFloat(){
        Float m = ConvertUtils.convertNumberStrToFloat("1,122.34%");

        assertEquals(Float.valueOf("1122.34"),  m);
    }

}