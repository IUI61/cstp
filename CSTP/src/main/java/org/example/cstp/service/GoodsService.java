package org.example.cstp.service;

import org.example.cstp.dto.GoodsDto;
import org.example.cstp.entity.Goods;
import org.example.cstp.vo.GoodsDetailVo;
import org.example.cstp.vo.HomeGoodsVo;
import org.example.cstp.vo.PageResultVo;
import org.springframework.web.multipart.MultipartFile;

public interface GoodsService {
    PageResultVo<HomeGoodsVo> handleGoodsList(int pageNum);
    void goodsIssuance(MultipartFile[] file, GoodsDto goodsDto);

    GoodsDetailVo handleGoodsDetail(Long goodsId);
}
