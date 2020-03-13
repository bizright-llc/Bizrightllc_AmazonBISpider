package vcspider;

import com.spider.SpiderServiceApplication;
import com.spider.amazon.config.SpiderConfig;
import com.spider.amazon.webmagic.amzvc.AmazonVcPromotionsPipeline;
import com.spider.amazon.webmagic.amzvc.AmazonVcPromotionsProcessor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Spider;


@ExtendWith(SpringExtension.class)
@SpringBootTest(classes=SpiderServiceApplication.class)
class TestPromotionsSpider {

    @Autowired
    private SpiderConfig spiderConfig;

    @Test
    void testHawSpider(){
        // 3.调用爬虫
        Spider spider= Spider.create(new AmazonVcPromotionsProcessor());
        spider.thread(2);
        spider.addPipeline(new AmazonVcPromotionsPipeline());
        Request request = new Request(spiderConfig.getSpiderIndex());

        request.putExtra("craw_id","20200303165500");
        spider.addRequest(request);
        spider.start();
    }
}