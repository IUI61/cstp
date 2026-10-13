package org.examole.goodsservice.service;

import org.example.goodsservice.dto.GoodsDto;
import org.example.goodsservice.vo.GoodsDetailVo;
import org.example.goodsservice.vo.HomeGoodsVo;
import org.example.goodsservice.vo.PageResultVo;
import org.springframework.web.multipart.MultipartFile;

public interface GoodsService {
    PageResultVo<HomeGoodsVo> handleGoodsList(int pageNum);
    void goodsIssuance(MultipartFile[] file, GoodsDto goodsDto);

    GoodsDetailVo handleGoodsDetail(Long goodsId);
}
