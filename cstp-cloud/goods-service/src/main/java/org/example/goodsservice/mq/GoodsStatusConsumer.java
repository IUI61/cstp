package org.example.goodsservice.mq;

import org.example.goodsservice.config.RabbitConfig;
import org.example.goodsservice.dto.GoodsStatusMessage;
import org.example.goodsservice.entity.Goods;
import org.example.goodsservice.mapper.GoodsMapper;
import org.example.goodsservice.service.GoodsCacheService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class GoodsStatusConsumer {
    private final GoodsMapper goodsMapper;
    private final GoodsCacheService goodsCacheService;

    public GoodsStatusConsumer(GoodsMapper goodsMapper, GoodsCacheService goodsCacheService) {
        this.goodsMapper = goodsMapper;
        this.goodsCacheService = goodsCacheService;
    }
    @RabbitListener(queues = RabbitConfig.GOODS_STATUS_QUEUE)
    public void handleStatusChange(GoodsStatusMessage message) {
        // 改商品状态（下单锁定 1→2 / 上下架 0↔1）
        Goods goods = new Goods();
        goods.setId(message.getGoodsId());
        goods.setStatus(message.getStatus());
        goodsMapper.updateById(goods);
        // 状态变了 → 首页在售列表缓存失效
        goodsCacheService.invalidateList();
    }
}
