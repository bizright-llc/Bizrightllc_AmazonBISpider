package datadeal;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.spider.SpiderServiceApplication;
import com.spider.amazon.cons.DateFormat;
import com.spider.amazon.service.IFbaInventoryReportDealService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;


@ExtendWith(SpringExtension.class)
@SpringBootTest(classes=SpiderServiceApplication.class)
class TestScFBAInventory {

    @Autowired
    private IFbaInventoryReportDealService  fbaInventoryReportDealService;

    /**
     * Fba库存日报处理属性
     */
    private final static String fbaInventoryFileName = "Fba_Inventory";
    private static final String filePath = "C:\\Users\\keeley.z\\Downloads\\";
    private static final int offerSetDay = 0;

    @Test
    void testVcWeeklySales(){
        fbaInventoryReportDealService.dealFbaInventoryReport(StrUtil.concat(true,fbaInventoryFileName,"-", DateUtil.format(DateUtil.offsetDay(DateUtil.date(),offerSetDay), DateFormat.YEAR_MONTH_DAY),".csv"),filePath,offerSetDay);
    }
}