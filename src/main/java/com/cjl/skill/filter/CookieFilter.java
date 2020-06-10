package com.cjl.skill.filter;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//@WebFilter(urlPatterns = "/*")
public class CookieFilter implements Filter {
	private static Logger logger = LoggerFactory.getLogger(CookieFilter.class);
	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		logger.info("doFilter...................");
		Cookie[] cookies = ((HttpServletRequest) request).getCookies();
		if (cookies!=null) {
			for (Cookie cookie : cookies) {
				if ("JSESSIONID".equals(cookie.getName())) {
					String domain = cookie.getDomain();
					logger.info(domain);
					cookie.setDomain("go8.com");
					((HttpServletResponse)response).addCookie(cookie);
					break;
				}
			} 
		}
		chain.doFilter(request, response);
	}
}
