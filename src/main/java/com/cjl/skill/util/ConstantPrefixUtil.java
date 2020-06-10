package com.cjl.skill.util;

public class ConstantPrefixUtil {
	public static final String SKILL_PRODUCT_PREFIX = "SKILL_PRODUCT:";
	
	public static final String ZK_SOLD_OUT_PRODUCT_ROOT_PATH = "/SOLD_OUT";
	
	public static final String REDIS_ORDER_SUCCESS_FLAG_PREFIX = "REDIS_ORDER:";
	
	//订单排队标记
	public static final String REDIS_ORDER_QUEUE_FLAG_PREFIX = "REDIS_ORDER_QUEUE:";
	
	public static final String REDIS_VCODE_FLAG_PREFIX = "Vcode:";
	
	public static final String REDIS_SKILL_TOKEN_FLAG_PREFIX = "Skill_Token:";
	
	//基于url访问的限流标记
	public static final String REDIS_SKILL_LIMIT_URL_FLAG_PREFIX = "Skill_Limit:Url:";
}
