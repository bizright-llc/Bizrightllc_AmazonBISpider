package com.spider.amazon.mapper;

import com.spider.SpiderServiceApplication;
import com.spider.amazon.model.ProxyAccountDO;
import com.spider.amazon.model.ProxyProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openqa.selenium.Proxy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes= SpiderServiceApplication.class)
class ProxyAccountDOMapperTest {

    @Autowired
    private ProxyAccountDOMapper proxyAccountDOMapper;

    @Test
    void testInsert() {
        ProxyAccountDO newAccount = new ProxyAccountDO();

        newAccount.setUsername("test");
        newAccount.setProvider(ProxyProvider.TEST);

        proxyAccountDOMapper.insertSelective(newAccount);

        assertTrue(newAccount.getId() == 2);

    }

    @Test
    void updateAccount() {

        ProxyAccountDO accountDO = new ProxyAccountDO();
        accountDO.setId(1l);
        accountDO.setToken("12344321");

        proxyAccountDOMapper.updateAccount(accountDO);

        assertTrue(accountDO.getUsername().equals("test"));

    }

    @Test
    void disActiveAccount() {

        proxyAccountDOMapper.disActiveAccount(2l);

        ProxyAccountDO account = proxyAccountDOMapper.getAccountById(2l);

        assertTrue(account.getActive() == false);
    }

    @Test
    void deleteAccount() {

        proxyAccountDOMapper.deleteAccount(2l);

        List<ProxyAccountDO> accounts = proxyAccountDOMapper.getAllAccount();

        assertTrue(accounts.size() == 0);

    }
}