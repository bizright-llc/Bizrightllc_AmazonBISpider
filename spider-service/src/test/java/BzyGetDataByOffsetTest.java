import com.spider.SpiderServiceApplication;
import com.spider.amazon.config.BaZhuaYuConfiguration;
import com.spider.amazon.dto.BzyGetDataOfTaskByOffsetDTO;
import com.spider.amazon.service.impl.BaZhuaYuServiceImpl;
import com.spider.amazon.vo.BzyGetDataOfTaskByOffsetRepVO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;


@ExtendWith(SpringExtension.class)
@SpringBootTest(classes=SpiderServiceApplication.class)
public class BzyGetDataByOffsetTest {

    @Autowired
    private BaZhuaYuServiceImpl baZhuaYuServiceImpl;

    @Autowired
    private BaZhuaYuConfiguration baZhuaYuConfiguration;

    @Test
    public void test() throws Exception{
        BzyGetDataOfTaskByOffsetDTO request=BzyGetDataOfTaskByOffsetDTO.builder()
                .taskId("14458884-7e8d-4f5f-94c9-5bcce817071e")
                .offset("1000")
                .size("500")
                .token("qB6A-FcEawkC-vh5XJio71EdqsNWijsvf1tLDeZpb4JPltnzdB1fDXtvPa3_k4ZnWUpEu3r7IdBV5GFbITrPHJZSegS0cn5FhK9tEdDTKNthC4uG7h62HoN7lABUrCnwhc1vJVC8jn5tTNv1jSD93pQA3xNsx0wK-lWNu4CUZE5he6NlwGd0Z6mwZwHbxE08ns5zqq69g8cOLPO7R_um8A")
                .build();

        BzyGetDataOfTaskByOffsetRepVO result=baZhuaYuServiceImpl.callGetDataOfTaskByOffset(request);

        System.out.println("result："+result.toString());
    }
}