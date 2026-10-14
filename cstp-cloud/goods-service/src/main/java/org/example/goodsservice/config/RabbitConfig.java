package org.example.goodsservice.config;

import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {
    public static final String TOPIC_EXCHANGE = "cstp.order.topic";
    public static final String GOODS_STATUS_QUEUE = "goods.status.queue";
    public static final String GOODS_STATUS_KEY ="goods.status.change";
    @Bean
    public TopicExchange topicExchange() {
        return new TopicExchange(TOPIC_EXCHANGE);
    }
    @Bean
    public Queue goodsStatusQueue() {
        return new Queue(GOODS_STATUS_QUEUE);
    }
    @Bean
    public Binding goodsStatusBinding(){
        return BindingBuilder.bind(goodsStatusQueue()).to(topicExchange()).with(GOODS_STATUS_KEY);
    }
    /** 消息体用 JSON 序列化（跨服务通信必须，两边类路径不同） */
    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
