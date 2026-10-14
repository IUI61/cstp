package org.example.chatservice.client;

import org.example.chatservice.dto.UserBrief;
import org.example.chatservice.entity.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "user-service")
public interface UserClient {
    @GetMapping("/user/inner/{id}")
    Result<UserBrief> getById(@PathVariable("id") Long id);

    @PostMapping("/user/inner/batch")
    Result<List<UserBrief>> batch(@RequestBody List<Long> ids);
}
