package com.cjl.skill.interceptor;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import com.cjl.skill.util.DomainUtil;

//@Component
public class CookieInterceptor implements HandlerInterceptor {
	Logger logger = LoggerFactory.getLogger(CookieInterceptor.class);

	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
			ModelAndView modelAndView) throws Exception {
		logger.info("post Handle...................");
		Cookie[] cookies = request.getCookies();
		if (cookies!=null) {
			for (Cookie cookie : cookies) {
				if ("jsessionid".equals(cookie.getName())) {
					String domain = cookie.getDomain();
					logger.info(domain);
					// 修改成二级域名，截取后2位. 例如：www.go8.com
					String domain2 = DomainUtil.getDomain(domain, 2);
					if (!StringUtils.isEmpty(domain2)) {
						logger.info(domain);
						cookie.setDomain(domain2);
					}
					break;
				}
			} 
		}
	}

	
}
