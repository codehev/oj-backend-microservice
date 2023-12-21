package com.wei.ojbackendjudgeservice.rabbitmq;

import com.rabbitmq.client.Channel;
import com.wei.ojbackendjudgeservice.judge.JudgeService;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 消费者
 * 接收消息
 *
 * @author wei
 * @description TODO
 * @email 2529799312@qq.com
 * @date 2023-12-21 17:08
 */
@Component
@Slf4j
public class MessageConsumer {
    @Resource
    private JudgeService judgeService;

    /**
     * 指定程序监听的消息队列和确认机制
     *
     * @param message
     * @param channel
     * @param deliveryTag
     */
    @SneakyThrows
    @RabbitListener(queues = {"code_queue"}, ackMode = "MANUAL")
    public void receiveMessage(String message, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        log.info("receiveMessage message = {}", message);
        if (message == null || "".equals(message)) {
            return;
        }
        try {
            long questionSubmitId = Long.parseLong(message);
            judgeService.doJudge(questionSubmitId);
            /**执行成功
             *用于确认接收到的 RabbitMQ 消息。该方法接受两个参数：
             * - deliveryTag：表示接收到的消息的唯一标识符。
             * - false：表示不需要应答消费者确认收到的消息。
             */
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            /**执行失败
             * 拒绝接收到的 RabbitMQ 消息。(防止未执行成功，还把队列中的消息消耗了)该方法接受三个参数：
             * - deliveryTag：表示接收到的消息的唯一标识符。
             * - false：表示不需要应答消费者拒绝的消息。
             * - true：表示如果 RabbitMQ 重试消息 deliveryTag 对应的消息，那么仅重试一次。
             */
            channel.basicNack(deliveryTag, false, true);
        }


    }
}
