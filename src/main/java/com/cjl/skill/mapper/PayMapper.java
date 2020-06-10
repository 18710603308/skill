package com.cjl.skill.mapper;

import com.cjl.skill.pojo.Pay;

public interface PayMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(Pay record);

    int insertSelective(Pay record);

    Pay selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Pay record);

    int updateByPrimaryKey(Pay record);

    //删除所有
	void deleteAll();
}