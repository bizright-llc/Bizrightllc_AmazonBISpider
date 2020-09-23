package com.spider.amazon.utils;

import com.spider.SpiderServiceApplication;
import org.apache.commons.lang3.SystemUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openqa.selenium.WebDriver;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.*;

//@ExtendWith(SpringExtension.class)
//@SpringBootTest(classes= SpiderServiceApplication.class)
class WebDriverUtilsTest {

    @Test
    public void TestOS(){
        WebDriver driver = WebDriverUtils.getWebDriver("/Users/shaochinlin/Downloads/BZR-BI");

        driver.get("http://ipv4.download.thinkbroadband.com/50MB.zip");

    }
}