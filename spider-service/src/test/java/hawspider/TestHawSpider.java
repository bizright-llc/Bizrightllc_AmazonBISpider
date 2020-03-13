package hawspider;

import com.spider.SpiderServiceApplication;
import com.spider.amazon.config.SpiderConfig;
import com.spider.amazon.webmagic.haw.HawProductInfoPipeline;
import com.spider.amazon.webmagic.haw.HawProductInfoProcessor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Spider;


@ExtendWith(SpringExtension.class)
@SpringBootTest(classes=SpiderServiceApplication.class)
class TestHawSpider {

    @Autowired
    private SpiderConfig spiderConfig;

    @Test
    void testHawSpider(){
        // 3.调用爬虫
        Spider spider= Spider.create(new HawProductInfoProcessor());
        spider.addPipeline(new HawProductInfoPipeline());
        Request request = new Request(spiderConfig.getSpiderHawIndex());
        request.putExtra(HawProductInfoProcessor.PRODUCT_ID_LIST,"HGC736472|HGC701265");
        request.putExtra(HawProductInfoProcessor.TASK_ID,"a387a28e62fe43fa95b1562440a08fbb");
        spider.addRequest(request);
        spider.start();
    }
}