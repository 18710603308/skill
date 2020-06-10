package com.cjl.skill.mapper;

import java.util.List;

import com.cjl.skill.pojo.Product;

public interface ProductMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(Product record);

    int insertSelective(Product record);

    Product selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Product record);

    int updateByPrimaryKey(Product record);
    
    //custom
	int decreaseStock(Integer productId);

	int selectStockById(int productId);

	List<Product> selectAll();
	
	//恢复库存为测试初始值10
	int renewStock();
	
	//查询所有参加秒杀活动的商品
	List<Product> selectProductIdAndStock();
}