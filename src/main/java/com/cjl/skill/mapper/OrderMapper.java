package com.cjl.skill.mapper;

import org.apache.ibatis.annotations.Param;

import com.cjl.skill.pojo.Order;

public interface OrderMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(Order record);

    int insertSelective(Order record);

    Order selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Order record);

    int updateByPrimaryKey(Order record);

	int deleteByUserId(int userId);

	//custom
	int deleteAll();

	int selectCountByProductId(int productId);

	Order selectByUserAndProductId(@Param("userId") int userId,@Param("productId") int productId);
}