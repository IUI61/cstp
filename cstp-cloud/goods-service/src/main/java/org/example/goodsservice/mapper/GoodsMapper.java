package org.example.goodsservice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.example.goodsservice.entity.Goods;
import org.example.goodsservice.vo.GoodsDetailVo;
import org.example.goodsservice.vo.HomeGoodsVo;

public interface GoodsMapper extends BaseMapper<Goods> {
    IPage<HomeGoodsVo> handleGoodsList(Page<HomeGoodsVo> page, Long userId);
    GoodsDetailVo handleGoodsDetail(Long goodsId);
}
