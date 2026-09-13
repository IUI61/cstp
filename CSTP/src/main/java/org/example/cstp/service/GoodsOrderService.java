package org.example.cstp.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import jakarta.validation.Valid;
import org.example.cstp.dto.GoodsOrderStatusDto;
import org.example.cstp.dto.SubmitOrderDto;
import org.example.cstp.vo.HomeGoodsVo;
import org.example.cstp.vo.OrderListVo;

public interface GoodsOrderService {
    void createOrder(SubmitOrderDto submitOrderDto);
    IPage<OrderListVo> ordersList(int pageNum,String role);
    IPage<HomeGoodsVo> releaseList(int pageNum);

    void updateGoodsOrderStatus(GoodsOrderStatusDto goodsOrderStatusDto);
}
