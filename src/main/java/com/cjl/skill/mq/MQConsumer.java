package com.cjl.skill.mq;

import java.util.List;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.common.message.MessageExt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import com.cjl.skill.pojo.Order;
import com.cjl.skill.service.OrderService;
import com.cjl.skill.util.JsonUtil;

@Component
public class MQConsumer implements CommandLineRunner {
	@Autowired
	private OrderService orderService;

	@Override
	public void run(String... args) throws Exception {
		/**
		 * 启动消费者，然后消费消息，添加订单到数据库
		 */
		try {
			// Instantiate with specified consumer group name."order_group", "order_topic",
			// "order_tag",
			DefaultMQPushConsumer consumer = new DefaultMQPushConsumer("order_group");

			// Specify name server addresses.
			consumer.setNamesrvAddr("localhost:9876");

			// Subscribe one more more topics to consume.
			consumer.subscribe("order_topic", "order_tag");

			// Register callback to execute on arrival of messages fetched from brokers.
			consumer.registerMessageListener(new MessageListenerConcurrently() {
				@Override
				public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs,
						ConsumeConcurrentlyContext context) {

					for (MessageExt mes : msgs) {
						Order order = JsonUtil.string2Obj(new String(mes.getBody()), Order.class);
						orderService.createSkillOrder(order);
						System.out.printf("%s Receive New Order Messages: %s %n", Thread.currentThread().getName(),
								msgs);
					}
					return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
				}
			});

			// Launch the consumer instance.
			consumer.start();

			System.out.printf("Consumer Started.%n");
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("consumer started failed.");
		}

	}

}
