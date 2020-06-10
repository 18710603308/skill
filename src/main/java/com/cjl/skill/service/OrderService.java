package com.cjl.skill.service;

import com.cjl.skill.pojo.Order;

public interface OrderService {
	int add(Order order);

	//创建秒杀订单
	Order createSkillOrder(Order order);

	Order getOrderByUserAndProductId(int userId, int productId);

}
