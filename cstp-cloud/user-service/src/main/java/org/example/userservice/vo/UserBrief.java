package org.example.userservice.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

/** 用户简讯：给其他服务用（订单列表显示对方姓名/头像） */
@Data
public class UserBrief {
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    private String name;
    private String avatar;
}

