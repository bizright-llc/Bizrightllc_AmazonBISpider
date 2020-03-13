package vcspider;

import com.spider.amazon.webmagic.AmazonVcWeeklySales;
import org.junit.jupiter.api.Test;
import us.codecraft.webmagic.Spider;

class TestWeeklyVcSales {

    @Test
    void testVcWeeklySales(){
        Spider.create(new AmazonVcWeeklySales())
                .addUrl("https://vendorcentral.amazon.com/analytics/dashboard/salesDiagnostic")
                .start();
    }
}