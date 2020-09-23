package com.spider.amazon.mapper;

import com.spider.SpiderServiceApplication;
import com.spider.amazon.model.AsinSkuMapDO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


@ExtendWith(SpringExtension.class)
@SpringBootTest(classes= SpiderServiceApplication.class)
class AsinSkuMapDOMapperTest {

    @Autowired
    private AsinSkuMapDOMapper asinSkuMapDOMapper;

    @Test
    void insertBatch() throws IOException {

        List<List<AsinSkuMapDO>> records = new LinkedList<List<AsinSkuMapDO>>();

        try(BufferedReader csvReader = new BufferedReader(new FileReader("/Users/shaochinlin/Downloads/asin_sku_pm_map.csv"))){
            List<AsinSkuMapDO> block= null;
            String line;
            while ((line = csvReader.readLine()) != null) {
                String[] data = line.split(",");
                // do something with the data
                String pm = data[0];
                String asin = data[1];
                String sku = data[2];

                String pmId = "";

                if(pm.equals("Sam")){
                    pmId = "7";
                }else if (pm.equals("Yafu")){
                    pmId = "11";
                }else if (pm.equals("Todd")){
                    pmId = "8";
                }

                AsinSkuMapDO newRecord = AsinSkuMapDO.builder().asin(asin).pm(pmId).sku(sku).createdBy("4").updatedBy("4").build();

                if (block == null){
                    block = new LinkedList<AsinSkuMapDO>();
                }

                if(block.size()>= 200){
                    records.add(block);
                    block = new LinkedList<AsinSkuMapDO>();
                }

                block.add(newRecord);
            }
        }

        for (List<AsinSkuMapDO> b: records) {
            asinSkuMapDOMapper.insertBatch(b);
        }
    }
}