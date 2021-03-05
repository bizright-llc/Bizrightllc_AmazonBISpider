package com.spider.amazon.utils;

import cn.hutool.core.util.ObjectUtil;
import com.spider.amazon.dto.ProxyDTO;
import com.spider.amazon.entity.Cookie;
import com.spider.amazon.model.ProxyDO;
import com.spider.amazon.remote.api.SpiderUrl;
import com.spider.amazon.service.MailService;
import com.spider.amazon.service.impl.MailServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriverService;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

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
     * Get web driver
     *
     * @param driverPath
     * @param downloadPath
     * @param background
     * @return
     */
    public static WebDriver getWebDriverWithProxy(String driverPath, String downloadPath, String proxyFilepath, boolean background){
        ChromeOptions options = new ChromeOptions();

        File proxyFile = new File(proxyFilepath);

        if(!proxyFile.exists()){
            throw new IllegalArgumentException(String.format("Proxy filepath %s not exist, cannot create proxy webdriver.", proxyFilepath));
        }

        if(StringUtils.isNotEmpty(downloadPath)){
            HashMap<String, Object> chromePrefs = new HashMap<String, Object>();
            chromePrefs.put("profile.default_content_settings.popups", 0);
            chromePrefs.put("download.default_directory", downloadPath);

            options.setExperimentalOption("prefs", chromePrefs);
        }

        options.addExtensions(new File(proxyFilepath));

        if(background){
            // driver work at background
            options.addArguments("--headless", "--disable-gpu", "--window-size=1920,1200","--ignore-certificate-errors", "--silent");
        }

        System.setProperty("webdriver.chrome.driver", driverPath);

        RemoteWebDriver driver = new ChromeDriver(options);

        return driver;
    }

    /**
     * Get web driver with proxy setting
     * cannot use proxy with auth
     *
     * @param driverPath
     * @param downloadPath
     * @param proxy
     * @param background
     * @return
     */
    public static WebDriver getWebDriverWithProxy(String driverPath, String downloadPath, ProxyDTO proxy, boolean background){
        ChromeOptions options = new ChromeOptions();

        if(StringUtils.isNotEmpty(downloadPath)){
            HashMap<String, Object> chromePrefs = new HashMap<String, Object>();
            chromePrefs.put("profile.default_content_settings.popups", 0);
            chromePrefs.put("download.default_directory", downloadPath);

            options.setExperimentalOption("prefs", chromePrefs);
        }

        if (StringUtils.isNotEmpty(proxy.getUsername())){
            throw new IllegalArgumentException("Cannot set driver with proxy need auth");
        }

        if(proxy != null && StringUtils.isNotEmpty(proxy.getIp())){
            options.addArguments(String.format("--proxy-server=%s:%s", proxy.getIp(), proxy.getPort()));
        }else{
            throw new IllegalArgumentException("proxy cannot be null");
        }

        if(background){
            // driver work at background
            options.addArguments("--headless", "--disable-gpu", "--window-size=1920,1200","--ignore-certificate-errors", "--silent");
        }

        System.setProperty("webdriver.chrome.driver", driverPath);

        RemoteWebDriver driver = new ChromeDriver(options);

        return driver;
    }

    /**
     * This method help you debug, find the element you are processing
     *
     * @param driver
     * @param element
     * @throws InterruptedException
     */
    public static void highlight(WebDriver driver, WebElement element) throws InterruptedException {
        JavascriptExecutor executor = (JavascriptExecutor) driver;

        String originStyle = element.getAttribute("style");
        int count = 0;
        String[] colors = new String[]{"yellow", "red"};

        String background = "";
        String border = "";
        while(count < 5){
            if(count++ % 2 == 0){
                background = colors[0];
                border = colors[1];
            }else{
                background = colors[1];
                border = colors[0];
            }
            String style = String.format("background: %s; border: 2px solid %s;", background, border);
            executor.executeScript("arguments[0].setAttribute('style', arguments[1]);", element, style);
            Thread.sleep(2000);
        }

        executor.executeScript("arguments[0].setAttribute('style', arguments[1]);", element, originStyle);
    }

    /**
     * Set the browser scale
     * @param driver
     * @param zoom
     */
    public static void zoomBrowser(WebDriver driver, int zoom){

        if (zoom < 50 || zoom > 150){
            zoom = 90;
        }

        try{
            JavascriptExecutor js = (JavascriptExecutor) driver;

//                Below zoom in code is working for ChromeDriver but not for FirefoxDriver
            js.executeScript(String.format("document.body.style.zoom='%s%'", zoom));

            randomSleep();
        }catch (Exception ex){
            log.debug("[zoomBrowser] {} failed", zoom, ex);
        }
    }

    /**
     * Get firefox browser driver
     *
     * @param driverPath
     * @param downloadPath
     * @param proxy
     * @param backgroud
     * @return
     */
    public static WebDriver getFirefoxDriver(String driverPath, String downloadPath, ProxyDO proxy, boolean backgroud){

        System.setProperty("webdriver.gecko.driver", driverPath);

        FirefoxOptions options = new FirefoxOptions();

        if (proxy != null){
            FirefoxProfile profile = new FirefoxProfile();
            // use proxy
            profile.setPreference("network.proxy.type", 1);
            // proxy setting
            profile.setPreference("network.proxy.http", proxy.getIp());
            profile.setPreference("network.proxy.http_port", proxy.getPort());

            profile.setPreference("network.proxy.ssl", proxy.getIp());
            profile.setPreference("network.proxy.ssl_port", proxy.getPort());

            profile.setPreference("username", proxy.getUsername());
            profile.setPreference("password", proxy.getPassword());

            // use same proxy setting
            profile.setPreference("network.proxy.share_proxy_settings", true);

            // dont use proxy on localhost
            profile.setPreference("network.proxy.no_proxies_on", "localhost");

            options.setCapability("firefox_profile", profile);

        }

        options.addArguments("--no-sandbox");

        // open firefox driver
        FirefoxDriver driver = new FirefoxDriver(options);

        return driver;
    }

    public static WebDriver getPhantomJSDriver(String driverPath, String downloadPath, ProxyDO proxy, boolean backgroud){

        System.setProperty("phantomjs.binary.path", driverPath);

        WebDriver driver = null;
        ArrayList cliArgsCap = new ArrayList();

        cliArgsCap.add("--proxy="+String.format("%s:%s",proxy.getIp(),proxy.getPort()));
        cliArgsCap.add("--proxy-auth=" + String.format("%s:%s", proxy.getUsername(), proxy.getPassword()));
        cliArgsCap.add("--proxy-type=http");

        DesiredCapabilities capabilities = DesiredCapabilities.phantomjs();

        capabilities.setCapability(PhantomJSDriverService.PHANTOMJS_CLI_ARGS, cliArgsCap);

        driver = new PhantomJSDriver(capabilities);
        driver.manage().timeouts().implicitlyWait(5L, TimeUnit.SECONDS);
        driver.manage().window().maximize();

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
            log.debug("[expWaitForElement] expect element {} for {} second", locator.toString(), timeout);
            WebDriverWait wait = new WebDriverWait(driver, timeout);
            element = wait.until(ExpectedConditions
                    .visibilityOfElementLocated(locator));
            log.debug("[expWaitForElement] element {} exist", locator.toString());
//            System.out.println("元素出现了");
        } catch (Exception e) {
//            System.out.println("元素不存在");
            log.debug("[expWaitForElement] element {} not exist", locator.toString());
            return null;
        }
        return element;
    }

    public static List<WebElement> expWaitForElements(WebDriver driver, By locator, int timeout) {
        WebElement element = null;
        List<WebElement> elements = null;
        log.debug("[expWaitForElements] expect elements {}", locator.toString());
        try {
            WebDriverWait wait = new WebDriverWait(driver, timeout);
            elements = driver.findElements(locator);
            log.debug("[expWaitForElements] elements {} exist, {} elements", locator, elements.size());
        } catch (Exception e) {
//            System.out.println("元素不存在");
//            e.printStackTrace();
            log.debug("[expWaitForElements] elements {} not exist", locator);
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

        log.debug("[isExistsElementFindByXpath] check element {} exist or not", locator.toString());

        try {
            if (ObjectUtil.isNotEmpty(WebDriverUtils.expWaitForElement(driver, locator, timeout))) {
                log.debug("[isExistsElementFindByXpath] {} exist", locator.toString());
                return true;
            }
        } catch (Exception e) {
//            e.printStackTrace();
            log.debug("[isExistsElementFindByXpath] {} not found", locator.toString());
            return false;
        }
        return false;
    }

    public static void addCookies(WebDriver driver, List<Cookie> listCookies) {
        if(driver == null){
            throw new IllegalArgumentException("Cannot add cookies to null driver");
        }

        for (Cookie cookie : listCookies) {
            driver.manage().addCookie(new org.openqa.selenium.Cookie(cookie.getName(), cookie.getValue()));
        }
    }

    public static void addSeleniumCookies(WebDriver driver, List<org.openqa.selenium.Cookie> listCookies) {
        if(driver == null){
            throw new IllegalArgumentException("Cannot add cookies to null driver");
        }
        JavascriptExecutor javascript = (JavascriptExecutor) driver;
        String DomainUsingJS=(String)javascript.executeScript("return document.domain");

        for (org.openqa.selenium.Cookie cookie : listCookies) {
            // Haven't Know the reason cannot add these token
            try{
                driver.manage().addCookie(cookie);
            }catch (Exception ex){
                log.error("[WebDriverUtils][addSeleniumCookies]", ex);
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

    /**
     * Click element
     *
     * @param driver
     * @param element
     * @return
     */
    public static boolean isClicked(WebDriver driver, WebElement element){
        try {
            WebDriverWait wait = new WebDriverWait(driver, 10);
            wait.until(ExpectedConditions.elementToBeClickable(element));

            // move to the element
            Actions builder = new Actions(driver);
            builder.moveToElement(element).build().perform();

            randomSleepBetween(2000, 5000);

            String currentUrl = driver.getCurrentUrl();

            element.click();

            randomSleepBetween(2000, 5000);

            // click not working
            if(driver.getCurrentUrl().equals(currentUrl)){
                JavascriptExecutor executor = (JavascriptExecutor) driver;
                executor.executeScript("arguments[0].click()", element);

                randomSleepBetween(2000, 5000);
                if(driver.getCurrentUrl().equals(currentUrl)){
                    return false;
                }
            }

            return true;
        } catch(Exception e){
            log.debug("[isClicked] failed", e);
            return false;
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

    public static void getAmazonSCCookies(WebDriver driver){

        String loginBtnXPath = "//*[@id=\"wp-content\"]/div[1]/div/div/div/div[2]/div/div[2]/div[1]/div[1]/div";

        try{

            driver.manage().deleteAllCookies();

            driver.navigate().to(SpiderUrl.SPIDER_SC_INDEX);

            WebElement signInBtnEle = expWaitForElement(driver, By.xpath(loginBtnXPath), 5);
            elementClick(signInBtnEle);

            WebElement emailInputEle = expWaitForElement(driver, By.name("email"), 5);
            emailInputEle.sendKeys("james.l@bzrthinc.com");

            randomSleep();

            WebElement passwordInputEle = expWaitForElement(driver, By.name("password"), 5);
            passwordInputEle.sendKeys("Lovebizright");

            randomSleep();

            WebElement rememberMeCheckBoxEle = expWaitForElement(driver, By.name("rememberMe"), 5);
            elementClick(rememberMeCheckBoxEle);

            randomSleep();

            WebElement loginBtnEle = expWaitForElement(driver, By.id("signInSubmit"), 5);
            elementClick(loginBtnEle);

            randomSleep();

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

            randomSleep();

            WebElement otpRememberCheckBoxEle = expWaitForElement(driver, By.id("auth-mfa-remember-device"), 5);
            elementClick(otpRememberCheckBoxEle);

            randomSleep();

            WebElement otpSignInBtnEle = expWaitForElement(driver, By.id("auth-signin-button"), 5);
            elementClick(otpSignInBtnEle);

            randomSleep();

        }catch (Exception ex){
            log.info("[getAmazonVCCookies] throw exception");
            log.info(ex.getLocalizedMessage());
        }
    }

    /**
     * Click element and wait
     * @param element
     * @throws InterruptedException
     */
    public void elementClickAndWait(WebElement element) throws InterruptedException {
        elementClick(element);

        randomSleep();
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
            driver.navigate().to(SpiderUrl.AMAZON_VC_404);

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

    /**
     * Check web driver amazon seller central cookies valid or not
     *
     * @param driver
     * @return
     */
    public static boolean checkAmazonSCCookiesValid(WebDriver driver) throws InterruptedException {

        log.info("[checkAmazonSCCookiesValid] check web driver amazon sc cookies");

        if(driver == null){
            throw new IllegalArgumentException("Web driver cannot be null");
        }

        try{
            driver.navigate().to(SpiderUrl.AMAZON_SC_CHECK);

            sleep(1000);

            WebElement regionFlag = expWaitForElement(driver, By.xpath("//*[@id=\"sc-mkt-switcher-form\"]/img"), 10);

            WebElement settingMenuEle = expWaitForElement(driver, By.id("sc-quicklink-settings"), 10);

            if(regionFlag != null && settingMenuEle != null){
                return true;
            }

            return false;
        }catch (Exception ex){
            return false;
        }
    }

    public static void randomSleep() throws InterruptedException {
        Random rand = new Random();

        int low = 500;
        int high = 1500;

        randomSleepBetween(low, high);
    }

    public static void randomSleepBetween(int l, int h) throws InterruptedException {

        Random rand = new Random();

        int low = Math.min(l,h);
        int high = Math.max(l,h);

        int random = rand.nextInt(high - low) + low;
        sleep(random);
    }

    public static void addAmazonVCCookies(WebDriver driver, List<org.openqa.selenium.Cookie> cookies) throws InterruptedException {
        if (driver == null){
            throw new IllegalArgumentException("Add amazon cookies driver cannot be null");
        }

        driver.navigate().to(SpiderUrl.AMAZON_VC_404);

        sleep(1000);

        addSeleniumCookies(driver, cookies);
    }

    public static void waitElementClickable(WebDriver driver, By locator, int timeout){

        if (driver == null){
            throw new IllegalArgumentException("[Wait element clickable cannot pass null driver]");
        }

        if(locator == null){
            throw new IllegalArgumentException("[Wait element clickable cannot have null locator]");
        }

        WebDriverWait wait = new WebDriverWait(driver, timeout);

        wait.until(ExpectedConditions.elementToBeClickable(locator));
    }

}
