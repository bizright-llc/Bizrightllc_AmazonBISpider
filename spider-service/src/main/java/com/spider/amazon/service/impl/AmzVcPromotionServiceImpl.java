package com.spider.amazon.service.impl;


import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.common.exception.ServiceException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.spider.amazon.config.AmazonVcConfiguration;
import com.spider.amazon.cons.DateFormat;
import com.spider.amazon.cons.RespErrorEnum;
import com.spider.amazon.entity.PromotionList;
import com.spider.amazon.service.IAmzVcHttpService;
import com.spider.amazon.service.IAmzVcPromotionService;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.AbstractJavaTypeMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @ClassName AmzVcPromotionServiceImpl
 * @Description Amazon VC Promotion服务
 */
@Service
@Slf4j
public class AmzVcPromotionServiceImpl implements IAmzVcPromotionService {

    @Autowired
    private IAmzVcHttpService amzVcHttpServiceImpl;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private Environment env;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AmazonVcConfiguration amazonVcConfiguration;

    // 控制最大循环值，防止死循环
    private final static int MAX_SIZE=2;

    /**
     * 获取Promotion信息入库
     * @param params
     */
    @Override
    public void scrapyPromotionInfo(Map<String, Object> params) {

        // 组装请求参数
        buildParams(params);

        // 0.参数值
        String crawId= DateUtil.format(DateUtil.date(), DateFormat.YEAR_MONTH_DAY_yyyyMMddHHmmss1);
        int currentNum=0;
        Map<String, Object> docParams;
        do {
            currentNum++;
            try  {
                // 模拟请求Vc获取Promotion List列表
                String result=amzVcHttpServiceImpl.callVcPromotionsListInfo(params);
                if (ObjectUtil.isEmpty(result)) { // 结果为空
                    break;
                }

                // 解析节点获取所需数据
                docParams = parsingData(result);

                // 异步调用Promotion详情抓取
                if (ObjectUtil.isEmpty(docParams.get("currentRecordCount"))|| (int) docParams.get("currentRecordCount")==0) {
                    break;
                }
                callAmazonVcPrommotionDetailSpider((List<PromotionList>) docParams.get("promotionList"),crawId);

                // 获取下一页数据数据设置 （需要切换下一个页面的数据）
                setNextPageSetting(docParams , params);

            } catch (Exception e) {
                e.printStackTrace();
                break;
            }
        } while (currentNum < MAX_SIZE && hasNextPage(docParams) ) ;

    }

    /**
     * 解析节点获取所需数据
     */
    private Map<String,Object> parsingData(String result) {
        Map<String,Object> docParams= new HashMap<>();

        // 字符串转换Doc节点
        Document doc = Jsoup.parse(result);

        // currentPage
        Elements currentPageEle = doc.select("#currentPage");
        docParams.put("currentPage",currentPageEle.get(0).text());
        // recordCount
        Elements recordCountEle = doc.select("#recordCount");
        docParams.put("recordCount",recordCountEle.get(0).text());
        // recordCount
        Elements recordsPerPageEle = doc.select("#recordsPerPage");
        docParams.put("recordsPerPage",recordsPerPageEle.get(0).text());

        List<PromotionList> promotionList=new ArrayList<>();
        // 选择Script中的表格元素
        Elements itemRecordsScriptEle = doc.select("#itemRecords").get(0).select("script");
        String itemRecordsScriptSt=itemRecordsScriptEle.get(0).data();
        itemRecordsScriptSt= "<html><head></head><body><table>"+itemRecordsScriptSt+"</table></body></html>";
        Document tableDoc =Jsoup.parseBodyFragment(itemRecordsScriptSt);
        Elements promotionListEle = tableDoc.select("tr");
        if (ObjectUtil.isEmpty(promotionListEle)) {
            docParams.put("currentRecordCount",0);
            return docParams;
        } else {
            docParams.put("currentRecordCount",promotionListEle.size());
        }

        for ( Element promotion:promotionListEle) {
            promotionList.add(PromotionList.builder()
                    .promotionId(promotion.id())
                    .promotionDetailUrl(amazonVcConfiguration.getServer()+promotion.select("a").attr("href"))
                    .build());
        }

        docParams.put("promotionList",promotionList);

        return docParams;
    }

