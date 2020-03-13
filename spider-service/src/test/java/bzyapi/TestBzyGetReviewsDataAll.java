package bzyapi;

import com.spider.SpiderServiceApplication;
import com.spider.amazon.cons.GetDataOfTaskByOffsetOperaTypeEnum;
import com.spider.amazon.dto.BzyGetDataOfTaskByOffsetDTO;
import com.spider.amazon.handler.ReviewsInfoGetSpiderDataByOffsetHandler;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes=SpiderServiceApplication.class)
class TestBzyGetReviewsDataAll {

    @Autowired
    private ReviewsInfoGetSpiderDataByOffsetHandler  reviewsInfoGetSpiderDataByOffsetHandler;


    @Test
    void test() {
        reviewsInfoGetSpiderDataByOffsetHandler.getAllBzyDataByOffset(
                GetDataOfTaskByOffsetOperaTypeEnum.SKU_REVIEW_INFO_GET_DATA,
                BzyGetDataOfTaskByOffsetDTO.builder()
                        .taskId("14458884-7e8d-4f5f-94c9-5bcce817071e")
                        .offset("0")
                        .size("1000")
                        .token("MUV-kevto1ES1nui-T0O0FGwSBdPFYf4mMgDUOFeDsDdoI6fGwum688Vick68LIKuhLhQ_HsZdZ5O4J4AegBFAoeEmH7R4NC-uvmJDup0gdK2w__CLVq47VHrhXhLarmaOtbcLCZcsYWxg7SRJdDJwGhXusVFqKv8FtZfVJeho8Z3_aJi0vUP5JTJ2FrjGN9TVBgV-Yrb2kUEfs9YucUJMSjVuGKhr8UYxr5BPpW3xk")
                        .build());
    }


}