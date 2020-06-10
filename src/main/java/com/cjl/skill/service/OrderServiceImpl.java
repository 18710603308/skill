package com.cjl.skill.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import com.cjl.skill.exception.OrderFailException;
import com.cjl.skill.mapper.OrderMapper;
import com.cjl.skill.mapper.ProductMapper;
import com.cjl.skill.pojo.Order;
import com.cjl.skill.util.ConstantPrefixUtil;
import com.cjl.skill.util.JsonUtil;

@Service
public class OrderServiceImpl implements OrderService {
	private Logger logger = LoggerFactory.getLogger(OrderServiceImpl.class);
	
	@Autowired
	private OrderMapper orderMapper;

	@Autowired
	private ProductMapper productMapper;
	
	@Autowired
	private StringRedisTemplate stringRedisTemplate;


	@Override
	public int add(Order order) {
		return orderMapper.insertSelective(order);
	}

	/**
	 * 压测核心业务逻辑：下单、减库存
	 */
	@Transactional(isolation =Isolation.DEFAULT,  propagation = Propagation.REQUIRED)
	@Override
	public Order createSkillOrder(Order order) {
		try {
			// 扣减库存时，同时做判断，需要修改sql语句
			
			//syncoostock--; stock = 10; stock = 10-1; 赋值
			if(productMapper.decreaseStock(order.getProductId())==0) { 
				order.setId(0);
				return order;
			}
			//if ("1".equals("1")) throw new OrderFailException(); 
			// 成功就下单
			orderMapper.insertSelective(order);
			//在redis缓存里设置一个下单成功标记
			stringRedisTemplate.opsForValue().set(
					ConstantPrefixUtil.REDIS_ORDER_SUCCESS_FLAG_PREFIX+order.getUserId()+":"+order.getProductId(), JsonUtil.obj2String(order));
			return order;
		} catch (Exception e) {
			//e.printStackTrace();
			//logger.debug("order fail with userid {} and productId {}",order.getUserId(),order.getProductId());
			logger.error("order fail with userid "+order.getUserId()+" and productId "+order.getProductId()+"_"+e.getMessage(), e);
			throw new OrderFailException(); 
		}finally {
			//无论成功还是失败，都清除排队标记
			//清除订单排队标记，防止死锁
			stringRedisTemplate.delete(ConstantPrefixUtil.REDIS_ORDER_QUEUE_FLAG_PREFIX + order.getUserId() + ":" + order.getProductId());
		}
	}

	@Override
	public Order getOrderByUserAndProductId(int userId, int productId) {
		return orderMapper.selectByUserAndProductId(userId,productId);
	}
}
