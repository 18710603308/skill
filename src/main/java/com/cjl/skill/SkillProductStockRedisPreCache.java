package com.cjl.skill;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import com.cjl.skill.mapper.ProductMapper;
import com.cjl.skill.pojo.Product;
import com.cjl.skill.util.ConstantPrefixUtil;

@Component
public class SkillProductStockRedisPreCache implements CommandLineRunner {
	@Autowired
	private ProductMapper productMapper;
	
	@Autowired
	private StringRedisTemplate stringRedisTemplate;
	
	//缓存预热
	@Override
	public void run(String... args) throws Exception {
		
		//查数据库
		List<Product> stocks = productMapper.selectProductIdAndStock();
		for (Product product : stocks) {
			//存到redis中
			stringRedisTemplate.opsForValue().set(ConstantPrefixUtil.SKILL_PRODUCT_PREFIX+String.valueOf(product.getId()), String.valueOf(product.getStock()));
		}
		
		System.out.println("秒杀商品缓存预热成功！！！！！");
	}

}
