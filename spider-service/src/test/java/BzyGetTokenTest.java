import com.spider.SpiderServiceApplication;
import com.spider.amazon.config.BaZhuaYuConfiguration;
import com.spider.amazon.cons.GrantType;
import com.spider.amazon.dto.BzyGetTokenDTO;
import com.spider.amazon.service.impl.BaZhuaYuServiceImpl;
import com.spider.amazon.vo.BzyGetTokenRepVO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;


@ExtendWith(SpringExtension.class)
@SpringBootTest(classes=SpiderServiceApplication.class)
public class BzyGetTokenTest {

    @Autowired
    private BaZhuaYuServiceImpl baZhuaYuServiceImpl;

    @Autowired
    private BaZhuaYuConfiguration baZhuaYuConfiguration;

    @Test
    public void test() throws Exception{
        BzyGetTokenDTO bzyGetTokenDTO=BzyGetTokenDTO.builder().userName(baZhuaYuConfiguration.getUsername()).passWord(baZhuaYuConfiguration.getPassword()).grantType(GrantType.BY_PASSWORD).build();

        BzyGetTokenRepVO callBzyGetToken=baZhuaYuServiceImpl.callBzyGetToken(bzyGetTokenDTO);

        System.out.println("callBzyGetToken："+callBzyGetToken.toString());


    }
}