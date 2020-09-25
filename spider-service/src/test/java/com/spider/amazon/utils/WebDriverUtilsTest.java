package com.spider.amazon.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spider.SpiderServiceApplication;
import com.spider.amazon.config.SpiderConfig;
import com.spider.amazon.entity.Cookie;
import com.spider.amazon.model.Consts;
import com.spider.amazon.remote.api.SpiderUrl;
import com.spider.amazon.service.CommonSettingService;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static java.lang.Thread.sleep;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = SpiderServiceApplication.class)
class WebDriverUtilsTest {

    Logger log = LoggerFactory.getLogger(WebDriverUtilsTest.class);

    @Autowired
    private SpiderConfig spiderConfig;

    @Autowired
    private CommonSettingService commonSettingService;

    private String dateStartPickerXPath = "//*[@id=\"dashboard-filter-periodPicker\"]/div/div/div[1]/input";
    private String dateEndPickerXPath = "//*[@id=\"dashboard-filter-periodPicker\"]/div/div/div[3]/input";

    private String datePickerPreviousMonthXPath = "/html/body/div[4]/div/a[1]";
    private String datePickerLeftMonthXPath = "/html/body/div[4]/div/div[2]/div[1]/div[1]";

    //day element xpath format
    private String datePickerLeftDayXPathFormat = "/html/body/div[4]/div/div[2]/div[2]//div[(@aria-label='day-%d')]";

    @Test
    public void TestOS() {
        WebDriver driver = WebDriverUtils.getWebDriver("/Users/shaochinlin/Downloads/BZR-BI");

        driver.get("http://ipv4.download.thinkbroadband.com/50MB.zip");
    }

