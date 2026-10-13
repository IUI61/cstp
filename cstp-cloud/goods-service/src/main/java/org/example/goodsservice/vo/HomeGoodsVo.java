package org.examole.goodsservice.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class HomeGoodsVo {
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    private String goodsName;
    private BigDecimal price;
    private String goodsImage;
    private Integer status;
}
