package org.example.orderservice.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.example.orderservice.client.GoodsClient;
import org.example.orderservice.client.UserClient;
import org.example.orderservice.dto.GoodsInfo;
import org.example.orderservice.dto.GoodsOrderStatusDto;
import org.example.orderservice.dto.SubmitOrderDto;
import org.example.orderservice.dto.UserBrief;
import org.example.orderservice.entity.GoodsOrder;
import org.example.orderservice.exception.BusinessException;
import org.example.orderservice.mapper.GoodsOrderMapper;
import org.example.orderservice.mq.OrderEventPublisher;
import org.example.orderservice.service.GoodsOrderService;
import org.example.orderservice.util.UserContext;
import org.example.orderservice.vo.OrderListVo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Service
public class GoodsOrderServiceImpl implements GoodsOrderService {
    private final GoodsClient goodsClient;
    private final GoodsOrderMapper goodsOrderMapper;
    private final OrderEventPublisher eventPublisher;
    private final UserClient userClient;
    public GoodsOrderServiceImpl(GoodsClient goodsClient,
                                 GoodsOrderMapper goodsOrderMapper,
                                 UserClient userClient,
                                 OrderEventPublisher eventPublisher) {
        this.goodsClient = goodsClient;
        this.goodsOrderMapper = goodsOrderMapper;
        this.eventPublisher = eventPublisher;
        this.userClient = userClient;
    }
    @Override
    @Transactional
    public void createOrder(SubmitOrderDto submitOrderDto) {
        Long buyerId = currentUserId();
        //检查商品是否在售
        GoodsInfo goods =  goodsClient.getById(submitOrderDto.getGoodsId()).getData();
        if (goods == null || goods.getStatus() != 1) {
            throw new BusinessException(400, "商品不存在或已下架");
        }
        //检查商品买家和卖家是否是同一个人
        if (goods.getUserId().equals(buyerId)) {
            throw new BusinessException(400, "不能购买自己的商品");
        }
        //更新商品状态，防止多人同时下单
        eventPublisher.publishGoodsStatus(submitOrderDto.getGoodsId(), 2);
        //检查是否重复下单
        Long count = goodsOrderMapper.selectCount(new LambdaQueryWrapper<GoodsOrder>()
                .eq(GoodsOrder::getBuyerId, buyerId)
                .eq(GoodsOrder::getGoodsId, submitOrderDto.getGoodsId())
                .eq(GoodsOrder::getStatus, 1));
        if (count > 0) {
            throw new BusinessException(400, "用户已下过该商品订单");
        }
        //通过商品ID获取GoodsOrder对象剩余属性
        GoodsOrder goodsOrder = new GoodsOrder();
        goodsOrder.setOrderNo(generateOrderNo());
        goodsOrder.setGoodsId(submitOrderDto.getGoodsId());
        goodsOrder.setGoodsName(goods.getName());
        goodsOrder.setGoodsImage(goods.getImage());
        goodsOrder.setPrice(goods.getPrice());
        goodsOrder.setBuyerId(buyerId);
        goodsOrder.setSellerId(goods.getUserId());
        goodsOrder.setReceiverName(submitOrderDto.getBuyerName());
        goodsOrder.setReceiverPhone(submitOrderDto.getBuyerPhone());
        goodsOrder.setReceiverAddress(submitOrderDto.getBuyerAddress());
        goodsOrder.setStatus(1);
        goodsOrderMapper.insert(goodsOrder);
    }
    @Override
    public IPage<OrderListVo> ordersList(int pageNum, String role) {
        Page<OrderListVo> page = new Page<>(pageNum, 10);
        IPage<OrderListVo> result = goodsOrderMapper.ordersList(page, currentUserId(), role);
// 收集"对方" id：buy 看卖家 sellerId，sell 看买家 buyerId
        List<Long> userIds = result.getRecords().stream()
                .map(vo -> "buy".equals(role) ? vo.getSellerId() : vo.getBuyerId())
                .distinct().collect(Collectors.toList());
        if (!userIds.isEmpty()) {
            Map<Long, UserBrief> briefMap = userClient.batch(userIds).getData().stream()
                    .collect(Collectors.toMap(UserBrief::getId, b -> b));
            result.getRecords().forEach(vo -> {
                UserBrief b = briefMap.get("buy".equals(role) ? vo.getSellerId() : vo.getBuyerId());
                if (b != null) {
                    vo.setName(b.getName());
                    vo.setAvatar(b.getAvatar());
                }
            });
        }
        return result;

    }
    @Override
    public IPage<?> releaseList(int pageNum) {
        // 遗留：'我发布的'列表需要 goods-service 提供分页内部接口，后续阶段补
        return new Page<>(pageNum, 10);
    }
    @Override
    public void updateGoodsOrderStatus(GoodsOrderStatusDto goodsOrderStatusDto) {
        //校验当前登录人是不是这笔订单的买家/卖家
        Long userId = currentUserId();
        Long orderId = goodsOrderStatusDto.getOrderId();
        GoodsOrder goodsOrderAuthority = goodsOrderMapper.selectById(orderId);
        if (!goodsOrderAuthority.getBuyerId().equals(userId) && !goodsOrderAuthority.getSellerId().equals(userId)) {
            throw new BusinessException(403, "无权限修改这笔订单");
        }
        Integer status = goodsOrderStatusDto.getStatus();
        String role = goodsOrderStatusDto.getRole();
        Long goodsId = goodsOrderAuthority.getGoodsId();
        if (role.equals("release")) {
            Integer s = status.equals(0) ? 1 : 0;   // 上下架互切（原逻辑）
            eventPublisher.publishGoodsStatus(goodsId, s);  // goods 消费端改状态 + 缓存失效
            return;
        }
        GoodsOrder goodsOrder = new GoodsOrder();
        goodsOrder.setId(orderId);
        if(status.equals(1)){
            if(role.equals("buy")) goodsOrder.setStatus(4);// 买家取消订单
            else if(role.equals("sell")) goodsOrder.setStatus(2);// 卖家发货
        } else if(status.equals(2) && role.equals("buy")){// 买家确认收货
            goodsOrder.setStatus(3);
        }
        goodsOrderMapper.updateById(goodsOrder);
    }
    public Long currentUserId() {
        Long userId = UserContext.getUserId();
        if(userId == null){
            throw new BusinessException(401, "请先登入");
        }
        return userId;
    }
    // 业务订单号: 时间戳+随机数, 唯一索引兜底
    private String generateOrderNo() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"))
                + String.format("%03d", ThreadLocalRandom.current().nextInt(1000));
    }
}
