package org.example.userservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.example.userservice.dto.LoginDto;
import org.example.userservice.dto.UserDto;
import org.example.userservice.entity.Result;
import org.example.userservice.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Tag(name = "用户接口")
@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {
    private final UserService userService;
    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }
    @Operation(summary = "用户注册")
    @PostMapping("/register")
    public Result userRegister(@RequestPart(value = "avatarFile",required = false) MultipartFile file,@Valid @ModelAttribute UserDto userDto) {
        userService.userRegister(file,userDto);
        return Result.success();
    }
    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public Result userLogin(@Valid @RequestBody LoginDto loginDto) {
        log.info("用户登录");
        Map<String, Object> map = userService.userLogin(loginDto);
        return Result.success(map);
    }

}


