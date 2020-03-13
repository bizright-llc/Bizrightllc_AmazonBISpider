import com.spider.SpiderServiceApplication;
import com.spider.amazon.mq.HawDataDealReceicer;
import com.spider.amazon.mq.HawReceicer;
import com.spider.amazon.service.TestMain;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.annotation.Resource;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes=SpiderServiceApplication.class)
public class TestAutoWired {

    @Resource
    private TestMain testMain;

    @Autowired
    private HawDataDealReceicer hawDataDealReceicer ;

    @Autowired
    private HawReceicer hawReceicer ;

    @Test
    public void test() throws Exception{

//        hawDataDealReceicer.consumeHawQueue1();

//        hawReceicer.consumeHawQueue1();
    }
}
