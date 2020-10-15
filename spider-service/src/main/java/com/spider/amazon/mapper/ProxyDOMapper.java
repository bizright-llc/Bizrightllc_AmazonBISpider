package com.spider.amazon.mapper;

import com.spider.amazon.dto.ProxyDTO;
import com.spider.amazon.model.ProxyDO;
import com.spider.amazon.model.IpPoolDOKey;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository to database table "IpPool"
 */
@Repository
@Mapper
public interface ProxyDOMapper {

    /**
     * Get proxy is active
     * @return
     */
//    List<ProxyDO> getActiveProxy();

    /**
     * Get random proxy which is active
     *
     * @return
     */
    ProxyDO getRandomActiveProxy();

    @Select({"Select top ${count} * from IpPool where Active = 1 and (ExpireAt is null or ExpireAt > getdate()) order by newid()"})
    List<ProxyDO> getRandomActiveProxiesCount(@Param("count") int count);

    ProxyDO getProxyByIpAndPort(@Param("ip") String ip, @Param("port") String port);

//    boolean isExist(@Param("ip") String ip, @Param("port") String port);

    int insert(ProxyDO record);

    int insertSelective(ProxyDO record);

//    int insertBatch(List<ProxyDO> recordList);

    void update(ProxyDO proxyDO);

    /**
     * Update ip pool
     * @param recordList
     */
//    void updateBatch(List<ProxyDO> recordList);

    /**
     * Mark used time
     * @param id
     */
    void markProxyUsedTime(@Param("id")Long id);

    /**
     * Mark proxies used by ids
     *
     * @param ids
     */
    void markProxiesUsedTime(List<Long> ids);
//
//    void setProxyActive(@Param("id")Long id);
//
//    void setProxyNonActive(@Param("id")Long id);
//
//    void deleteById(@Param("id")Long id);

}