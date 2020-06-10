package com.cjl.skill.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.cjl.skill.mapper.ActivityMapper;
import com.cjl.skill.mapper.ActivityProductMapper;
import com.cjl.skill.mapper.OrderMapper;
import com.cjl.skill.mapper.PayMapper;
import com.cjl.skill.mapper.ProductMapper;
import com.cjl.skill.pojo.Activity;
import com.cjl.skill.pojo.ActivityProduct;

@Service
public class SkillManageServiceImpl implements SkillManageService {
	@Autowired
	private ActivityMapper activityMapper;

	@Autowired
	private ActivityProductMapper activityProductMapper;

	@Autowired
	private ProductMapper productMapper;

	@Autowired
	private OrderMapper orderMapper;

	@Autowired
	private PayMapper payMapper;

	/**
	 * 初始化秒杀环境数据
	 */
	@Override
	public void init() {
		//删除订单和支付信息
		orderMapper.deleteAll();
		payMapper.deleteAll();

		//恢复初始数据
		productMapper.renewStock();
		
	}

	/**
	 * 获取订单数量
	 */
	@Override
	public int getOrderCountByProductId(int productId) {
		return orderMapper.selectCountByProductId(productId);
	}

	/**
	 * 获取商品库存
	 */
	@Override
	public int getStockByProductId(int productId) {
		return productMapper.selectStockById(productId);
	}

	/**
	 * 修改保存秒杀活动信息
	 */
	@Override
	public void saveActivity(Activity activity, int[] pids) {

		activityMapper.updateByPrimaryKeySelective(activity);

		activityProductMapper.deleteByActId(activity.getId());

		if (pids!=null&&pids.length>0) {
			List<ActivityProduct> aps = new ArrayList<ActivityProduct>();
			for (int s : pids) {
				ActivityProduct ap = new ActivityProduct();
				ap.setActivityId(activity.getId());
				ap.setProductId(s);
				ap.setCreateTime(new Date());
				aps.add(ap);
			}
			activityProductMapper.insertBatch(aps);
		}
	}

}
