package com.cjl.skill.service;

import java.util.List;
import com.cjl.skill.pojo.Product;

public interface ProductService {
	Product getById(int id);
	
	int decreaseStock(int id);

	List<Product> getAll();
}
