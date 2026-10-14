package org.example.orderservice.controller;

import jakarta.validation.Valid;
import org.example.orderservice.dto.GoodsOrderStatusDto;
import org.example.orderservice.dto.SubmitOrderDto;
import org.example.orderservice.entity.Result;
import org.example.orderservice.service.GoodsOrderService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/orders")
public class OrderController {
    private final GoodsOrderService goodsOrderService;
    public OrderController(GoodsOrderService goodsOrderService) {
        this.goodsOrderService = goodsOrderService;
    }
    @PostMapping("/create")
    public Result createOrder(@Valid @RequestBody SubmitOrderDto submitOrderDto) {
        goodsOrderService.createOrder(submitOrderDto);
        return Result.success();
    }
    @GetMapping("/goodsOrdersList")
    public Result goodsOrdersList(@RequestParam(value = "pageNum",defaultValue = "1") int pageNum, @RequestParam String role) {
        if(role.equals("release")){
            return Result.success(goodsOrderService.releaseList(pageNum));
        }
        return Result.success(goodsOrderService.ordersList(pageNum, role));
    }
    @PutMapping("/goodsOrderStatus")
    public Result updateGoodsOrderStatus(@Valid @RequestBody GoodsOrderStatusDto goodsOrderStatusDto) {
        goodsOrderService.updateGoodsOrderStatus(goodsOrderStatusDto);
        return Result.success();
    }
}
