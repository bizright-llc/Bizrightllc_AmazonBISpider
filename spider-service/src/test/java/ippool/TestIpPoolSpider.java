package ippool;

import com.spider.SpiderServiceApplication;
import com.spider.amazon.config.SpiderConfig;
import com.spider.amazon.webmagic.ippool.IpPoolPipeline;
import com.spider.amazon.webmagic.ippool.IpPoolProcessor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.scheduler.PriorityScheduler;


@ExtendWith(SpringExtension.class)
@SpringBootTest(classes=SpiderServiceApplication.class)
class TestIpPoolSpider {

    @Autowired
    private SpiderConfig spiderConfig;

    @Test
    void testHawSpider(){

        // 3.调用爬虫
        Spider spider= Spider.create(new IpPoolProcessor());
        PriorityScheduler scheduler = new PriorityScheduler();
        for (int index=1;index<=100;index++) {
            scheduler.push(new Request("http://www.89ip.cn/index_"+index+".html"),spider);
        }
        spider.addPipeline(new IpPoolPipeline());
        spider.setScheduler(scheduler);
        spider.thread(2).run();
    }
}