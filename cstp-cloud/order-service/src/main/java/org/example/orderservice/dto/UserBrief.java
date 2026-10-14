package org.example.orderservice.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

/** 用户简讯（user-service 内部接口返回） */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserBrief {
    private Long id;
    private String name;
    private String avatar;
}
