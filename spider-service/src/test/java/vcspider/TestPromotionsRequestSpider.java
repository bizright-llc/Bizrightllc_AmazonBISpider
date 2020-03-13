package vcspider;

import com.spider.SpiderServiceApplication;
import com.spider.amazon.service.IAmzVcPromotionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.HashMap;
import java.util.Map;


@ExtendWith(SpringExtension.class)
@SpringBootTest(classes=SpiderServiceApplication.class)
class TestPromotionsRequestSpider {
    @Autowired
    private IAmzVcPromotionService amzVcPromotionServiceImpl;

    @Test
    void testHawSpider(){
        Map<String,Object> params=new HashMap<>();
        amzVcPromotionServiceImpl.scrapyPromotionInfo(params);
    }
}