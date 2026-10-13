package org.examole.goodsservice.controller;

import jakarta.validation.Valid;
import org.example.goodsservice.dto.GoodsDto;
import org.example.goodsservice.entity.Result;
import org.example.goodsservice.service.GoodsService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/goods")
public class GoodsController {
    private final GoodsService goodsService;
    public GoodsController(GoodsService goodsService) {
        this.goodsService = goodsService;
    }

    @GetMapping("/show")
    public Result handleGoodsList(@RequestParam(value = "pageNum",defaultValue = "1") int pageNum) {
        return Result.success(goodsService.handleGoodsList(pageNum));
    }
    @PostMapping("/issuance")
    public Result goodsIssuance(@RequestPart(value = "file",required = false) MultipartFile[] file,@Valid @ModelAttribute GoodsDto goodsDto) {
        goodsService.goodsIssuance(file,goodsDto);
        return Result.success();
    }
    @GetMapping("/detail")
    public Result handleGoodsDetail(@RequestParam("id") Long goodsId) {
        return Result.success(goodsService.handleGoodsDetail(goodsId));
    }
}
