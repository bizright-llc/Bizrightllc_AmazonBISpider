package com.spider.amazon.utils;

import cn.hutool.core.util.ObjectUtil;
import com.spider.amazon.entity.Cookie;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.HashMap;
import java.util.List;

/**
 * driver工具集,目前仅支持chromedriver
 */
public class WebDriverUtils {

    public static WebDriver getWebDriver(String downloadPath) {

        HashMap<String, Object> chromePrefs = new HashMap<String, Object>();
        chromePrefs.put("profile.default_content_settings.popups", 0);
        chromePrefs.put("download.default_directory", downloadPath);
        ChromeOptions options = new ChromeOptions();
        options.setExperimentalOption("prefs", chromePrefs);
        DesiredCapabilities cap = DesiredCapabilities.chrome();
        cap.setCapability(CapabilityType.ACCEPT_SSL_CERTS, true);
        cap.setCapability(ChromeOptions.CAPABILITY, options);

        WebDriver driver = new ChromeDriver(cap);

        return driver;
    }

    /**
     *
     * @param driver
     */
    public static void waitForLoad(WebDriver driver) {
        ExpectedCondition<Boolean> pageLoadCondition = new
                ExpectedCondition<Boolean>() {
                    public Boolean apply(WebDriver driver) {
                        return ((JavascriptExecutor)driver).executeScript("return document.readyState").equals("complete");
                    }
                };
        WebDriverWait wait = new WebDriverWait(driver, 30);
        wait.until(pageLoadCondition);
    }

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
     *
     * @param element
     * @param xpath
     * @return
     */
    public static boolean isExistsElementFindByXpath(WebElement element, String xpath) {
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
     *
     * @param driver
     * @param locator
     * @param timeout
     * @return
     */
    public static boolean isExistsElementFindByXpath(WebDriver driver, By locator, int timeout) {
        try {
            if (ObjectUtil.isNotEmpty(WebDriverUtils.expWaitForElement(driver, locator, timeout))) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return false;
    }

    public static void addCookies(WebDriver driver, List<Cookie> listCookies) {
        if(driver == null){
            return;
        }

        for (Cookie cookie : listCookies) {
            // Havn't Know the reason cannot add these token
            if (!cookie.getName().equals("__Host-mons-selections") && !cookie.getName().equals("__Host-mselc")) {
                driver.manage().addCookie(new org.openqa.selenium.Cookie(cookie.getName(), cookie.getValue(), cookie.getDomain(),
                        cookie.getPath(), cookie.getExpiry(), cookie.getIsSecure(), cookie.getIsHttpOnly()));
            }
        }
    }

    /**
     * Element click helper
     *
     * @param element
     */
    public static void elementClick(WebElement element){
        if(element != null && element.isEnabled()){
            element.click();
        }
    }
}