    /**
     * 异步队列调用亚马逊详情爬虫抓取
      */
    private void callAmazonVcPrommotionDetailSpider(List<PromotionList> promotionList,String crawId) {
        if (CollectionUtil.isEmpty(promotionList)) {
            return;
        }

        Map<String,Object> params=new HashMap<>();
        params.put("promotionList", promotionList);
        params.put("crawId", crawId);
        log.debug("params=>[{}]",params);
        try {
            // 异步队列
            rabbitTemplate.setMessageConverter(new Jackson2JsonMessageConverter());
            rabbitTemplate.setExchange(env.getProperty("promotion.exchange.name"));
            rabbitTemplate.setRoutingKey(env.getProperty("promotion.routing.key.name"));
            Message message= MessageBuilder.withBody(objectMapper.writeValueAsBytes(params)).setDeliveryMode(MessageDeliveryMode.PERSISTENT).build();
            message.getMessageProperties().setHeader(AbstractJavaTypeMapper.DEFAULT_CONTENT_CLASSID_FIELD_NAME, MessageProperties.CONTENT_TYPE_JSON);
            rabbitTemplate.convertAndSend(message);
        } catch (Exception e) {
            throw new ServiceException(RespErrorEnum.TASK_DEAL_ERROR.getSubStatusCode(),RespErrorEnum.TASK_DEAL_ERROR.getSubStatusMsg());
        }


    }

