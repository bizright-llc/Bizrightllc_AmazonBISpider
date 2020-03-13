package com.spider.amazon.service.impl;


import cn.hutool.json.JSONUtil;
import com.spider.amazon.config.AmazonVcConfiguration;
import com.spider.amazon.remote.api.AmzVcAPI;
import com.spider.amazon.service.IAmzVcHttpService;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Map;

/**
 * @ClassName AmzVcHttpServiceImpl
 * @Description Amazon VC Http请求服务
 */
@Service
@Slf4j
public class AmzVcHttpServiceImpl implements IAmzVcHttpService {


    private static CloseableHttpClient httpClient;

    static {
        PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
        cm.setMaxTotal(100);
        cm.setDefaultMaxPerRoute(20);
        cm.setDefaultMaxPerRoute(50);
        httpClient = HttpClients.custom().setConnectionManager(cm).build();
    }

    @Autowired
    private AmazonVcConfiguration amazonVcConfiguration;


    private void buildParams(Map<String, Object> params) {
        params.put("Cookie", "session-id-time-vcna=2082758401l; vg-vcna=2541670; i18n-prefs=USD; regStatus=pre-register; s_dslv=1574068621389; s_fid=6FF184674BB25A7B-2516179A65261821; s_vn=1605604591462%26vn%3D1; sid=\"ZebXhV9cFrXyLag6ZJUHbA==|6ThqtW5Dw5eyfHpwZGaIJxECUFQGTLAvsyi817TsTgg=\"; ubid-main=134-6498085-4423910; session-id=143-6813053-7895842; session-id-vcna=134-7750426-8415946; ubid-vcna=131-7486202-4974847; x-wl-uid=1Rkcz++HkolZ8Ai8qMuiBSoRI0LsVMydF6svtWQnLNh1SGFHjpToTvgB+2s76f/UkrXodAGivCU402WXMjlHxN9b0hAoPH16HImBzEElFjKL5FMJIZNU0zz7Myvs0ixc/09AJu31FdL8=; s_pers=%20s_fid%3D4941B3CE99CFA3A6-004B25E87A3F3E9D%7C1731288161377%3B%20s_dl%3D1%7C1578536400382%3B%20gpv_page%3DUS%253ASC%253A%2520SellerCentralLogin%7C1578536400398%3B%20s_ev15%3D%255B%255B%2527Typed/Bookmarked%2527%252C%25271578534600409%2527%255D%255D%7C1736387400409%3B; sst-main=Sst1|PQHFwsTT2nF-uWXZk240tR8WC8XE8X6uoNIgsz2B2gYeMaLkMQp8Hdt9zEOxxp9aXRIWZJmEMwQEAB6RCob-cwB1ilhDZL8lr5XGo-5kmAjzpXwHa7tS_GYxUYD28ocEohkzmO14iW5_pl62a4dSg02XDbUgOh-nc-ZavDHISlVjJP7qPkw09ly6x6U_Fde8AqHmSc2kc0JHXWhpupoki_U0dpXlKEoRZpNXCd44wKKLrRobGUlCFiF5fLiNvpK8I-iROCwD5PKsAGs4h7tSPQggmiV0ViMNWMaROTDzc8heUTBSaQSE_6OpOy5L-Rg4qGVBW0RbjWd2D9P2hhL38ufkZA; x-vcna=\"7N1gU1Q688FzxOZByhGYKx1?3a7SuBvLmMb5vhYAVVVq1z@Szo?7Wjjs0?UcwzCf\"; x-main=\"gJOufpCeGorycasrHYm7AYsH7?4tTLej6mjXdu9@SkQCgt6dtLNVyxx1jUVnFJGm\"; at-main=Atza|IwEBIPgGpSVeV1sTNaKqbJ9gyXE9IKxEsAxdfrwR_tMFTwHkiG05qcuVSoCpUGTJe7H815R0OiM0s13H-SwN2q3QkFxMDVDYnTBOQlLuDz7zhVJvKcEkmMPE-onkwhjO0Q1Y9HEbT6Ncj1ZeduhFEt6NMQeXnu8C3BACydHzcYlVWIfHv-C1rT9f5FPTzLyA19_OF6NbJq1gi8fD0cNaA3mM0Wl9d28sxiIeMBzvVoU7Zhf4T14sqnjyaCKRlemHDO4frksiUE9seD52gOoOORPZSKPhjSHXla83Sq4W-NKQ787HgoaoVy0NYX3bJe_6eJPFaA6O25A_9tIqWCp0HD3Fz0yqcV3_Xd9xyFUmsLFrTWeTyeh1cG8WzJ5SsQcckwaseB72RRFKPr1Ms8SSYLhHwiERDA95gNtXyepqeJmIRCIuEQ; sess-at-main=\"s1JwE5GR7VgDn9PLYp9n0FKY8LUGa+WDtK0a13+Xf8o=\"; session-id-time=2082787201l; lcvc-acbna=en_US; session-token=\"2XZybNS6SdrVYGxSf0upYN6QCvBYL5CNrUBUhwsy0UydeIJoP0/xoUtpkbvdhh0WnL2sDJEuCGFjNxI3PlNA0OemjfFdM/aIfu8vkGtzOT4rxlR1NW0W2A/3r9USfie2tW/q8wQkcR9qPeqrsyljPk9a2hvKZwn4ByJdMgChVpe9pmjx9xiM7tiU8OD0+PH9m7Uumh97+npiygNsQjST4YmnN5iJCkOYoIppxRlzGmk=\"; lc-main=en_US; csm-hit=tb:W2Z5ANDARFW1F7NB1EY5+s-YEKWT5JP7771KPV3JV4J|1583371891966&t:1583371891966&adb:adblk_no");
        params.put("RequestBody", JSONUtil.parseObj("{\"action\":\"RECORDS_PER_PAGE_CHANGED\",\"pageNumber\":1,\"recordsPerPage\":30,\"sortedColumnId\":\"promotionId\",\"sortOrder\":\"ASCENDING\",\"searchText\":\"\",\"tableId\":\"promotion-list\",\"filters\":[{\"filterGroupId\":\"promotionType\",\"filterCriteria\":[{\"value\":\"true\",\"filterId\":\"all\"}]},{\"filterGroupId\":\"promotionStatus\",\"filterCriteria\":[{\"value\":\"true\",\"filterId\":\"all\"}]},{\"filterGroupId\":\"startDateAfter\",\"filterCriteria\":[{\"value\":\"true\",\"filterId\":\"all\"}]},{\"filterGroupId\":\"startDateBefore\",\"filterCriteria\":[{\"value\":\"true\",\"filterId\":\"all\"}]},{\"filterGroupId\":\"endDateAfter\",\"filterCriteria\":[{\"value\":\"true\",\"filterId\":\"all\"}]},{\"filterGroupId\":\"endDateBefore\",\"filterCriteria\":[{\"value\":\"true\",\"filterId\":\"all\"}]},{\"filterGroupId\":\"promotionEvent\",\"filterCriteria\":[{\"value\":\"true\",\"filterId\":\"all\"}]}],\"clientState\":{}}"));
        params.put("csrfToken","gnGYEPw6kvmEZZSoiQpQ%2B84MynM5hS8Bp8i910YAAAABAAAAAF5gVm1yYXcAAAAAFVfwLBerPxe%2F4nuL9RKf");
        params.put("ref_","xx_xx_cont_xx");
    }

