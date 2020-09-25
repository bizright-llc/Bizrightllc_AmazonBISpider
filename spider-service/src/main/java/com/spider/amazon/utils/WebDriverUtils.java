package com.spider.amazon.utils;

import cn.hutool.core.util.ObjectUtil;
import com.spider.amazon.entity.Cookie;
import com.spider.amazon.service.MailService;
import com.spider.amazon.service.impl.MailServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
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
import java.util.Set;

import static java.lang.Thread.sleep;

/**
 * driver工具集,目前仅支持chromedriver
 */
@Slf4j
public class WebDriverUtils {

    public static WebDriver getWebDriver(String downloadPath) {

        HashMap<String, Object> chromePrefs = new HashMap<String, Object>();
        chromePrefs.put("profile.default_content_settings.popups", 0);
        chromePrefs.put("download.default_directory", downloadPath);

        ChromeOptions options = new ChromeOptions();
        options.setExperimentalOption("prefs", chromePrefs);
        options.addArguments("--headless", "--disable-gpu", "--window-size=1920,1200","--ignore-certificate-errors", "--silent");
        DesiredCapabilities cap = DesiredCapabilities.chrome();
        cap.setCapability(CapabilityType.ACCEPT_SSL_CERTS, true);
        cap.setCapability(ChromeOptions.CAPABILITY, options);

        WebDriver driver = new ChromeDriver(cap);

        return driver;
    }

    public static WebDriver getBackgroudWebDriver(){
        ChromeOptions options = new ChromeOptions();
        // driver work at background
        options.addArguments("--headless", "--disable-gpu", "--window-size=1920,1200","--ignore-certificate-errors", "--silent");
        WebDriver driver = new ChromeDriver(options);

        return driver;
    }

    public static WebDriver getWebDriver(){
        WebDriver driver = new ChromeDriver();
        return driver;
    }

    /**
     * Get web driver
     * @param background the web driver wont show the window
     * @param downloadPath the download file path
     * @return
     */
    public static WebDriver getWebDriver(String driverPath, String downloadPath, boolean background){

        ChromeOptions options = new ChromeOptions();

        if(StringUtils.isNotEmpty(downloadPath)){
            HashMap<String, Object> chromePrefs = new HashMap<String, Object>();
            chromePrefs.put("profile.default_content_settings.popups", 0);
            chromePrefs.put("download.default_directory", downloadPath);

            options.setExperimentalOption("prefs", chromePrefs);
        }

        if(background){
            // driver work at background
            options.addArguments("--headless", "--disable-gpu", "--window-size=1920,1200","--ignore-certificate-errors", "--silent");
        }

        System.setProperty("webdriver.chrome.driver", driverPath);

        WebDriver driver = new ChromeDriver(options);

        return driver;
    }

