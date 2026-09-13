package org.example.cstp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.example.cstp.entity.GoodsOrder;
import org.example.cstp.vo.OrderListVo;

public interface GoodsOrderMapper extends BaseMapper<GoodsOrder> {
    IPage<OrderListVo> buyList(Page<OrderListVo> page, Long userId);
    IPage<OrderListVo> ordersList(Page<OrderListVo> page, Long userId, String role);
}
