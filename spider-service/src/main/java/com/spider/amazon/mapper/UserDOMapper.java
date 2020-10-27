package com.spider.amazon.mapper;

import com.spider.amazon.model.UserDO;
import org.apache.catalina.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
@Mapper
public interface UserDOMapper {

	UserDO getUserAccountByUserId(@Param("userId")Long userId);

    int deleteByPrimaryKey(Integer userid);

    int insert(User record);

    int insertSelective(User record);

    User selectByPrimaryKey(Integer userid);

    int updateByPrimaryKeySelective(User record);

    int updateByPrimaryKey(User record);

	List<Object> getUserAccount(Map<String, Object> map);

	int addUserAccount(Map<String, Object> map);

	int editUserAccount(Map<String, Object> map);

	int delUserAccount(Map<String, Object> map);

	User login(String userName, String password);

	List<Map<String, Object>> selectLabelList(String userName);

	List<Object> getAccountByuser(Map<String, Object> map);
}