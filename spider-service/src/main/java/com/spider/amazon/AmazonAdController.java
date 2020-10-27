package com.spider.amazon;

import com.spider.amazon.dto.AmazonAdConsumeSettingDTO;
import com.spider.amazon.service.AmazonAdService;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/amazonad")
public class AmazonAdController {

    private AmazonAdService amazonAdService;

    public AmazonAdController(AmazonAdService amazonAdService) {
        this.amazonAdService = amazonAdService;
    }

    @RequestMapping("/all")
    public List<AmazonAdConsumeSettingDTO> getAllSettings(){
        return amazonAdService.getAllSetting();
    }

}
