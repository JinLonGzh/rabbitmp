package com.zjl.rabbitmq.eight;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.zjl.rabbitmq.utils.RabbitMqUtils;

import java.nio.charset.StandardCharsets;

public class Producer {
    private static final String NORMAL_EXCHANGE = "normal_exchange";

    public static void main(String[] args) throws Exception {
        Channel channel = RabbitMqUtils.getChannel();

//        //死信消息 设置TTL时间
//        AMQP.BasicProperties basicProperties =
//                new AMQP.BasicProperties()
//                        .builder().expiration("5000").build();
        for (int i = 0; i < 10; i++) {
            String message = "info" + i;
            channel.basicPublish(NORMAL_EXCHANGE, "zhangsan", null, message.getBytes(StandardCharsets.UTF_8));
        }
    }
}
