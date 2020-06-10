package com.cjl.skill.listener;

import org.springframework.context.ApplicationListener;
//import org.springframework.session.Session;
//import org.springframework.session.events.SessionDeletedEvent;
import org.springframework.stereotype.Component;
import com.cjl.skill.pojo.User;

//监听session失效删除
/*
 * @Component public class SessionEventListener implements
 * ApplicationListener<SessionDeletedEvent> {
 * 
 * @Override public void onApplicationEvent(SessionDeletedEvent event) { Session
 * session = event.getSession(); User user = (User)
 * session.getAttribute("user"); System.out.println("invalid session's user:" +
 * user.toString()); } }
 */