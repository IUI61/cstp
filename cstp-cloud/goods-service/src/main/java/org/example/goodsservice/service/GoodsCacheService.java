package org.examole.goodsservice.service;

import org.example.goodsservice.vo.HomeGoodsVo;
import org.example.goodsservice.vo.PageResultVo;

public interface GoodsCacheService {
    long version();
    PageResultVo<HomeGoodsVo> getPage(long version, int pageNum);
    void putPage(long version, int pageNum, PageResultVo<HomeGoodsVo> pageResult);
    void invalidateList();
}
