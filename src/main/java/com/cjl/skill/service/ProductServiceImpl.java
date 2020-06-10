package com.cjl.skill.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.cjl.skill.mapper.ProductMapper;
import com.cjl.skill.pojo.Product;

@Service
public class ProductServiceImpl implements ProductService {
	@Autowired
	private ProductMapper productMapper;
	
	@Override
	public int decreaseStock(int id) {
		return productMapper.decreaseStock(id);
	}

	@Override
	public Product getById(int id) {
		return productMapper.selectByPrimaryKey(id);
	}

	@Override
	public List<Product> getAll() {
		return productMapper.selectAll();
	}

}
