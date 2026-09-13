package org.example.cstp.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ChatDto {
    @NotNull(message = "卖家ID不能为空")
    private Long sellerId;
    @NotNull(message = "商品ID不能为空")
    private Long goodsId;
}
