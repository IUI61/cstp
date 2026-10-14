package org.example.orderservice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.example.orderservice.entity.GoodsOrder;
import org.example.orderservice.vo.OrderListVo;

public interface GoodsOrderMapper extends BaseMapper<GoodsOrder> {
    IPage<OrderListVo> ordersList(Page<OrderListVo> page, Long userId, String role);
}
