package com.spider.amazon.utils;


import com.spider.amazon.entity.Cookie;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

/**
 * Cookie工具类
 */
@Component
@Slf4j
public class CookiesUtils {

    /**
     * 环境变量
     */
    @Autowired
    private Environment environment;


    /**
     * 从环境变量读取变量值处理
     *
     * @param cookiesName
     * @return
     * @desc key1=value1;key2=value2  trans to  key,value Sets
     */
    public static Set<Cookie> keyValueCookies2CookiesSet(String cookiesName, String split1, String split2) {
        log.info("start [keyValueCookies2CookiesSet]");

        // 读取变量名
//        String cookiesStr = environment.getProperty(cookiesName);
        // TODO Local Test Code Start
        String cookiesStr = "session-id-vcna=146-0519556-7974555; lcvc-acbna=en_US; session-id-time-vcna=2082758401l; lc-main=en_US; ubid-main=131-6886633-6836839; session-id=146-0519556-7974555; ubid-vcna=131-6886633-6836839; sid=\"ZebXhV9cFrXyLag6ZJUHbA==|6ThqtW5Dw5eyfHpwZGaIJxECUFQGTLAvsyi817TsTgg=\"; session-id-time=2082787201l; vg-vcna=2541670; x-vcna=\"f606VBeaM56dXSlOKc8DE6QkJ@QZrGfsBzxTtAJYP2?rfS7?bFNlcIAGzi1q3o0O\"; i18n-prefs=USD; sp-cdn=\"L5Z9:CN\"; session-token=\"SYv42EnGzx1qea3uZDbI70CfxPeulbOUmCADiSgZDY/lCyFO2Mh65foNa9Bw+eXMY0HN+s7jg2vTURm1s+Tp5h5oy5scAPRFB64TMBmttZWnLClDAoJXUmi4b6Sb2CL04WxImsTMajng0m+5lOOQBqK7kvpmsxb4Ew8xp2XK0lEX85KOaNUeoNl1m+VGLemmPC/MAok3wyCnOUoTaVZFtw==\"; session-token=\"YSI06b7F/nRy3So4YiLhGfpXjiAEa+vTPLV6zBxygZiWObW2OuJLmewa75sJCHSDTcjJ8VM9n1I9uS8SWI9nJO66PptY28s03cmVFNZ/hlJXihKBi25rD1lQgObmAqWdStasguqJZBABO3T4rY3b8jSQsg+la4u4ppWMLFLrfjqHpzVTPG4+01TRvnpjeeSWwiIrboNCovb4GZRwrPeB4t8plJ/prn9HEDbguojA7as=\"; x-main=\"??8ShrnWNNgR5yce3GMeeRUtzHV1hL?VTlDSOHGDw9S52bwr1JM7m36MGxQ7D63f\"; at-main=Atza|IwEBIHytmFSzk8lM5d4FY2TcgAxia_sec9sOKNtkpoAcJsOmZ4ZvF18PQUNdt1hYJbvWbudRIIS2N-_xe1v8WCx8AaRmro29H_-xBhLrwJSP1Wmt-TqbOLrLifuXYEWNoRvV2Sm_N9JhOzaXtkwz5TEPI6XFQQWP4GNGVaOs10qHLozob_K-I0FD-1Yw7Ky-tkJBoielGHbmJdvGBsuwRSBfUq79lpBTLE1EVXR1anVcIasbYufIoNYqY0dcYoEcjJtV8SKFzpwYB30LNfrK8dzN1l5YZYVopPQ8eBfGIPp5b7OebNDC1Zyrz14xdy_bERaX4o7nkO8Fd_MrqzXfcD7DodwNDrW0fppwUzaX_NDwv0JYHe4Jp5DpIVGPekoSoqCwoB488NES04l4ZOBBhwvm-IwWZh1FrQZVyROlqYJu36vJaQ; sess-at-main=\"WCPwxB4RlY1EVKfNG4DMf2WAkpiqWZbk75Re7kTsqKM=\"; csm-hit=88ZKKJH0FZEHCWX7DDRB+s-XS1QHT8EKZZFSBG97G5Z|1572335029424";
        // split1一次切分
        String[] cookiesList = cookiesStr.split(";");
        // splist2二次切分
        Set<Cookie> cookies = new HashSet<>();
        for (String str : cookiesList) {
            Cookie cookie = new Cookie();
            int strindex = str.trim().indexOf("=");
            cookie.setName(str.trim().substring(0, strindex));
            cookie.setValue(str.trim().substring(strindex));
            cookies.add(cookie);
        }
        // TODO Local Test Code End

        return cookies;
    }
//
//    public List<Cookie> chromeJsonCookis2CookieList(Json) {
//
//
//
//
//        return null;
//    }
    public static void main(String[] args) {

        CookiesUtils cookiesUtils = new CookiesUtils();
        Set<Cookie> cookies = cookiesUtils.keyValueCookies2CookiesSet("amazon.vc.freelogin.cookies", ";", "=");

        for (Cookie cookie : cookies) {
            System.out.println("name:" + cookie.getName() + " value:" + cookie.getValue());
        }

        return;
    }
}
