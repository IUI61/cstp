package org.example.orderservice.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.math.BigDecimal;

/** 商品信息（goods-service 内部接口返回，消费方自定义结构） */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class GoodsInfo {
    private Long id;
    private Long userId;      // 卖家
    private String name;      // 商品名（Goods 实体的 name 字段）
    private BigDecimal price;
    private String image;
    private Integer status;   // 1在售 2锁定 0下架
}
