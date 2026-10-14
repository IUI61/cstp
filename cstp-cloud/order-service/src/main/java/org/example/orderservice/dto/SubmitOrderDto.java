package org.example.orderservice.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class   SubmitOrderDto {
    @NotNull(message = "商品ID不能为空")
    private Long goodsId;
    @NotNull(message = "买家姓名不能为空")
    @Size(max = 20, message = "买家姓名长度在20个字符以内`")
    private String buyerName;
    @NotNull(message = "买家电话不能为空")
    @Size(max = 11, message = "买家电话长度在11个字符以内`")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "买家电话格式不正确")
    private String buyerPhone;
    @NotNull(message = "买家地址不能为空")
    @Size(max = 200, message = "买家地址长度在200个字符以内`")
    private String buyerAddress;
}