    @Override
    public String callVcPromotionsListInfo(Map<String, Object> params) {

        // 自建参数
//        buildParams(params);

        CloseableHttpResponse response = null;
        BufferedReader in = null;
        String result = "";
        try {
            final String uri = AmzVcAPI.PROMOTIONS_LIST.replace("{csrfToken}",String.valueOf(params.get("csrfToken")))
                .replace("{ref_}",String.valueOf(params.get("ref_")));

            HttpPost httpPost = new HttpPost(amazonVcConfiguration.getServer() + uri);
            RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(30000).setConnectionRequestTimeout(30000).setSocketTimeout(30000).build();
            httpPost.setConfig(requestConfig);
            httpPost.setConfig(requestConfig);
            httpPost.addHeader("Content-type", "application/json");
            httpPost.setHeader("Cookie", params.get("Cookie").toString());
            httpPost.setEntity(new StringEntity(params.get("RequestBody").toString(), Charset.forName("UTF-8")));
            response = httpClient.execute(httpPost);
            in = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
            StringBuffer sb = new StringBuffer("");
            String line = "";
            String NL = System.getProperty("line.separator");
            while ((line = in.readLine()) != null) {
                sb.append(line + NL);
            }
            in.close();
            result = sb.toString();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (null != response) {
                    response.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }
}
