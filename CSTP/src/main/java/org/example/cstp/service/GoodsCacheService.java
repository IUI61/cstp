package org.example.cstp.service;

import org.example.cstp.vo.HomeGoodsVo;
import org.example.cstp.vo.PageResultVo;

public interface GoodsCacheService {
    long version();
    PageResultVo<HomeGoodsVo> getPage(long version, int pageNum);
    void putPage(long version, int pageNum, PageResultVo<HomeGoodsVo> pageResult);
    void invalidateList();
}
