package vcspider;

import com.spider.amazon.config.SpiderConfig;
import com.spider.amazon.webmagic.AmazonVcWeeklySales;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import us.codecraft.webmagic.Spider;

class TestWeeklyVcSales {

    @Autowired
    private SpiderConfig spiderConfig;

    @Test
    void testVcWeeklySales(){
        Spider.create(new AmazonVcWeeklySales(spiderConfig))
                .addUrl("https://vendorcentral.amazon.com/analytics/dashboard/salesDiagnostic")
                .start();
    }
}