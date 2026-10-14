package org.example.orderservice.dto;

import lombok.Data;

/** 商品状态变更消息（与 goods-service 同名类字段一致，JSON 通信） */
@Data
public class GoodsStatusMessage {
    private Long goodsId;
    private Integer status;
}
