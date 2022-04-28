package com.zjl.rabbitmq.five;

import com.rabbitmq.client.Channel;
import com.zjl.rabbitmq.utils.RabbitMqUtils;

import java.util.Scanner;

public class EmitLog {
    private static final String EXCHANGE_NAME = "direct";

    public static void main(String[] argv) throws Exception {
        Channel channel = RabbitMqUtils.getChannel();

            Scanner sc = new Scanner(System.in);
            System.out.println("请输入信息");
            while (sc.hasNext()) {
                String message = sc.next();
                channel.basicPublish(EXCHANGE_NAME, "123", null, message.getBytes("UTF-8"));
                System.out.println("生产者发出消息" + message);
            }
        }
    }
