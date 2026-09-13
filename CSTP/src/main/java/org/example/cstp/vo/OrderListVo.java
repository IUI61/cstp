package org.example.cstp.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class OrderListVo {
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    private String orderNo;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long goodsId;
    private String goodsName;
    private String goodsImage;
    private BigDecimal price;
    private Integer status;
    private String receiverName;
    private String receiverPhone;
    private String receiverAddress;
    private String shipCompany;
    private String shipNo;
    private LocalDateTime shipTime;
    private LocalDateTime finishTime;
    private LocalDateTime cancelTime;
    private LocalDateTime createTime;
    private String name;
    private String avatar;
}
