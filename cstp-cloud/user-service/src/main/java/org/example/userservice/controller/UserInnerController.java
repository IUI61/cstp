package org.example.userservice.controller;

import org.example.userservice.entity.Result;
import org.example.userservice.entity.User;
import org.example.userservice.service.UserService;
import org.example.userservice.vo.UserBrief;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 内部接口：只给其他服务（Feign）调用，不对前端暴露
 */
@RestController
@RequestMapping("/user/inner")
public class UserInnerController {
    private final UserService userService;

    public UserInnerController(UserService userService) {
        this.userService = userService;
    }

    /** 批量查用户简讯（订单列表显示对方姓名/头像） */
    @PostMapping("/batch")
    public Result batch(@RequestBody List<Long> ids) {
        List<User> users = userService.listByIds(ids);
        List<UserBrief> briefs = users.stream().map(u -> {
            UserBrief b = new UserBrief();
            b.setId(u.getId());
            b.setName(u.getName());
            b.setAvatar(u.getAvatar());
            return b;
        }).collect(Collectors.toList());
        return Result.success(briefs);
    }
}
