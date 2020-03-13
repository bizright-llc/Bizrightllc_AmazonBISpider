import org.junit.jupiter.api.Test;
import org.springframework.util.StringUtils;

class TestConfig {

    @Test
    void myFirstTest() {
        System.out.println("test");
        ClassPathResource("//Users/zhucan/Downloads/person.csv");
    }


    public void ClassPathResource(String path) {
        String pathToUse = StringUtils.cleanPath(path);
        if (pathToUse.startsWith("/")) {
            pathToUse = pathToUse.substring(1);
        }
        System.out.println("pathToUse:"+pathToUse);
    }

}