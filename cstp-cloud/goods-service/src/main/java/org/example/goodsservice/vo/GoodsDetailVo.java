package org.examole.goodsservice.vo;

import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class GoodsDetailVo {
    @JsonSerialize(using = ToStringSerializer.class)
    private Long sellerId;
    private String SellerName;
    private String avatar;
    @TableField(exist = false)
    private List<String> imageList;
    private String name;
    private BigDecimal price;
    private String type;
    private String description;
}