    /**
     * Download all history inventory health sourcing file
     */
    @Test
    public void downloadAmazonVCInventoryHealthSourcingFile() {

        // 1.建立WebDriver
        System.setProperty("webdriver.chrome.driver", spiderConfig.getChromeDriverPath());

        String downloadFilePath = spiderConfig.getDownloadPath();

        ObjectMapper objectMapper = new ObjectMapper();

        try {

            LocalDate lastDate = LocalDate.of(2019, 9, 1);
            LocalDate preDate = LocalDate.of(2020, 8, 30);
            ;
            LocalDate currentDate = preDate.minusDays(1);
            boolean apply = false;

            while (!lastDate.isEqual(currentDate) && lastDate.compareTo(currentDate) < 0) {
                WebDriver driver = WebDriverUtils.getWebDriver(downloadFilePath, false);
                try {

                    WebDriverWait wait = new WebDriverWait(driver, 60);

                    // 2.初始打开页面
                    driver.manage().timeouts().pageLoadTimeout(20, TimeUnit.SECONDS); // 页面加载超时时间
                    driver.get(SpiderUrl.AMAZON_VC_INDEX);

                    // 3.Set cookie
                    driver.manage().deleteAllCookies();

                    List<Cookie> cookies = commonSettingService.getAmazonVCCookies();

                    List<org.openqa.selenium.Cookie> savedCookies = CookiesUtils.cookiesToSeleniumCookies(cookies);

                    WebDriverUtils.addSeleniumCookies(driver, savedCookies);

                    // cookies are not valid
                    if (!WebDriverUtils.checkAmazonVCCookiesValid(driver)) {
                        driver.manage().deleteAllCookies();
                        WebDriverUtils.getAmazonVCCookies(driver);

                        List<Cookie> driverCookies = CookiesUtils.seleniumCookieToCookie(driver.manage().getCookies());

                        String newCookiesStr = objectMapper.writeValueAsString(driverCookies);

                        commonSettingService.setValue(Consts.AMAZON_VC_COOKIES, newCookiesStr, "system");
                    }

                    navigateToInventoryHealthPage(driver, wait);

                    //4.1点击日期选择按钮
                    WebElement reportingRangeButtonElement = WebDriverUtils.expWaitForElement(driver, By.xpath("//*[@id='dashboard-filter-reportingRange']//awsui-button-dropdown//button[1]"), 10);
                    if (log.isInfoEnabled()) {
                        log.info("1.step105=>reportingRangeButtonElement:" + reportingRangeButtonElement.toString());
                    }
                    WebDriverUtils.elementClick(reportingRangeButtonElement);

                    //4.2点击选择daily
                    driver.manage().timeouts().pageLoadTimeout(7, TimeUnit.SECONDS); // 页面加载超时时间
                    if (log.isInfoEnabled()) {
                        log.info("1.1.step137=>点击选择daily");
                    }

                    WebElement dailySelectElement = WebDriverUtils.expWaitForElement(driver, By.xpath("//*[@id=\"dashboard-filter-reportingRange\"]/div/awsui-button-dropdown/div/div/ul/li[1]"), 10);
                    if (log.isInfoEnabled() && dailySelectElement != null) {
                        log.info("2.step112=>dailySelectElement:" + dailySelectElement.toString());
                    }
                    WebDriverUtils.elementClick(dailySelectElement);

                    // 4.21点击DistributeView View
                    WebElement distributeViewViewButtonElement = WebDriverUtils.expWaitForElement(driver, By.xpath("//*[@id='dashboard-filter-distributorView']//awsui-button-dropdown//button"), 10);
                    if (log.isInfoEnabled() && distributeViewViewButtonElement != null) {
                        log.info("1.step105=>distributeViewViewButtonElement:" + distributeViewViewButtonElement.toString());
                    }
                    WebDriverUtils.elementClick(distributeViewViewButtonElement);

                    // 4.22点击选择 Sourcing View
                    if (log.isInfoEnabled()) {
                        log.info("1.1.step137=>点击选择 Sourcing View");
                    }
                    WebElement distributeViewSelectElement = WebDriverUtils.expWaitForElement(driver, By.xpath("//*[@id=\"dashboard-filter-distributorView\"]/div/awsui-button-dropdown/div/div/ul/li[2]"), 10);
                    if (log.isInfoEnabled() && distributeViewSelectElement != null) {
                        log.info("2.step112=>distributeViewSelectElement:" + distributeViewSelectElement.toString());
                    }
                    WebDriverUtils.elementClick(distributeViewSelectElement);

//                    //4.3点击应用按钮
//                    sleep(7000);
//                    driver.manage().timeouts().pageLoadTimeout(7, TimeUnit.SECONDS); // 页面加载超时时间
//                    WebElement applyElement = WebDriverUtils.expWaitForElement(driver, By.xpath("//*[@id='dashboard-filter-applyCancel']/div/awsui-button[2]/button"), 10);
//                    WebDriverUtils.elementClick(applyElement);
//
//                    sleep(5000);
                    // all set
                    // select date

                    // click date picker end date
                    WebElement dateEndPicker = WebDriverUtils.expWaitForElement(driver, By.xpath(dateEndPickerXPath), 60);
                    WebDriverUtils.expWaitForElement(driver, By.xpath(dateEndPickerXPath), 60);
                    if (dateEndPicker != null) {
                        dateEndPicker = wait.until(ExpectedConditions.elementToBeClickable(dateEndPicker));
                        WebDriverUtils.elementClick(dateEndPicker);
                    }
                    sleep(1000);

                    // check have to click previous month
                    WebElement leftMonthEle = WebDriverUtils.expWaitForElement(driver, By.xpath(datePickerLeftMonthXPath), 60);

                    // check left month element
                    if (leftMonthEle != null) {

                        String currentSelectedMonth = leftMonthEle.getText();

                        String currentDateMonth = currentDate.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH);

                        String currentDateYear = String.valueOf(currentDate.getYear());

                        while (!currentSelectedMonth.toLowerCase().contains(currentDateMonth.toLowerCase()) || !currentSelectedMonth.contains(currentDateYear)) {
                            // click previous month
                            WebElement preMonthEle = WebDriverUtils.expWaitForElement(driver, By.xpath(datePickerPreviousMonthXPath), 60);
                            preMonthEle.click();
                            sleep(1000);
                            leftMonthEle = WebDriverUtils.expWaitForElement(driver, By.xpath(datePickerLeftMonthXPath), 60);

                            currentSelectedMonth = leftMonthEle.getText();
                            currentDateMonth = currentDate.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH);
                            currentDateYear = String.valueOf(currentDate.getYear());

                        }

                    } else {
                        throw new Exception("Didn't find left month element");
                    }

                    // click current date eletment
                    String dayXPath = String.format(datePickerLeftDayXPathFormat, currentDate.getDayOfMonth());
                    List<WebElement> currentDateEles = WebDriverUtils.expWaitForElements(driver, By.xpath(dayXPath), 60);

                    WebElement currentDateEle = currentDateEles.size() == 1 ? currentDateEles.get(0) :
                            currentDate.getDayOfMonth() <= 7 ? currentDateEles.get(0) : currentDateEles.get(1);

                    WebDriverUtils.elementClick(currentDateEle);
                    sleep(1000);

                    //4.3点击应用按钮
                    sleep(7000);
                    driver.manage().timeouts().pageLoadTimeout(7, TimeUnit.SECONDS); // 页面加载超时时间
                    WebElement applyEle = WebDriverUtils.expWaitForElement(driver, By.xpath("//*[@id='dashboard-filter-applyCancel']/div/awsui-button[2]/button"), 10);
                    WebDriverUtils.elementClick(applyEle);
                    apply = true;

                    // download file
                    // 6.抓取点击下载元素进行点击
                    // 判断是否出现了Download按钮,未在规定时间内出现重新刷新页面
                    driver.manage().timeouts().pageLoadTimeout(7, TimeUnit.SECONDS); // 页面加载超时时间
                    // css selector click
                    wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//*[@id=\"downloadButton\"]/awsui-button-dropdown/div/awsui-button/button")));

                    WebElement downloadButtonElement = WebDriverUtils.expWaitForElement(driver, By.xpath("//*[@id=\"downloadButton\"]/awsui-button-dropdown/div/awsui-button/button"), 10);
                    WebDriverUtils.elementClick(downloadButtonElement);

                    // 7.抓取CSV元素生成并进行点击
                    WebElement csvButtonElement = WebDriverUtils.expWaitForElement(driver, By.xpath("//*[@id=\"downloadButton\"]/awsui-button-dropdown/div/div/ul/li/ul/li[2]"), 10);
                    WebDriverUtils.elementClick(csvButtonElement);

                    // 8.获取点击之后的弹出框点击确定
                    if (log.isInfoEnabled()) {
                        log.info("1.step132=>wait for alert is present");
                    }

                    wait.until(ExpectedConditions.alertIsPresent());
                    if (log.isInfoEnabled()) {
                        log.info("1.1.step137=>scrapy the alert");
                    }
                    Alert downloadAlertElement = driver.switchTo().alert();//获取弹出框
                    log.info("alert text:" + downloadAlertElement.getText());//获取框中文本内容
                    log.info("alert toString():" + downloadAlertElement.toString());
                    downloadAlertElement.accept();

                    sleep(5000);

                    // parse next day
                    preDate = currentDate;
                    currentDate = currentDate.minusDays(1);

                    apply = false;
                } catch (Exception e) {
                    log.info("Get Date {} file failed", currentDate.toString(), e);
                    e.printStackTrace();
                    driver.quit();
                } finally {
                    driver.quit();
                }
            }

        } catch (Exception ex) {

            ex.printStackTrace();

            log.error("Get Inventory Health Sourcing failed");

            log.error(ex.getLocalizedMessage());

        }
    }

    /**
     * Download all history inventory health manufacturing file
     */
    @Test
    public void downloadAmazonVCInventoryHealthManufacturingFile() {

        // 1.建立WebDriver
        System.setProperty("webdriver.chrome.driver", spiderConfig.getChromeDriverPath());

        String downloadFilePath = spiderConfig.getDownloadPath();

        ObjectMapper objectMapper = new ObjectMapper();

        try {

            LocalDate lastDate = LocalDate.of(2019, 9, 1);
            LocalDate preDate = LocalDate.now();
            LocalDate currentDate = preDate.minusDays(1);
            boolean apply = false;

            while (!lastDate.isEqual(currentDate) && lastDate.compareTo(currentDate) < 0) {
                WebDriver driver = WebDriverUtils.getWebDriver(downloadFilePath, false);
                try {

                    WebDriverWait wait = new WebDriverWait(driver, 60);

                    // 2.初始打开页面
                    driver.manage().timeouts().pageLoadTimeout(20, TimeUnit.SECONDS); // 页面加载超时时间
                    driver.get(SpiderUrl.AMAZON_VC_INDEX);

                    // 3.Set cookie
                    driver.manage().deleteAllCookies();

                    List<Cookie> cookies = commonSettingService.getAmazonVCCookies();

                    List<org.openqa.selenium.Cookie> savedCookies = CookiesUtils.cookiesToSeleniumCookies(cookies);

                    WebDriverUtils.addSeleniumCookies(driver, savedCookies);

                    // cookies are not valid
                    if (!WebDriverUtils.checkAmazonVCCookiesValid(driver)) {
                        driver.manage().deleteAllCookies();
                        WebDriverUtils.getAmazonVCCookies(driver);

                        List<Cookie> driverCookies = CookiesUtils.seleniumCookieToCookie(driver.manage().getCookies());

                        String newCookiesStr = objectMapper.writeValueAsString(driverCookies);

                        commonSettingService.setValue(Consts.AMAZON_VC_COOKIES, newCookiesStr, "system");
                    }

                    navigateToInventoryHealthPage(driver, wait);

                    //4.1点击日期选择按钮
                    WebElement reportingRangeButtonElement = WebDriverUtils.expWaitForElement(driver, By.xpath("//*[@id='dashboard-filter-reportingRange']//awsui-button-dropdown//button[1]"), 10);
                    if (log.isInfoEnabled()) {
                        log.info("1.step105=>reportingRangeButtonElement:" + reportingRangeButtonElement.toString());
                    }
                    WebDriverUtils.elementClick(reportingRangeButtonElement);

                    //4.2点击选择daily
                    driver.manage().timeouts().pageLoadTimeout(7, TimeUnit.SECONDS); // 页面加载超时时间
                    if (log.isInfoEnabled()) {
                        log.info("1.1.step137=>点击选择daily");
                    }

                    WebElement dailySelectElement = WebDriverUtils.expWaitForElement(driver, By.xpath("//*[@id=\"dashboard-filter-reportingRange\"]/div/awsui-button-dropdown/div/div/ul/li[1]"), 10);
                    if (log.isInfoEnabled() && dailySelectElement != null) {
                        log.info("2.step112=>dailySelectElement:" + dailySelectElement.toString());
                    }
                    WebDriverUtils.elementClick(dailySelectElement);

                    // 4.21点击DistributeView View
                    WebElement distributeViewViewButtonElement = WebDriverUtils.expWaitForElement(driver, By.xpath("//*[@id='dashboard-filter-distributorView']//awsui-button-dropdown//button"), 10);
                    if (log.isInfoEnabled() && distributeViewViewButtonElement != null) {
                        log.info("1.step105=>distributeViewViewButtonElement:" + distributeViewViewButtonElement.toString());
                    }
                    WebDriverUtils.elementClick(distributeViewViewButtonElement);

                    // 4.22点击选择 Manufacturing View
                    if (log.isInfoEnabled()) {
                        log.info("1.1.step137=>点击选择 Manufacturing View");
                    }
                    WebElement distributeViewSelectElement = WebDriverUtils.expWaitForElement(driver, By.xpath("//*[@id=\"dashboard-filter-distributorView\"]/div/awsui-button-dropdown/div/div/ul/li[1]"), 10);
                    if (log.isInfoEnabled() && distributeViewSelectElement != null) {
                        log.info("2.step112=>distributeViewSelectElement:" + distributeViewSelectElement.toString());
                    }
                    WebDriverUtils.elementClick(distributeViewSelectElement);

//                    //4.3点击应用按钮
//                    sleep(7000);
//
//                    driver.manage().timeouts().pageLoadTimeout(7, TimeUnit.SECONDS); // 页面加载超时时间
//                    WebElement applyElement = WebDriverUtils.expWaitForElement(driver, By.xpath("//*[@id='dashboard-filter-applyCancel']/div/awsui-button[2]/button"), 10);
//                    WebDriverUtils.elementClick(applyElement);
//
//                    sleep(5000);
                    // all set
                    // select date

                    // click date picker end date
                    WebElement dateEndPicker = WebDriverUtils.expWaitForElement(driver, By.xpath(dateEndPickerXPath), 60);
                    WebDriverUtils.expWaitForElement(driver, By.xpath(dateEndPickerXPath), 60);
                    if (dateEndPicker != null) {
                        dateEndPicker = wait.until(ExpectedConditions.elementToBeClickable(dateEndPicker));
                        dateEndPicker.click();
                    }
                    sleep(1000);

                    // check have to click previous month
                    WebElement leftMonthEle = WebDriverUtils.expWaitForElement(driver, By.xpath(datePickerLeftMonthXPath), 60);

                    // check left month element
                    if (leftMonthEle != null) {

                        String currentSelectedMonth = leftMonthEle.getText();

                        String currentDateMonth = currentDate.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH);

                        String currentDateYear = String.valueOf(currentDate.getYear());

                        while (!currentSelectedMonth.toLowerCase().contains(currentDateMonth.toLowerCase()) || !currentSelectedMonth.contains(currentDateYear)) {
                            // click previous month
                            WebElement preMonthEle = WebDriverUtils.expWaitForElement(driver, By.xpath(datePickerPreviousMonthXPath), 60);
                            preMonthEle.click();
                            sleep(1000);
                            leftMonthEle = WebDriverUtils.expWaitForElement(driver, By.xpath(datePickerLeftMonthXPath), 60);

                            currentSelectedMonth = leftMonthEle.getText();
                            currentDateMonth = currentDate.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH);
                            currentDateYear = String.valueOf(currentDate.getYear());

                        }

                    } else {
                        throw new Exception("Didn't find left month element");
                    }

                    // click current date eletment
                    String dayXPath = String.format(datePickerLeftDayXPathFormat, currentDate.getDayOfMonth());
                    List<WebElement> currentDateEles = WebDriverUtils.expWaitForElements(driver, By.xpath(dayXPath), 60);

                    WebElement currentDateEle = currentDateEles.size() == 1 ? currentDateEles.get(0) :
                            currentDate.getDayOfMonth() <= 7 ? currentDateEles.get(0) : currentDateEles.get(1);

                    WebDriverUtils.elementClick(currentDateEle);
                    sleep(1000);

                    //4.3点击应用按钮
                    sleep(7000);
                    driver.manage().timeouts().pageLoadTimeout(7, TimeUnit.SECONDS); // 页面加载超时时间
                    WebElement applyEle = WebDriverUtils.expWaitForElement(driver, By.xpath("//*[@id='dashboard-filter-applyCancel']/div/awsui-button[2]/button"), 10);
                    WebDriverUtils.elementClick(applyEle);
                    apply = true;

                    // download file
                    // 6.抓取点击下载元素进行点击
                    // 判断是否出现了Download按钮,未在规定时间内出现重新刷新页面
                    driver.manage().timeouts().pageLoadTimeout(7, TimeUnit.SECONDS); // 页面加载超时时间
                    // css selector click
                    wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//*[@id=\"downloadButton\"]/awsui-button-dropdown/div/awsui-button/button")));

                    WebElement downloadButtonElement = WebDriverUtils.expWaitForElement(driver, By.xpath("//*[@id=\"downloadButton\"]/awsui-button-dropdown/div/awsui-button/button"), 10);
                    WebDriverUtils.elementClick(downloadButtonElement);

                    // 7.抓取CSV元素生成并进行点击
                    WebElement csvButtonElement = WebDriverUtils.expWaitForElement(driver, By.xpath("//*[@id=\"downloadButton\"]/awsui-button-dropdown/div/div/ul/li/ul/li[2]"), 10);
                    WebDriverUtils.elementClick(csvButtonElement);

                    // 8.获取点击之后的弹出框点击确定
                    if (log.isInfoEnabled()) {
                        log.info("1.step132=>wait for alert is present");
                    }

                    wait.until(ExpectedConditions.alertIsPresent());
                    if (log.isInfoEnabled()) {
                        log.info("1.1.step137=>scrapy the alert");
                    }
                    Alert downloadAlertElement = driver.switchTo().alert();//获取弹出框
                    log.info("alert text:" + downloadAlertElement.getText());//获取框中文本内容
                    log.info("alert toString():" + downloadAlertElement.toString());
                    downloadAlertElement.accept();

                    sleep(5000);

                    // parse next day
                    preDate = currentDate;
                    currentDate = currentDate.minusDays(1);

                    apply = false;
                } catch (Exception e) {
                    log.info("Get Date {} file failed", currentDate.toString(), e);
                    e.printStackTrace();
                    driver.quit();
                } finally {
                    driver.quit();
                }
            }

        } catch (Exception ex) {

            ex.printStackTrace();

            log.error("Get Inventory Health Sourcing failed");

            log.error(ex.getLocalizedMessage());

        }
    }

    @Test
    public void downloadAmazonVCDailySalesFiles() {

        String manufacturingViewXPath = "//*[@id=\"dashboard-filter-distributorView\"]/div/awsui-button-dropdown/div/div/ul/li[contains(@data-testid,'manufacturer')]";

        String sourcingViewXPath = "//*[@id=\"dashboard-filter-distributorView\"]/div/awsui-button-dropdown/div/div/ul/li[contains(@data-testid,'sourcing')]";

        String salesViewShippedCOGSLevelXPath = "//*[@id='dashboard-filter-viewFilter']//awsui-button-dropdown//ul/li[contains(@data-testid, \"shippedCOGSLevel\")]";

        String detailCsvXPath = "//*[@id=\"downloadButton\"]/awsui-button-dropdown/div/div/ul/li/ul[contains(@aria-label, 'Detail View')]/li[contains(@data-testid, 'salesDiagnosticDetail_csv')]";

        // 1.建立WebDriver
        System.setProperty("webdriver.chrome.driver", spiderConfig.getChromeDriverPath());

        String downloadFilePath = spiderConfig.getDownloadPath();

        ObjectMapper objectMapper = new ObjectMapper();

        LocalDate lastDate = LocalDate.of(2019, 9, 1);
        LocalDate preDate = LocalDate.of(2020, 9, 22);
        LocalDate currentDate = preDate.minusDays(1);

        while (!lastDate.isEqual(currentDate) && lastDate.compareTo(currentDate) < 0) {

            WebDriver driver = WebDriverUtils.getWebDriver(spiderConfig.getChromeDriverPath(), downloadFilePath, false);

            try {

                // 1.1设置页面超时等待时间,20S
                driver.manage().timeouts().implicitlyWait(20, TimeUnit.SECONDS);

                WebDriverWait wait = new WebDriverWait(driver, 60);

                // 2.初始打开页面
                driver.manage().timeouts().pageLoadTimeout(20, TimeUnit.SECONDS); // 页面加载超时时间
                driver.get(SpiderUrl.AMAZON_VC_INDEX);

                // Set cookie
                driver.manage().deleteAllCookies();

                List<Cookie> cookies = commonSettingService.getAmazonVCCookies();

                List<org.openqa.selenium.Cookie> savedCookies = CookiesUtils.cookiesToSeleniumCookies(cookies);

                WebDriverUtils.addSeleniumCookies(driver, savedCookies);

                // cookies are not valid
                if (!WebDriverUtils.checkAmazonVCCookiesValid(driver)) {
                    driver.manage().deleteAllCookies();

                    WebDriverUtils.getAmazonVCCookies(driver);

                    List<Cookie> driverCookies = CookiesUtils.seleniumCookieToCookie(driver.manage().getCookies());

                    String newCookiesStr = objectMapper.writeValueAsString(driverCookies);

                    commonSettingService.setValue(Consts.AMAZON_VC_COOKIES, newCookiesStr, "system");
                }

                List<org.openqa.selenium.Cookie> driverCookies = driver.manage().getCookies().stream().collect(Collectors.toList());

                driver.quit();

                LocalDate sourcingCurrentDate = currentDate;
                CompletableFuture<Void> sourcingCompletableFuture = CompletableFuture.runAsync(new Runnable() {
                    @SneakyThrows
                    @Override
                    public void run() {

                        WebDriver driver = WebDriverUtils.getWebDriver(spiderConfig.getChromeDriverPath(), downloadFilePath, false);

                        WebDriverWait wait = new WebDriverWait(driver, 60);

                        try{

                            driver.navigate().to(SpiderUrl.AMAZON_VC_INDEX);

                            WebDriverUtils.addSeleniumCookies(driver, driverCookies);

                            //4.1 navigate to sales daily page
                            navigateToDailySalesPage(driver, wait);

                            //4.1点击日期选择按钮, Reporting range
                            WebElement reportingRangeButtonElement = WebDriverUtils.expWaitForElement(driver, By.xpath("//*[@id=\"dashboard-filter-reportingRange\"]//awsui-button-dropdown//awsui-button/button"), 10);
                            if (log.isInfoEnabled()) {
                                log.info("1.step105=>reportingRangeButtonElement:" + reportingRangeButtonElement.toString());
                            }
                            WebDriverUtils.elementClick(reportingRangeButtonElement);

                            //4.2点击选择daily
                            WebElement dailySelectElement = WebDriverUtils.expWaitForElement(driver, By.xpath("//*[@id=\"dashboard-filter-reportingRange\"]/div/awsui-button-dropdown//ul/li[contains(@data-testid, 'DAILY')]"), 10);
                            if (log.isInfoEnabled()) {
                                log.info("2.step112=>dailySelectElement:" + dailySelectElement.toString());
                            }
                            dailySelectElement.click();

                            // 4.21 Choose DistributeView View
                            WebElement distributeViewViewButtonElement = WebDriverUtils.expWaitForElement(driver, By.xpath("//*[@id='dashboard-filter-distributorView']//awsui-button-dropdown//button"), 10);
                            if (log.isInfoEnabled()) {
                                log.info("1.step105=>distributeViewViewButtonElement:" + distributeViewViewButtonElement.toString());
                            }
                            distributeViewViewButtonElement.click();

                            // 4.22点击选择View
                            if (log.isInfoEnabled()) {
                                log.info("1.1.step137=>点击选择View");
                            }
                            WebElement distributeViewSelectElement = WebDriverUtils.expWaitForElement(driver, By.xpath(sourcingViewXPath), 10);
                            if (log.isInfoEnabled()) {
                                log.info("[Select sourcing distribute view]" + distributeViewSelectElement.toString());
                            }
                            distributeViewSelectElement.click();

                            // 4.21 Choose SalesView
                            WebElement salesViewButtonElement = WebDriverUtils.expWaitForElement(driver, By.xpath("//*[@id='dashboard-filter-viewFilter']//awsui-button-dropdown//button[1]"), 10);
                            if (log.isInfoEnabled()) {
                                log.info("1.step105=>salesViewButtonElement:" + salesViewButtonElement.toString());
                            }
                            salesViewButtonElement.click();

                            // 4.22 Select Shipped COGS
                            if (log.isInfoEnabled()) {
                                log.info("1.1.step137=>点击选择SalesView");
                            }
                            WebElement salesViewSelectElement = WebDriverUtils.expWaitForElement(driver, By.xpath(salesViewShippedCOGSLevelXPath), 10);
                            if (log.isInfoEnabled()) {
                                log.info("2.step112=>salesViewSelectElement:" + salesViewSelectElement.toString());
                            }
                            salesViewSelectElement.click();

                            // click date picker end date
                            WebElement dateEndPicker = WebDriverUtils.expWaitForElement(driver, By.xpath(dateEndPickerXPath), 60);
                            WebDriverUtils.expWaitForElement(driver, By.xpath(dateEndPickerXPath), 60);
                            if (dateEndPicker != null) {
                                dateEndPicker = wait.until(ExpectedConditions.elementToBeClickable(dateEndPicker));
                                dateEndPicker.click();
                            }
                            sleep(1000);

                            // check have to click previous month
                            WebElement leftMonthEle = WebDriverUtils.expWaitForElement(driver, By.xpath(datePickerLeftMonthXPath), 60);

                            // check left month container element
                            if (leftMonthEle != null) {

                                String currentSelectedMonth = leftMonthEle.getText();

                                String currentDateMonth = sourcingCurrentDate.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH);

                                String currentDateYear = String.valueOf(sourcingCurrentDate.getYear());

                                // select the current date month
                                while (!currentSelectedMonth.toLowerCase().contains(currentDateMonth.toLowerCase()) || !currentSelectedMonth.contains(currentDateYear)) {
                                    // click previous month
                                    WebElement preMonthEle = WebDriverUtils.expWaitForElement(driver, By.xpath(datePickerPreviousMonthXPath), 60);
                                    preMonthEle.click();
                                    sleep(1000);
                                    leftMonthEle = WebDriverUtils.expWaitForElement(driver, By.xpath(datePickerLeftMonthXPath), 60);

                                    currentSelectedMonth = leftMonthEle.getText();
                                    currentDateMonth = sourcingCurrentDate.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH);
                                    currentDateYear = String.valueOf(sourcingCurrentDate.getYear());

                                }

                            } else {
                                throw new Exception("Didn't find left month element");
                            }

                            // click current date eletment
                            String dayXPath = String.format(datePickerLeftDayXPathFormat, sourcingCurrentDate.getDayOfMonth());
                            List<WebElement> currentDateEles = WebDriverUtils.expWaitForElements(driver, By.xpath(dayXPath), 60);

                            WebElement currentDateEle = currentDateEles.size() == 1 ? currentDateEles.get(0) :
                                    sourcingCurrentDate.getDayOfMonth() <= 7 ? currentDateEles.get(0) : currentDateEles.get(1);

                            WebDriverUtils.elementClick(currentDateEle);
                            sleep(1000);

                            //4.3点击应用按钮
                            WebElement applyElement = WebDriverUtils.expWaitForElement(driver, By.xpath("//*[@id='dashboard-filter-applyCancel']/div/awsui-button[2]/button"), 10);
                            applyElement.click();

                            sleep(10000);

                            // 6.抓取点击下载元素进行点击
                            // 判断是否出现了Download按钮,未在规定时间内出现重新刷新页面
                            WebElement downloadButtonElement = WebDriverUtils.expWaitForElement(driver, By.xpath("//*[@id='downloadButton']//button[1]"), 10);
                            downloadButtonElement.click();

                            // 7.抓取CSV元素生成并进行点击
                            WebElement detailCsvDownloadButtonElement = WebDriverUtils.expWaitForElement(driver, By.xpath(detailCsvXPath), 10);
                            detailCsvDownloadButtonElement.click();

                            // 8.获取点击之后的弹出框点击确定
                            if (log.isInfoEnabled()) {
                                log.info("1.step132=>wait for alert is present");
                            }

                            wait.until(ExpectedConditions.alertIsPresent());
                            if (log.isInfoEnabled()) {
                                log.info("1.1.step137=>scrapy the alert");
                            }
                            Alert downloadAlertElement = driver.switchTo().alert();//获取弹出框
                            log.info("alert text:" + downloadAlertElement.getText());//获取框中文本内容
                            log.info("alert toString():" + downloadAlertElement.toString());
                            downloadAlertElement.accept();

                            sleep(3000);

                        }catch (Exception e){
                            e.printStackTrace();
                            driver.quit();
                            throw e;
                        }finally {
                            driver.quit();
                        }

                    }
                });

                sleep(1000);

                LocalDate manufacturingCurrentDate = currentDate;
                CompletableFuture<Void> manufacturingCompletableFuture = CompletableFuture.runAsync(new Runnable() {
                    @SneakyThrows
                    @Override
                    public void run() {

                        WebDriver driver = WebDriverUtils.getWebDriver(spiderConfig.getChromeDriverPath(), downloadFilePath, false);

                        WebDriverWait wait = new WebDriverWait(driver, 60);

                        try{

                            driver.navigate().to(SpiderUrl.AMAZON_VC_INDEX);

                            WebDriverUtils.addSeleniumCookies(driver, driverCookies);

                            //4.1 navigate to sales daily page
                            navigateToDailySalesPage(driver, wait);

                            //4.1点击日期选择按钮, Reporting range
                            WebElement reportingRangeButtonElement = WebDriverUtils.expWaitForElement(driver, By.xpath("//*[@id=\"dashboard-filter-reportingRange\"]//awsui-button-dropdown//awsui-button/button"), 10);
                            if (log.isInfoEnabled()) {
                                log.info("1.step105=>reportingRangeButtonElement:" + reportingRangeButtonElement.toString());
                            }
                            WebDriverUtils.elementClick(reportingRangeButtonElement);

                            //4.2点击选择daily
                            WebElement dailySelectElement = WebDriverUtils.expWaitForElement(driver, By.xpath("//*[@id=\"dashboard-filter-reportingRange\"]/div/awsui-button-dropdown//ul/li[contains(@data-testid, 'DAILY')]"), 10);
                            if (log.isInfoEnabled()) {
                                log.info("2.step112=>dailySelectElement:" + dailySelectElement.toString());
                            }
                            dailySelectElement.click();

                            // 4.21 Choose DistributeView View
                            WebElement distributeViewViewButtonElement = WebDriverUtils.expWaitForElement(driver, By.xpath("//*[@id='dashboard-filter-distributorView']//awsui-button-dropdown//button"), 10);
                            if (log.isInfoEnabled()) {
                                log.info("1.step105=>distributeViewViewButtonElement:" + distributeViewViewButtonElement.toString());
                            }
                            distributeViewViewButtonElement.click();

                            // 4.22点击选择View
                            if (log.isInfoEnabled()) {
                                log.info("1.1.step137=>点击选择View");
                            }
                            WebElement distributeViewSelectElement = WebDriverUtils.expWaitForElement(driver, By.xpath(manufacturingViewXPath), 10);
                            if (log.isInfoEnabled()) {
                                log.info("2.step112=>distributeViewSelectElement:" + distributeViewSelectElement.toString());
                            }
                            distributeViewSelectElement.click();

                            // 4.21 Choose SalesView
                            WebElement salesViewButtonElement = WebDriverUtils.expWaitForElement(driver, By.xpath("//*[@id='dashboard-filter-viewFilter']//awsui-button-dropdown//button[1]"), 10);
                            if (log.isInfoEnabled()) {
                                log.info("1.step105=>salesViewButtonElement:" + salesViewButtonElement.toString());
                            }
                            salesViewButtonElement.click();

                            // 4.22 Select Shipped COGS
                            if (log.isInfoEnabled()) {
                                log.info("1.1.step137=>点击选择SalesView");
                            }
                            WebElement salesViewSelectElement = WebDriverUtils.expWaitForElement(driver, By.xpath(salesViewShippedCOGSLevelXPath), 10);
                            if (log.isInfoEnabled()) {
                                log.info("2.step112=>salesViewSelectElement:" + salesViewSelectElement.toString());
                            }
                            salesViewSelectElement.click();

                            // click date picker end date
                            WebElement dateEndPicker = WebDriverUtils.expWaitForElement(driver, By.xpath(dateEndPickerXPath), 60);
                            WebDriverUtils.expWaitForElement(driver, By.xpath(dateEndPickerXPath), 60);
                            if (dateEndPicker != null) {
                                dateEndPicker = wait.until(ExpectedConditions.elementToBeClickable(dateEndPicker));
                                dateEndPicker.click();
                            }
                            sleep(5000);

                            // check have to click previous month
                            WebElement leftMonthEle = WebDriverUtils.expWaitForElement(driver, By.xpath(datePickerLeftMonthXPath), 60);

                            // check left month container element
                            if (leftMonthEle != null) {

                                String currentSelectedMonth = leftMonthEle.getText();

                                String currentDateMonth = manufacturingCurrentDate.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH);

                                String currentDateYear = String.valueOf(manufacturingCurrentDate.getYear());

                                // select the current date month
                                while (!currentSelectedMonth.toLowerCase().contains(currentDateMonth.toLowerCase()) || !currentSelectedMonth.contains(currentDateYear)) {
                                    // click previous month
                                    WebElement preMonthEle = WebDriverUtils.expWaitForElement(driver, By.xpath(datePickerPreviousMonthXPath), 60);
                                    preMonthEle.click();
                                    sleep(1000);
                                    leftMonthEle = WebDriverUtils.expWaitForElement(driver, By.xpath(datePickerLeftMonthXPath), 60);

                                    currentSelectedMonth = leftMonthEle.getText();
                                    currentDateMonth = manufacturingCurrentDate.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH);
                                    currentDateYear = String.valueOf(manufacturingCurrentDate.getYear());

                                }

                            } else {
                                throw new Exception("Didn't find left month element");
                            }

                            // click current date eletment
                            String dayXPath = String.format(datePickerLeftDayXPathFormat, manufacturingCurrentDate.getDayOfMonth());
                            List<WebElement> currentDateEles = WebDriverUtils.expWaitForElements(driver, By.xpath(dayXPath), 60);

                            WebElement currentDateEle = currentDateEles.size() == 1 ? currentDateEles.get(0) :
                                    manufacturingCurrentDate.getDayOfMonth() <= 7 ? currentDateEles.get(0) : currentDateEles.get(1);

                            WebDriverUtils.elementClick(currentDateEle);
                            sleep(1000);

                            //4.3点击应用按钮
                            WebElement applyElement = WebDriverUtils.expWaitForElement(driver, By.xpath("//*[@id='dashboard-filter-applyCancel']/div/awsui-button[2]/button"), 10);
                            applyElement.click();

                            sleep(10000);

                            // 6.抓取点击下载元素进行点击
                            // 判断是否出现了Download按钮,未在规定时间内出现重新刷新页面
                            WebElement downloadButtonElement = WebDriverUtils.expWaitForElement(driver, By.xpath("//*[@id='downloadButton']//button[1]"), 10);
                            downloadButtonElement.click();

                            // 7.抓取CSV元素生成并进行点击
                            WebElement detailCsvDownloadButtonElement = WebDriverUtils.expWaitForElement(driver, By.xpath(detailCsvXPath), 10);
                            detailCsvDownloadButtonElement.click();

                            // 8.获取点击之后的弹出框点击确定
                            if (log.isInfoEnabled()) {
                                log.info("1.step132=>wait for alert is present");
                            }

                            wait.until(ExpectedConditions.alertIsPresent());
                            if (log.isInfoEnabled()) {
                                log.info("1.1.step137=>scrapy the alert");
                            }
                            Alert downloadAlertElement = driver.switchTo().alert();//获取弹出框
                            log.info("alert text:" + downloadAlertElement.getText());//获取框中文本内容
                            log.info("alert toString():" + downloadAlertElement.toString());
                            downloadAlertElement.accept();

                            sleep(3000);

                        }catch (Exception e){
                            log.info("manufacturingCompletableFuture {} failed", manufacturingCurrentDate.toString(), e);
                            e.printStackTrace();
                            driver.quit();
                            throw e;
                        }finally {
                            driver.quit();
                        }
                    }
                });

                CompletableFuture.allOf(manufacturingCompletableFuture, sourcingCompletableFuture).join();

                // parse next day
                preDate = currentDate;
                currentDate = currentDate.minusDays(1);

            } catch (Exception e) {
                log.info("Get Amazon Daily Sales Date {} files failed", currentDate.toString(), e);
                e.printStackTrace();
                driver.quit();
            } finally {
                driver.quit();
            }
        }

    }

    private void navigateToDailySalesPage(WebDriver driver, WebDriverWait wait) throws InterruptedException {
        // 1.1设置页面超时等待时间,20S
        driver.manage().timeouts().implicitlyWait(20, TimeUnit.SECONDS);

        // 2.Navigate to daily sales page
        driver.manage().timeouts().pageLoadTimeout(20, TimeUnit.SECONDS); // 页面加载超时时间
        driver.get(SpiderUrl.AMAZON_VC_ANALYTICS_SALES_DIAGNOSTIC);

        sleep(3000);

        if (driver.getCurrentUrl().equals(SpiderUrl.AMAZON_VC_ANALYTICS_SALES_DIAGNOSTIC)) {
            // 4.重定向跳转
            driver.manage().timeouts().pageLoadTimeout(20, TimeUnit.SECONDS); // 页面加载超时时间
            driver.get(SpiderUrl.AMAZON_VC_DASHBOARD);

            sleep(3000);

            //4.0 click salesDiagnostic
            WebElement inventoryHealthButtonElement = WebDriverUtils.expWaitForElement(driver, By.xpath("//a[contains(@href,'/analytics/dashboard/salesDiagnostic')]"), 10);
            if (log.isInfoEnabled() && inventoryHealthButtonElement != null) {
                log.info("1.step105=>reportingRangeButtonElement:" + inventoryHealthButtonElement.toString());
            }
            WebDriverUtils.elementClick(inventoryHealthButtonElement);
            sleep(5000);
        }
    }

    private void navigateToInventoryHealthPage(WebDriver driver, WebDriverWait wait) throws InterruptedException {

        // 1.1设置页面超时等待时间,20S
        driver.manage().timeouts().implicitlyWait(20, TimeUnit.SECONDS);

        // 2.Navigate to inventory health page
        driver.get(SpiderUrl.AMAZON_VC_ANALYTICS_INVENTORY_HEALTH);

        sleep(3000);

        if (!driver.getCurrentUrl().equals(SpiderUrl.AMAZON_VC_ANALYTICS_INVENTORY_HEALTH)) {
            // 4.重定向跳转
            driver.navigate().to(SpiderUrl.AMAZON_VC_DASHBOARD);

            //4.0 click inventoryHealth
            WebElement inventoryHealthButtonElement = WebDriverUtils.expWaitForElement(driver, By.xpath("//a[contains(@href,'/analytics/dashboard/inventory')]"), 10);
            if (log.isInfoEnabled() && inventoryHealthButtonElement != null) {
                log.info("1.step105=>reportingRangeButtonElement:" + inventoryHealthButtonElement.toString());
            }
            WebDriverUtils.elementClick(inventoryHealthButtonElement);
            sleep(10000);
        }
    }
}