    /**
     * 更新下一个页面请求数据
     * @param docParams
     * @param params
     */
    private boolean setNextPageSetting(Map<String,Object> docParams,Map<String, Object> params) {

        try {
            // 更新请求体里面的页码字段
            JSONObject reqBody = (JSONObject) params.get("RequestBody");
            int pageNumber= (int) reqBody.get("pageNumber");
            reqBody.put("pageNumber",++pageNumber);
            params.put("RequestBody",reqBody);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * 是否存在下一页数据
     * @param docParams
     * @return
     */
    private boolean hasNextPage(Map<String,Object> docParams) {
        if (ObjectUtil.isEmpty(docParams)) {
            return false;
        }

        // 当前请求页码及页数
        // currentPage
        int currentPage =ObjectUtil.isEmpty (docParams.get("currentPage"))? 0: Integer.parseInt(String.valueOf(docParams.get("currentPage")));
        // recordCount
        int recordCount = ObjectUtil.isEmpty ( docParams.get("recordCount"))? 0: Integer.parseInt(String.valueOf(docParams.get("recordCount")));
        // recordsPerPage
        int recordsPerPage =  ObjectUtil.isEmpty (docParams.get("recordsPerPage"))? 0: Integer.parseInt(String.valueOf(docParams.get("recordsPerPage")));

        if (currentPage*recordsPerPage<recordCount) {
            return true;
        }

        return false;
    }

    private void buildParams(Map<String, Object> params) {
        params.put("Cookie", "session-id-time-vcna=2082758401l; vg-vcna=2541670; i18n-prefs=USD; regStatus=pre-register; s_dslv=1574068621389; s_fid=6FF184674BB25A7B-2516179A65261821; s_vn=1605604591462%26vn%3D1; sid=\"ZebXhV9cFrXyLag6ZJUHbA==|6ThqtW5Dw5eyfHpwZGaIJxECUFQGTLAvsyi817TsTgg=\"; ubid-main=134-6498085-4423910; session-id=143-6813053-7895842; session-id-vcna=134-7750426-8415946; ubid-vcna=131-7486202-4974847; x-wl-uid=1Rkcz++HkolZ8Ai8qMuiBSoRI0LsVMydF6svtWQnLNh1SGFHjpToTvgB+2s76f/UkrXodAGivCU402WXMjlHxN9b0hAoPH16HImBzEElFjKL5FMJIZNU0zz7Myvs0ixc/09AJu31FdL8=; s_pers=%20s_fid%3D4941B3CE99CFA3A6-004B25E87A3F3E9D%7C1731288161377%3B%20s_dl%3D1%7C1578536400382%3B%20gpv_page%3DUS%253ASC%253A%2520SellerCentralLogin%7C1578536400398%3B%20s_ev15%3D%255B%255B%2527Typed/Bookmarked%2527%252C%25271578534600409%2527%255D%255D%7C1736387400409%3B; sst-main=Sst1|PQHFwsTT2nF-uWXZk240tR8WC8XE8X6uoNIgsz2B2gYeMaLkMQp8Hdt9zEOxxp9aXRIWZJmEMwQEAB6RCob-cwB1ilhDZL8lr5XGo-5kmAjzpXwHa7tS_GYxUYD28ocEohkzmO14iW5_pl62a4dSg02XDbUgOh-nc-ZavDHISlVjJP7qPkw09ly6x6U_Fde8AqHmSc2kc0JHXWhpupoki_U0dpXlKEoRZpNXCd44wKKLrRobGUlCFiF5fLiNvpK8I-iROCwD5PKsAGs4h7tSPQggmiV0ViMNWMaROTDzc8heUTBSaQSE_6OpOy5L-Rg4qGVBW0RbjWd2D9P2hhL38ufkZA; x-vcna=\"7N1gU1Q688FzxOZByhGYKx1?3a7SuBvLmMb5vhYAVVVq1z@Szo?7Wjjs0?UcwzCf\"; x-main=\"gJOufpCeGorycasrHYm7AYsH7?4tTLej6mjXdu9@SkQCgt6dtLNVyxx1jUVnFJGm\"; at-main=Atza|IwEBIPgGpSVeV1sTNaKqbJ9gyXE9IKxEsAxdfrwR_tMFTwHkiG05qcuVSoCpUGTJe7H815R0OiM0s13H-SwN2q3QkFxMDVDYnTBOQlLuDz7zhVJvKcEkmMPE-onkwhjO0Q1Y9HEbT6Ncj1ZeduhFEt6NMQeXnu8C3BACydHzcYlVWIfHv-C1rT9f5FPTzLyA19_OF6NbJq1gi8fD0cNaA3mM0Wl9d28sxiIeMBzvVoU7Zhf4T14sqnjyaCKRlemHDO4frksiUE9seD52gOoOORPZSKPhjSHXla83Sq4W-NKQ787HgoaoVy0NYX3bJe_6eJPFaA6O25A_9tIqWCp0HD3Fz0yqcV3_Xd9xyFUmsLFrTWeTyeh1cG8WzJ5SsQcckwaseB72RRFKPr1Ms8SSYLhHwiERDA95gNtXyepqeJmIRCIuEQ; sess-at-main=\"s1JwE5GR7VgDn9PLYp9n0FKY8LUGa+WDtK0a13+Xf8o=\"; session-id-time=2082787201l; lcvc-acbna=en_US; lc-main=en_US; session-token=\"EE6NU55Xy8BVMkQ4cqJ83HyT/1LpDnKHLvSrbeyHDEhufal9cpPPhPsasZ+0Zfl0bwO2v4N95jM2tXDT147Fot8WR0rdcLAg1+0d/dDJPdV23amakPRjsCeFg476PDFFn+BTPK1VGLpZPktK5QldKpo4upUsDKMZpVC6Yo3GZWxeSOcAf9CqEB5Yplcx5jtL8OfZDzMwA+FMjpOH0Koxz5gLqQxMjXlEL1JCWtAPd9g=\"; csm-hit=tb:W2Z5ANDARFW1F7NB1EY5+s-R0H6AB059TEGXQ8HY4YG|1583721791041&t:1583721791041&adb:adblk_no");
        params.put("RequestBody", JSONUtil.parseObj("{\"action\":\"PAGE_CHANGED\",\"pageNumber\":1,\"recordsPerPage\":30,\"sortedColumnId\":\"promotionId\",\"sortOrder\":\"ASCENDING\",\"searchText\":\"\",\"tableId\":\"promotion-list\",\"filters\":[{\"filterGroupId\":\"promotionType\",\"filterCriteria\":[{\"value\":\"true\",\"filterId\":\"all\"}]},{\"filterGroupId\":\"promotionStatus\",\"filterCriteria\":[{\"value\":\"true\",\"filterId\":\"all\"}]},{\"filterGroupId\":\"startDateAfter\",\"filterCriteria\":[{\"value\":\"true\",\"filterId\":\"all\"}]},{\"filterGroupId\":\"startDateBefore\",\"filterCriteria\":[{\"value\":\"true\",\"filterId\":\"all\"}]},{\"filterGroupId\":\"endDateAfter\",\"filterCriteria\":[{\"value\":\"true\",\"filterId\":\"all\"}]},{\"filterGroupId\":\"endDateBefore\",\"filterCriteria\":[{\"value\":\"true\",\"filterId\":\"all\"}]},{\"filterGroupId\":\"promotionEvent\",\"filterCriteria\":[{\"value\":\"true\",\"filterId\":\"all\"}]}],\"clientState\":{}}"));
        params.put("csrfToken","gkZKb1HIz6AbE%2FbHX0D1mcdWESFQJmnA6yvALncAAAABAAAAAF5lrTVyYXcAAAAAFVfwLBerPxe%2F4nuL9RKf");
        params.put("ref_","xx_xx_cont_xx");
    }



}
