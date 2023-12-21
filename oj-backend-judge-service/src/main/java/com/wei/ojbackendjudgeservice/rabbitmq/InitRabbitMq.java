package com.wei.ojbackendjudgeservice.rabbitmq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import lombok.extern.slf4j.Slf4j;

/**
 * @author wei
 * @description 用于创建测试程序用到的交换机和队列（只用在程序启动前执行一次）,可以重复运行，不会报错
 * @email 2529799312@qq.com
 * @date 2023-12-21 17:28
 */
@Slf4j
public class InitRabbitMq {
    public static void main(String[] args) {
        try {
            ConnectionFactory factory = new ConnectionFactory();

            // 设置连接参数，分别是：主机名、端口号、vhost、用户名、密码
            factory.setHost("localhost");

            Connection connection = factory.newConnection();
            //相当于客户端
            Channel channel = connection.createChannel();
            //创建交换机
            String EXCHANGE_NAME = "code_exchange";
            channel.exchangeDeclare(EXCHANGE_NAME, "direct");

            // 创建队列，随机分配一个队列名称
            String queueName = "code_queue";
            channel.queueDeclare(queueName, true, false, false, null);
            //交换机与对列绑定
            channel.queueBind(queueName, EXCHANGE_NAME, "my_routingKey");

            // 关闭通道和连接
            channel.close();
            connection.close();

            log.info("创建成功");
        } catch (Exception e) {
            log.error("创建失败");
        }

    }
}
