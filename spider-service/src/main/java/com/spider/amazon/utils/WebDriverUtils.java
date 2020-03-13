package com.spider.amazon.utils;

import cn.hutool.core.util.ObjectUtil;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * driver工具集,目前仅支持chromedriver
 */
public class WebDriverUtils {
    /**
     * 超时等待元素时间,看元素是否出现
     *
     * @param driver
     * @param locator
     * @param timeout
     * @return
     */
    public static WebElement expWaitForElement(WebDriver driver, By locator, int timeout) {
        WebElement element = null;
        try {
//            System.out.println(timeout + "秒之后出现");
            WebDriverWait wait = new WebDriverWait(driver, timeout);
            element = wait.until(ExpectedConditions
                    .visibilityOfElementLocated(locator));
//            System.out.println("元素出现了");
        } catch (Exception e) {
//            System.out.println("元素不存在");
            e.printStackTrace();
            return null;
        }
        return element;
    }

    /**
     * 在父元素查找元素是否存在
     * @param element
     * @param xpath
     * @return
     */
    public static boolean isExistsElementFindByXpath(WebElement element,String xpath) {
        try {
            if (ObjectUtil.isNotEmpty(element.findElement(By.xpath(xpath)))) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return false;
    }

    /**
     * 在父元素查找元素是否存在(通过driver来找)
     * @param driver
     * @param locator
     * @param timeout
     * @return
     */
    public static boolean isExistsElementFindByXpath(WebDriver driver,By locator, int timeout) {
        try {
            if (ObjectUtil.isNotEmpty(WebDriverUtils.expWaitForElement(driver,locator,timeout))) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return false;
    }
}