    /**
     * Get web driver
     * @param background the web driver wont show the window
     * @param downloadPath the download file path
     * @return
     */
    public static WebDriver getWebDriver(String downloadPath, boolean background){

        ChromeOptions options = new ChromeOptions();

        if(StringUtils.isNotEmpty(downloadPath)){
            HashMap<String, Object> chromePrefs = new HashMap<String, Object>();
            chromePrefs.put("profile.default_content_settings.popups", 0);
            chromePrefs.put("download.default_directory", downloadPath);

            options.setExperimentalOption("prefs", chromePrefs);
        }

        if(background){
            // driver work at background
            options.addArguments("--headless", "--disable-gpu", "--window-size=1920,1200","--ignore-certificate-errors", "--silent");
        }

        WebDriver driver = new ChromeDriver(options);

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

    public static void waitAlert(WebDriver driver, int timeout){
        try {
//            System.out.println(timeout + "秒之后出现");
            WebDriverWait wait = new WebDriverWait(driver, timeout);

            wait.until(ExpectedConditions.alertIsPresent());

        } catch (Exception e) {
            e.printStackTrace();
            log.info("[Alert wait {} seconds not present]", timeout, e);
            throw e;
        }
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

    public static List<WebElement> expWaitForElements(WebDriver driver, By locator, int timeout) {
        WebElement element = null;
        List<WebElement> elements = null;
        try {
//            System.out.println(timeout + "秒之后出现");
            WebDriverWait wait = new WebDriverWait(driver, timeout);
            element = wait.until(ExpectedConditions
                    .visibilityOfElementLocated(locator));
            elements = driver.findElements(locator);
//            System.out.println("元素出现了");
        } catch (Exception e) {
//            System.out.println("元素不存在");
            e.printStackTrace();
            return null;
        }
        return elements;
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
            if(cookie.getDomain() == null){
                driver.manage().addCookie(new org.openqa.selenium.Cookie(cookie.getName(), cookie.getValue()));
            }else{
                driver.manage().addCookie(new org.openqa.selenium.Cookie(cookie.getName(), cookie.getValue(), cookie.getDomain(),
                        cookie.getPath(), cookie.getExpiry(), cookie.getIsSecure(), cookie.getIsHttpOnly()));
            }
        }
    }

    public static void addSeleniumCookies(WebDriver driver, List<org.openqa.selenium.Cookie> listCookies) {
        if(driver == null){
            return;
        }

        for (org.openqa.selenium.Cookie cookie : listCookies) {
            // Haven't Know the reason cannot add these token
            driver.manage().addCookie(cookie);
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

    /**
     * Re login to get the latest cookies for amazon vendor central
     * @param driver
     */
    public static void getAmazonVCCookies(WebDriver driver){

        try{

            driver.get("https://vendorcentral.amazon.com/hz/vendor/members/support/hub");

            WebElement signInBtnEle = expWaitForElement(driver, By.id("login-button-container"), 5);
            elementClick(signInBtnEle);

            WebElement emailInputEle = expWaitForElement(driver, By.name("email"), 5);
            emailInputEle.sendKeys("james.l@bzrthinc.com");

            WebElement passwordInputEle = expWaitForElement(driver, By.name("password"), 5);
            passwordInputEle.sendKeys("Lovebizright");

            WebElement rememberMeCheckBoxEle = expWaitForElement(driver, By.name("rememberMe"), 5);
            elementClick(rememberMeCheckBoxEle);

            WebElement loginBtnEle = expWaitForElement(driver, By.id("signInSubmit"), 5);
            elementClick(loginBtnEle);

            sleep(1000);

            WebElement authSendCodeBtnEle = expWaitForElement(driver, By.id("auth-send-code"), 5);

            if (authSendCodeBtnEle != null)
            {
                elementClick(authSendCodeBtnEle);
            }

            MailService mailService = new MailServiceImpl();
            mailService.login("bizright.spider@gmail.com", "Lovebizright");

            String otpCode = "";
            for (int i=0; i<5; i++){
                otpCode = mailService.getLastAmazonVCOTP();
                if(StringUtils.isNotEmpty(otpCode)){
                    break;
                }
            }

            WebElement otpCodeInputEle = expWaitForElement(driver, By.id("auth-mfa-otpcode"), 5);
            otpCodeInputEle.sendKeys(otpCode);

            WebElement otpRememberCheckBoxEle = expWaitForElement(driver, By.id("auth-mfa-remember-device"), 5);
            elementClick(otpRememberCheckBoxEle);

            WebElement otpSignInBtnEle = expWaitForElement(driver, By.id("auth-signin-button"), 5);
            elementClick(otpSignInBtnEle);

        }catch (Exception ex){
            log.info("[getAmazonVCCookies] throw exception");
            log.info(ex.getLocalizedMessage());
        }

    }

    /**
     * Check web driver amazon vendor central cookies valid or not
     *
     * @param driver
     * @return
     */
    public static boolean checkAmazonVCCookiesValid(WebDriver driver) throws InterruptedException {

        log.info("[checkAmazonVCCookiesValid] check web driver amazon vc cookies");

        if(driver == null){
            throw new IllegalArgumentException("Web driver cannot be null");
        }

        try{
            driver.navigate().to("https://vendorcentral.amazon.com/hz/vendor/members/home/check");

            sleep(1000);

            WebElement logoutBtnEle = expWaitForElement(driver, By.id("logout_topRightNav"), 5);

            if(logoutBtnEle != null){

                WebElement regionSpanEle = expWaitForElement(driver, By.id("account-region_topRightNav"), 5);

                if (regionSpanEle != null)
                {
                    String text = regionSpanEle.getText();
                    if (text.contains("US"))
                    {
                        return true;
                    }
                }

            }
            return false;
        }catch (Exception ex){
            return false;
        }

    }

}
