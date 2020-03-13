package bopapi;

import com.spider.SpiderServiceApplication;
import com.spider.amazon.dto.GetWarehouseTransferDTO;
import com.spider.amazon.service.IBopService;
import com.spider.amazon.service.IWarehouseTransferService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes=SpiderServiceApplication.class)
class TestWarehouseTransferService {

    @Autowired
    private IWarehouseTransferService warehouseTransferService;

    @Autowired
    private IBopService bopServiceImpl;

    @Test
    void test() {

//        GetWarehouseTransferRepVO responseVO = bopServiceImpl.callBopGetWarehouseTransfer(GetWarehouseTransferDTO.builder()
//                .pageNo(1).pageSize(10000).build());
//
//        System.out.println("responseVO:"+responseVO.toString());
//
        warehouseTransferService.getWarehouseTransfer(GetWarehouseTransferDTO.builder()
                .pageNo(1).pageSize(10000).build());
    }

}