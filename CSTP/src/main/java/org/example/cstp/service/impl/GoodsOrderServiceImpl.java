package org.example.cstp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.example.cstp.dto.GoodsOrderStatusDto;
import org.example.cstp.dto.SubmitOrderDto;
import org.example.cstp.entity.Goods;
import org.example.cstp.entity.GoodsOrder;
import org.example.cstp.exception.BusinessException;
import org.example.cstp.mapper.GoodsMapper;
import org.example.cstp.mapper.GoodsOrderMapper;
import org.example.cstp.service.GoodsCacheService;
import org.example.cstp.service.GoodsOrderService;
import org.example.cstp.util.UserContext;
import org.example.cstp.vo.HomeGoodsVo;
import org.example.cstp.vo.OrderListVo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class GoodsOrderServiceImpl implements GoodsOrderService {
    private final GoodsMapper goodsMapper;
    private final GoodsOrderMapper goodsOrderMapper;
    private final GoodsCacheService goodsCacheService;
    public GoodsOrderServiceImpl(GoodsMapper goodsMapper,
                                 GoodsOrderMapper goodsOrderMapper,
                                 GoodsCacheService goodsCacheService) {
        this.goodsMapper = goodsMapper;
        this.goodsOrderMapper = goodsOrderMapper;
        this.goodsCacheService = goodsCacheService;
    }
    @Override
    @Transactional
    public void createOrder(SubmitOrderDto submitOrderDto) {
        Long buyerId = currentUserId();
        //检查商品是否在售
        Goods goods = goodsMapper.selectById(submitOrderDto.getGoodsId());
        if (goods == null || goods.getStatus() != 1) {
            throw new BusinessException(400, "商品不存在或已下架");
        }
        //检查商品买家和卖家是否是同一个人
        if (goods.getUserId().equals(buyerId)) {
            throw new BusinessException(400, "不能购买自己的商品");
        }
        //更新商品状态，防止多人同时下单
        LambdaUpdateWrapper<Goods> wrapper = new LambdaUpdateWrapper<Goods>()
                .eq(Goods::getId, submitOrderDto.getGoodsId())
                .eq(Goods::getStatus, 1)
                .set(Goods::getStatus, 2);
        int row = goodsMapper.update(null, wrapper);
        if (row == 0) {
            throw new BusinessException(400, "商品被抢购");
        }
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
        IPage<OrderListVo> goodsOrderPage = goodsOrderMapper.ordersList(page, currentUserId(), role);
        return goodsOrderPage;
    }
    @Override
    public IPage<HomeGoodsVo> releaseList(int pageNum) {
        Page<HomeGoodsVo> page = new Page<>(pageNum, 10);
        IPage<HomeGoodsVo> goodsPage = goodsMapper.handleGoodsList(page, currentUserId());
        return goodsPage;
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
        if (role.equals("release")){
            Goods goods = new Goods();
            goods.setId(orderId);
            if(status.equals(0)){
                goods.setStatus(1);
            } else if(status.equals(1)){
                goods.setStatus(0);
            }
            goodsMapper.updateById(goods);
            // 上架/下架会改变首页在售列表（所有页都会错位），必须失效
            goodsCacheService.invalidateList();
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
        Long userId = UserContext.getId();
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
