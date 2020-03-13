import com.spider.SpiderServiceApplication;
import com.spider.amazon.cons.UpdateBzyTaskRuleOperaTypeEnum;
import com.spider.amazon.handler.SkuCommonInfoUpdSpiderHandler;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;


@ExtendWith(SpringExtension.class)
@SpringBootTest(classes=SpiderServiceApplication.class)
public class BzyUpdateTaskRuleByAllSkuTest {

    @Autowired
    private SkuCommonInfoUpdSpiderHandler skuCommonInfoUpdSpiderHandler;

    @Test
    public void test() throws Exception{
        skuCommonInfoUpdSpiderHandler.updateBzyTaskRuleForAllSku(UpdateBzyTaskRuleOperaTypeEnum.SKU_COMMON_INFO_UPD_RULE);
    }
}