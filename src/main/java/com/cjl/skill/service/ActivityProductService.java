package com.cjl.skill.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.cjl.skill.mapper.ActivityProductMapper;
import com.cjl.skill.pojo.ActivityProduct;

@Service
public class ActivityProductService {
	@Autowired
	private ActivityProductMapper activityProductMapper;

	public List<ActivityProduct> getByActId(int actId) {
		return activityProductMapper.selectByActivityId(actId);
	}
	
}
