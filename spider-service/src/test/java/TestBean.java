import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.*;

class TestBean {



    /**
     * Bean注册容器校验
     */
    @Test
    void test() {

//        List<us.codecraft.webmagic.proxy.Proxy> proxies = new ArrayList<us.codecraft.webmagic.proxy.Proxy>();
//        proxies.add(new us.codecraft.webmagic.proxy.Proxy("183.215.206.39", Integer.valueOf("55443")));
//        proxies.add(new us.codecraft.webmagic.proxy.Proxy("47.112.222.157", Integer.valueOf("8000")));
//        proxies.add(new us.codecraft.webmagic.proxy.Proxy("210.22.5.117", Integer.valueOf("3128")));
//        proxies.add(new us.codecraft.webmagic.proxy.Proxy("210.5.10.87", Integer.valueOf("53281")));
//        proxies.add(new us.codecraft.webmagic.proxy.Proxy("47.99.65.77", Integer.valueOf("3128")));
        Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("47.99.65.77", Integer.valueOf("3128")));
        try {
            URLConnection httpCon = new URL("https://www.amazon.com/").openConnection(proxy);
            httpCon.setConnectTimeout(5000);
            httpCon.setReadTimeout(5000);
            int code = ((HttpURLConnection) httpCon).getResponseCode();
            System.out.println("ip [{}] port[{}] code [{}]"+code);
        } catch (IOException e) {
            System.out.println("连接失败");
        }

    }

}