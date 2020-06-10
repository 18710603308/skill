package com.cjl.skill.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.cjl.skill.mapper.ActivityMapper;
import com.cjl.skill.pojo.Activity;

@Service
public class ActivityService {
	@Autowired
	private ActivityMapper activityMapper;
	
	public Activity getOne(int productId) {
		return activityMapper.selectByPrimaryKey(1);
	}
}
