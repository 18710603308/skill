package com.cjl.skill.mq;

import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.common.message.Message;
import org.springframework.stereotype.Component;

@Component
public class MQProducer {

	private DefaultMQProducer producer;

	public void sendMessage(String group, String topic, String tag, byte[] message) throws Exception {
		if(producer!=null) {
			Message msg = new Message(topic, tag, message);
			producer.send(msg);
		}else {
			producer = new DefaultMQProducer(group);
			// Specify name server addresses.
			producer.setNamesrvAddr("localhost:9876");
			//设置一些参数
			producer.setRetryTimesWhenSendAsyncFailed(0);
			// Launch the instance.
			producer.start();
			Message msg = new Message(topic, tag, message);
			producer.send(msg);
		}
		//producer.shutdown();
	}
}
