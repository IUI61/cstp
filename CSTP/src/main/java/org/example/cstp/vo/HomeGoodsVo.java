package org.example.cstp.vo;

import com.baomidou.mybatisplus.annotation.TableName;
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
