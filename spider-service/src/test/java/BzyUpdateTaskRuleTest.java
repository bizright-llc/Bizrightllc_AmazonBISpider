import com.spider.SpiderServiceApplication;
import com.spider.amazon.config.BaZhuaYuConfiguration;
import com.spider.amazon.dto.BzyUpdateTaskRuleDTO;
import com.spider.amazon.service.impl.BaZhuaYuServiceImpl;
import com.spider.amazon.vo.BzyUpdateTaskRuleRepVO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;


@ExtendWith(SpringExtension.class)
@SpringBootTest(classes=SpiderServiceApplication.class)
public class BzyUpdateTaskRuleTest {

    @Autowired
    private BaZhuaYuServiceImpl baZhuaYuServiceImpl;

    @Autowired
    private BaZhuaYuConfiguration baZhuaYuConfiguration;

    @Test
    public void test() throws Exception{
        BzyUpdateTaskRuleDTO bzyUpdateTaskRuleDTO=BzyUpdateTaskRuleDTO.builder()
                .taskId("d497d55d-a0b0-404d-b630-a2bf61c974fc")
                .name("loopAction1.UrlList")
                .value("['http://www.amazon.com/dp/B005CY100I','http://www.amazon.com/dp/B018WIOEDA','http://www.amazon.com/dp/B017V6BQJE','http://www.amazon.com/dp/B018WIOGKG']")
                .token("cSSsJPwV7izRA4B84gBeHhwyKzuhKT66fPb8D9h5whFZMR2hR0T5xrbv12UcT6xYAAg5vkSjPCCP8gVdmklh93nsTUqmyx_WcXcg7xkeCGKwwhG7QqhvBAUPp07pVPKvrafRD5i82ijpHNtY4yL2n-_n55UhsUYHXmQ5UO8kDAax-yX42hjEanXGTjeCgRBRP0KUgo6C4fu16YdwaZ7IIw")
                .build();

        BzyUpdateTaskRuleRepVO result=baZhuaYuServiceImpl.callBzyUpdateTaskRule(bzyUpdateTaskRuleDTO);

        System.out.println("result："+result.toString());
    }
}