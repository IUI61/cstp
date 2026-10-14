package org.example.orderservice.client;

import org.example.orderservice.dto.UserBrief;
import org.example.orderservice.entity.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/** 调 user-service 内部接口 */
@FeignClient(name = "user-service")
public interface UserClient {
    @PostMapping("/user/inner/batch")
    Result<List<UserBrief>> batch(@RequestBody List<Long> ids);
}
