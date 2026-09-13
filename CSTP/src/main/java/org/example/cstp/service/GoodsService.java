package org.example.cstp.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.example.cstp.entity.Goods;
import org.example.cstp.vo.GoodsDetailVo;
import org.example.cstp.vo.HomeGoodsVo;
import org.springframework.web.multipart.MultipartFile;

public interface GoodsService {
    IPage<HomeGoodsVo> handleGoodsList(int pageNum);
    void goodsIssuance(MultipartFile[] file, Goods goods);

    GoodsDetailVo handleGoodsDetail(Long goodsId);
}
