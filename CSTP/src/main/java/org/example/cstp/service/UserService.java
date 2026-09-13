package org.example.cstp.service;

import org.example.cstp.dto.LoginDto;
import org.example.cstp.entity.User;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

public interface UserService {
    void userRegister(MultipartFile file, User user);

    Map<String, Object> userLogin(LoginDto loginDto);
}
