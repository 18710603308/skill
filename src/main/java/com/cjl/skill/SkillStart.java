package com.cjl.skill;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.session.web.http.CookieSerializer;
import org.springframework.session.web.http.DefaultCookieSerializer;

@SpringBootApplication
@MapperScan("com.cjl.skill.mapper")
//@ServletComponentScan("com.cjl.skill.filter")
public class SkillStart {
	public static void main(String[] args) {
		SpringApplication.run(SkillStart.class, args);
	}

	/**
	 * 实现跨域
	 * 
	 * @return
	 */
	@Bean
	public CookieSerializer defaultCookieSerializer() {
		DefaultCookieSerializer serializer = new DefaultCookieSerializer();
	    serializer.setCookieName("JSESSIONID");
	    serializer.setCookiePath("/");
	    serializer.setDomainNamePattern("^.+?\\.(\\w+\\.[a-z]+)$");
	    //serializer.setDomainName("go8.com");
	    //取消站点限制
	    serializer.setSameSite(null);
	    serializer.setUseSecureCookie(false);
	    serializer.setUseHttpOnlyCookie(true);
	    return serializer;
	}

	/**
	 * 自定义序列化方式为json格式 叫这个默认名字：springSessionDefaultRedisSerializer
	 * 
	 * @return
	 */
	@Bean
	public GenericJackson2JsonRedisSerializer springSessionDefaultRedisSerializer() {
		return new GenericJackson2JsonRedisSerializer();
	}

}
