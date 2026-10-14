package org.example.orderservice.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class GoodsOrderStatusDto {
    @NotNull(message = "orderId不能为空")
    private Long orderId;
    @NotNull(message = "status不能为空")
    private Integer status;
    @NotNull(message = "role不能为空")
    private String role;
}
