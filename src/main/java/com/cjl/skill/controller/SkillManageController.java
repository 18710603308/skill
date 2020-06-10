package com.cjl.skill.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.cjl.skill.mapper.ProductMapper;
import com.cjl.skill.pojo.Activity;
import com.cjl.skill.pojo.ActivityProduct;
import com.cjl.skill.pojo.Product;
import com.cjl.skill.service.ActivityProductService;
import com.cjl.skill.service.ProductService;
import com.cjl.skill.service.SkillManageService;
import com.cjl.skill.util.AckMessage;
import com.cjl.skill.util.ConstantPrefixUtil;

@Controller
@RequestMapping("/admin/skill")
public class SkillManageController {
	@Autowired
	private SkillManageService skillService;

	@Autowired
	private ActivityProductService activityProductService;

	@Autowired
	private ProductService productService;

	@Autowired
	private ProductMapper productMapper;

	@Autowired
	private StringRedisTemplate stringRedisTemplate;

	/**
	 * 初始化测试环境数据
	 * 
	 * @return
	 */
	@GetMapping("/init")
	public @ResponseBody Object initSkill() {
		try {
			skillService.init();
			return AckMessage.ok();
		} catch (Exception e) {
			e.printStackTrace();
			return AckMessage.error();
		}
	}

	/**
	 * 生成秒杀报告
	 * 
	 * @param
	 * @return
	 */
	@GetMapping("/report")
	public @ResponseBody Object reportSkill() {
		try {
			// 定义一个秒杀报告集合
			List<Report> reports = new ArrayList<SkillManageController.Report>();
			// 参加活动商品的集合
			List<ActivityProduct> list = activityProductService.getByActId(1);
			for (ActivityProduct ap : list) {
				int orderCount = skillService.getOrderCountByProductId(ap.getProductId());
				// 获取后动商品具体信息
				Product p = productService.getById(ap.getProductId());
				Report report = new Report();
				report.setId(p.getId());
				report.setOrderCount(orderCount);
				report.setProductStock(p.getStock());
				report.setProductName(p.getProductName());
				reports.add(report);
			}
			return AckMessage.ok(reports);
		} catch (Exception e) {
			e.printStackTrace();
			return AckMessage.error();
		}
	}

	/**
	 * 维护活动
	 */
	@PostMapping("/activity")
	public @ResponseBody Object save(Activity activity, int[] pids) {
		System.out.println(pids);
		skillService.saveActivity(activity, pids);
		return AckMessage.ok();
	}

	// 缓存预热
	@GetMapping("/load/stock")
	public @ResponseBody Object loadStock() {
		try {
			// 查数据库
			List<Product> stocks = productMapper.selectProductIdAndStock();
			for (Product product : stocks) {
				// 存到redis中
				stringRedisTemplate.opsForValue().set(
						ConstantPrefixUtil.SKILL_PRODUCT_PREFIX + String.valueOf(product.getId()),
						String.valueOf(product.getStock()));
			}
			System.out.println("秒杀商品缓存预热成功！！！！！");
			return AckMessage.ok();
		} catch (Exception e) {
			e.printStackTrace();
			return AckMessage.error();
		}
	}

	/**
	 * 报告对象
	 * 
	 * @author cjl
	 *
	 */
	class Report {
		private int id; // 商品id
		private int orderCount; // 订单
		private int productStock; // 商品的库存
		private String productName; // 商品的名称

		public int getId() {
			return id;
		}

		public void setId(int id) {
			this.id = id;
		}

		public String getProductName() {
			return productName;
		}

		public void setProductName(String productName) {
			this.productName = productName;
		}

		public int getOrderCount() {
			return orderCount;
		}

		public void setOrderCount(int orderCount) {
			this.orderCount = orderCount;
		}

		public int getProductStock() {
			return productStock;
		}

		public void setProductStock(int productStock) {
			this.productStock = productStock;
		}

	}
}
