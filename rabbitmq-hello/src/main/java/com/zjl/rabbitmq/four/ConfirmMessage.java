package com.zjl.rabbitmq.four;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConfirmCallback;
import com.zjl.rabbitmq.utils.RabbitMqUtils;

import java.util.UUID;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;

public class ConfirmMessage {

    private static final int MESSAGE_COUNT = 1000;

    public static void main(String[] args) throws Exception {

//        ConfirmMessage.publishMessageIndividually();//耗时592ms
//        ConfirmMessage.publishMessageBatch();//耗时90ms
        ConfirmMessage.publishMessageAsync();//耗时50ms
    }

    public static void publishMessageIndividually() throws Exception {
        Channel channel = RabbitMqUtils.getChannel();
        String queueName = UUID.randomUUID().toString();

        channel.queueDeclare(queueName, false, false, false, null);

        //开启发布确认
        channel.confirmSelect();
        long begin = System.currentTimeMillis();

        for (int i = 0; i < MESSAGE_COUNT; i++) {
            String message = i + "";
            channel.basicPublish("", queueName, null, message.getBytes());
            //服务端返回 false 或超时时间内未返回，生产者可以消息重发
            boolean flag = channel.waitForConfirms();
            if (flag) {
                System.out.println("消息发送成功");
            }
        }

        long end = System.currentTimeMillis();
        System.out.println("发布" + MESSAGE_COUNT + "个单独确认消息,耗时" + (end - begin) + "ms");
    }

    //批量
    public static void publishMessageBatch() throws Exception {
        Channel channel = RabbitMqUtils.getChannel();
        String queueName = UUID.randomUUID().toString();

        channel.queueDeclare(queueName, false, false, false, null);

        //开启发布确认
        channel.confirmSelect();
        long begin = System.currentTimeMillis();

        for (int i = 0; i < MESSAGE_COUNT; i++) {
            String message = i + "";
            channel.basicPublish("", queueName, null, message.getBytes());

            if (i % 100 == 0) {
                boolean flag = channel.waitForConfirms();
            }
        }
        long end = System.currentTimeMillis();
        System.out.println("发布" + MESSAGE_COUNT + "个单独确认消息,耗时" + (end - begin) + "ms");
    }

    //异步
    public static void publishMessageAsync() throws Exception {
        Channel channel = RabbitMqUtils.getChannel();
        String queueName = UUID.randomUUID().toString();
        channel.queueDeclare(queueName, false, false, false, null);
        //开启发布确认
        channel.confirmSelect();

        /**
         * 线程安全有序的一个哈希表，使用于高并发的情况下
         * 1.轻松的将序号与消息相连
         * 2.轻松的批量删除条目，只要给到序号
         * 3.支持高并发（多线程）
         */
        ConcurrentSkipListMap<Long, String> outstandingConfirms =
                new ConcurrentSkipListMap<>();


        /**
         * 添加一个异步确认的监听器
         * 1.确认收到消息的回调
         * 2.未收到消息的回调
         */
        ConfirmCallback ackCallback = (deliveryTag, multiple) -> {
            if (multiple) {
                //删除已经确认的消息，剩下的就是未确认的消息
                ConcurrentNavigableMap<Long, String> confirmed =
                        outstandingConfirms.headMap(deliveryTag);
                confirmed.clear();
            } else {
                outstandingConfirms.remove(deliveryTag);
            }
            System.out.println("确认的消息" + deliveryTag);
        };
        /**
         * 1.消息的标记
         * 2.是否为批量删除
         */
        ConfirmCallback nackCallback = (deliveryTag, multiple) -> {
            String message = outstandingConfirms.get(deliveryTag);
            System.out.println("未确认的消息是：" + message + ":::未确认的消息" + deliveryTag);
        };

        channel.addConfirmListener(ackCallback, nackCallback);

        long begin = System.currentTimeMillis();
        for (int i = 0; i < MESSAGE_COUNT; i++) {
            String message = "消息" + i;
            channel.basicPublish("", queueName, null, message.getBytes());
            //此处记录下所有要发送的消息  消息的总和
            outstandingConfirms.put(channel.getNextPublishSeqNo(), message);
        }


        long end = System.currentTimeMillis();
        System.out.println("异步发布" + MESSAGE_COUNT + "个单独确认消息,耗时" + (end - begin) + "ms");

    }


}
