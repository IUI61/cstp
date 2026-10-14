package org.example.goodsservice.controller;

import org.example.goodsservice.entity.Goods;
import org.example.goodsservice.entity.Result;
import org.example.goodsservice.service.GoodsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/goods/inner")
public class GoodsInnerController {
    private final GoodsService goodsService;

    public GoodsInnerController(GoodsService goodsService) {
        this.goodsService = goodsService;
    }

    /** 查商品详情（order-service 下单时校验/取信息用） */
    @GetMapping("/{id}")
    public Result getById(@PathVariable Long id) {
        Goods goods = goodsService.getById(id);
        return goods == null ? Result.error(404, "商品不存在") : Result.success(goods);
    }
}
