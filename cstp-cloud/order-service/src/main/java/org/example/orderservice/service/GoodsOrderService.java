package org.example.orderservice.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.example.orderservice.dto.GoodsOrderStatusDto;
import org.example.orderservice.dto.SubmitOrderDto;
import org.example.orderservice.vo.OrderListVo;

public interface GoodsOrderService {
    void createOrder(SubmitOrderDto submitOrderDto);
    IPage<OrderListVo> ordersList(int pageNum,String role);
    IPage<?> releaseList(int pageNum);

    void updateGoodsOrderStatus(GoodsOrderStatusDto goodsOrderStatusDto);
}
