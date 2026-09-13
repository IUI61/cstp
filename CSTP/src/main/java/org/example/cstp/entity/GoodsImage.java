package org.example.cstp.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("goods_image")
public class GoodsImage {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long goodsId;
    private String url;
    private Integer sort;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer isDelete;
}
