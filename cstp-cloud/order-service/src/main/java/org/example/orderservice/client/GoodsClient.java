package org.example.orderservice.client;

import org.example.orderservice.dto.GoodsInfo;
import org.example.orderservice.entity.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/** 调 goods-service 内部接口 */
@FeignClient(name = "goods-service")
public interface GoodsClient {
    @GetMapping("/goods/inner/{id}")
    Result<GoodsInfo> getById(@PathVariable("id") Long id);
}
