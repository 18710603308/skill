package com.cjl.skill.util;

import java.text.SimpleDateFormat;
import org.springframework.util.StringUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.annotation.JsonInclude;

public class JsonUtil {
	private static ObjectMapper objectMapper = new ObjectMapper();
	// 日起格式化
	private static final String STANDARD_FORMAT = "yyyy-MM-dd HH:mm:ss";
	static {
		// 对象的所有字段全部列入
		objectMapper.setSerializationInclusion(JsonInclude.Include.ALWAYS);
		// 取消默认转换timestamps形式
		objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
		// 忽略空Bean转json的错误
		objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
		// 所有的日期格式都统一为以下的样式，即yyyy-MM-dd HH:mm:ss
		objectMapper.setDateFormat(new SimpleDateFormat(STANDARD_FORMAT));
		// 忽略 在json字符串中存在，但是在java对象中不存在对应属性的情况。防止错误
		objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
	}

	
	/**
     * 对象转Json格式字符串
     * @param obj 对象
     * @return Json格式字符串
     */
	public static <T> String obj2String(T obj) {
		if (obj == null) {
			return null;
		}
		try {
			return obj instanceof String ? (String) obj : objectMapper.writeValueAsString(obj);
		} catch (Exception e) {
			throw new RuntimeException("Parse Object to String error : " + e.getMessage());
		}
	}
	
	/**
     * 对象转Json格式字符串(格式化的Json字符串)
     * @param obj 对象
     * @return 美化的Json格式字符串
     */
    public static <T> String obj2StringPretty(T obj) {
        if (obj == null) {
            return null;
        }
        try {
            return obj instanceof String ? (String) obj : objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
        } catch (Exception e) {
			throw new RuntimeException("Parse Object to String error : " + e.getMessage());
		}
    }

    /**
     * 字符串转换为自定义对象
     * @param str 要转换的字符串
     * @param clazz 自定义对象的class对象
     * @return 自定义对象
     */
    @SuppressWarnings("unchecked")
	public static <T> T string2Obj(String str, Class<T> clazz){
        if(StringUtils.isEmpty(str) || clazz == null){
            return null;
        }
        try {
            return clazz.equals(String.class) ? (T) str : objectMapper.readValue(str, clazz);
        } catch (Exception e) {
        	throw new RuntimeException("Parse String to Object error : "+e.getMessage());
        }
    }
    
    /**
     * 字符串转集合
     * @param <T>
     * @param str
     * @param typeReference
     * @return
     */
    @SuppressWarnings("unchecked")
	public static <T> T string2Collection(String str, TypeReference<T> typeReference) {
        if (StringUtils.isEmpty(str) || typeReference == null) {
            return null;
        }
        try {
            return (T) (typeReference.getType().equals(String.class) ? str : objectMapper.readValue(str, typeReference));
        } catch (Exception e) {
        	throw new RuntimeException("Parse String to Collection error : "+e.getMessage());
        }
    }

    /**
     * 字符串转集合
     * @param <T>
     * @param str
     * @param collectionClazz
     * @param elementClazzes
     * @return
     */
    public static <T> T string2Collection(String str, Class<?> collectionClazz, Class<?>... elementClazzes) {
        JavaType javaType = objectMapper.getTypeFactory().constructParametricType(collectionClazz, elementClazzes);
        try {
            return objectMapper.readValue(str, javaType);
        } catch (Exception e) {
        	throw new RuntimeException("Parse String to Collection error : " + e.getMessage());
        }
    }
}
