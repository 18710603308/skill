package com.cjl.skill.service;

import com.cjl.skill.pojo.Activity;

public interface SkillManageService {
	//数据初始化
	void init();

	int getOrderCountByProductId(int productId);

	int getStockByProductId(int productId);

	void saveActivity(Activity activity, int[] pids);

}
