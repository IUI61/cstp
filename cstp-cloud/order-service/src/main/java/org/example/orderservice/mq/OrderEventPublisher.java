package org.example.orderservice.mq;

import org.example.orderservice.config.RabbitConfig;
import org.example.orderservice.dto.GoodsStatusMessage;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/** 订单事件发布：只往交换机发，与 goods-service 解耦 */
@Component
public class OrderEventPublisher {
    private final RabbitTemplate rabbitTemplate;

    public OrderEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    /** 发布"商品状态变更"（下单锁定 2 / 上下架 0/1） */
    public void publishGoodsStatus(Long goodsId, Integer status) {
        GoodsStatusMessage message = new GoodsStatusMessage();
        message.setGoodsId(goodsId);
        message.setStatus(status);
        rabbitTemplate.convertAndSend(RabbitConfig.TOPIC_EXCHANGE, RabbitConfig.GOODS_STATUS_KEY, message);
    }
}
