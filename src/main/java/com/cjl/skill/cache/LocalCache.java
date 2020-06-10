package com.cjl.skill.cache;

import java.util.concurrent.ConcurrentHashMap;

/**
 * jvm本地缓存
 * @author cjl
 *
 */
public class LocalCache {
	//缓存商品售完标记
	public static final ConcurrentHashMap<String, Boolean> SOLD_OUT_FLAGS = new ConcurrentHashMap<>();
	
}
