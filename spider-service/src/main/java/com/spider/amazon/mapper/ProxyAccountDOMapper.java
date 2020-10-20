package com.spider.amazon.mapper;

import com.spider.amazon.model.ProxyAccountDO;
import com.spider.amazon.model.ProxyProvider;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Mapper
public interface ProxyAccountDOMapper {

    /**
     * Get account by id
     * @param id
     * @return
     */
    ProxyAccountDO getAccountById(@Param("id") Long id);

    /**
     * Return all account
     * @return
     */
    List<ProxyAccountDO> getAllAccount();

    /**
     * Return all accounts which is active and not been remove
     *
     * @return
     */
    List<ProxyAccountDO> getAllActiveAccount();

    /**
     * Return all account from provider
     *
     * @param provider
     * @return
     */
    List<ProxyAccountDO> getAllAccountByProvider(ProxyProvider provider);

    /**
     * insert new account information
     * every provider cannot have same username account
     *
     * @param proxyAccountDO
     */
    void insertSelective(ProxyAccountDO proxyAccountDO);

    /**
     * Update account information
     *
     * @param proxyAccountDO
     */
    void updateAccount(ProxyAccountDO proxyAccountDO);

    void disActiveAccount(@Param("id") Long id);

    /**
     * Remove account with id
     * @param id
     */
    void deleteAccount(@Param("id") Long id);

}
