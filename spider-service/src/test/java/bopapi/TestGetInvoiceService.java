package bopapi;

import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateUtil;
import com.spider.SpiderServiceApplication;
import com.spider.amazon.dto.GetInvoiceDTO;
import com.spider.amazon.service.IInvoiceService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Date;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes=SpiderServiceApplication.class)
class TestGetInvoiceService {

    @Autowired
    private IInvoiceService invoiceService;


    @Test
    void test() {
        // 循环遍历某个日期段的值并获取
        Date startdate=DateUtil.parseDate("2020-03-12");
        Date enddate=DateUtil.parseDate("2020-03-13");
        Date curdate=startdate;
        while (DateUtil.compare(curdate,enddate)<0) {
            System.out.println("curdate:"+curdate.toString());
            invoiceService.getInvoice(GetInvoiceDTO.builder()
                    .pageNo(1)
                    .pageSize(10000)
                    .asin("")
                    .invoiceDate(DateUtil.format(curdate,"yyyyMMdd"))
                    .channel("")
                    .build());
            curdate=DateUtil.offset(curdate, DateField.DAY_OF_MONTH, 1);
        }
    }

}