package org.example.userservice.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.userservice.dto.LoginDto;
import org.example.userservice.dto.UserDto;
import org.example.userservice.entity.User;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface UserService {
    void userRegister(MultipartFile file, UserDto userDto);

    Map<String, Object> userLogin(LoginDto loginDto);
    /** 批量按 id 查用户（订单服务要） */
    List<User> listByIds(List<Long> ids);

    User getById(Long id);
}
