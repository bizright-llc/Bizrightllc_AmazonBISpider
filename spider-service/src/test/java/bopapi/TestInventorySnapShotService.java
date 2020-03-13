package bopapi;

import com.spider.SpiderServiceApplication;
import com.spider.amazon.cons.PageQryType;
import com.spider.amazon.dto.GetInventoryDataDailySnapShotDTO;
import com.spider.amazon.service.IBopService;
import com.spider.amazon.service.IInventoryDataDailySnapShotService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes=SpiderServiceApplication.class)
class TestInventorySnapShotService {

    @Autowired
    private IInventoryDataDailySnapShotService inventoryDataDailySnapShotService;

    @Autowired
    private IBopService bopServiceImpl;

    @Test
    void test() {

//        GetInventoryDataDailySnapShotRepVO responseVO = bopServiceImpl.callBopGetInventoryDataDailySnapShot(GetInventoryDataDailySnapShotDTO.builder()
//                .pageNo(1).pageSize(100).build());
//
//        System.out.println("responseVO:"+ JSONUtil.parse(responseVO));

        inventoryDataDailySnapShotService.getInventoryDataDailySnapShot(GetInventoryDataDailySnapShotDTO.builder()
                .pageNo(1).pageSize(10).getType(PageQryType.QRY_ALL).build());
    }


}