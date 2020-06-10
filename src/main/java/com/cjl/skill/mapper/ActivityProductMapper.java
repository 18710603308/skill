package com.cjl.skill.mapper;

import java.util.List;

import com.cjl.skill.pojo.ActivityProduct;

public interface ActivityProductMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(ActivityProduct record);

    int insertSelective(ActivityProduct record);

    ActivityProduct selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(ActivityProduct record);

    int updateByPrimaryKey(ActivityProduct record);

	void deleteByActId(int actId);

	void insertBatch(List<ActivityProduct> aps);
	
	List<ActivityProduct> selectByActivityId(int activityId);
}