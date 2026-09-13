package org.example.cstp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.example.cstp.entity.Goods;
import org.example.cstp.vo.GoodsDetailVo;
import org.example.cstp.vo.HomeGoodsVo;

public interface GoodsMapper extends BaseMapper<Goods> {
    IPage<HomeGoodsVo> handleGoodsList(Page<HomeGoodsVo> page, Long userId);
    GoodsDetailVo handleGoodsDetail(Long goodsId);
}
