package org.example.cstp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.example.cstp.dto.LoginDto;
import org.example.cstp.entity.User;
import org.example.cstp.exception.BusinessException;
import org.example.cstp.mapper.UserMapper;
import org.example.cstp.service.UserService;
import org.example.cstp.util.FileUtil;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
@Slf4j
@Service
public class UserServiceImpl implements UserService {
    private final UserMapper userMapper;
    private final BCryptPasswordEncoder passwordEncoder;
    private final ObjectMapper objectMapper;
    private final StringRedisTemplate stringRedisTemplate;

    public UserServiceImpl(UserMapper userMapper,
                           BCryptPasswordEncoder passwordEncoder,
                           ObjectMapper objectMapper,
                           StringRedisTemplate stringRedisTemplate) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.objectMapper = objectMapper;
        this.stringRedisTemplate = stringRedisTemplate;

    }
    // 用户注册
    @Override
    @Transactional
    public void userRegister(MultipartFile file, User user){
        //检查用户名是否已存在
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, user.getUsername());  // WHERE username = ?
        Long count = userMapper.selectCount(wrapper);
        if(count > 0){
            throw new BusinessException(400,"用户已存在");
        }
        //检查手机号是否已存在
        wrapper.eq(User::getPhone, user.getPhone());
        count = userMapper.selectCount(wrapper);
        if(count > 0){
            throw new BusinessException(400,"手机号已占用");
        }
        // 对密码进行加密
        String rawPassword = user.getPassword();
        String encodedPassword = passwordEncoder.encode(rawPassword);
        user.setPassword(encodedPassword);
        // 上传头像
        if(file != null){
            user.setAvatar(FileUtil.imageUpload(file,"avatar"));
        }
        userMapper.insert(user);
    }

    // 用户登录
    @Override
    public Map<String, Object> userLogin(LoginDto loginDto){
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, loginDto.getUsername());
        User user = userMapper.selectOne(wrapper);
        if(user == null){
            log.info("用户名不存在");
            throw new BusinessException(400,"用户名或密码错误");
        }
        if(!passwordEncoder.matches(loginDto.getPassword(), user.getPassword())){
            log.info("密码错误");
            throw new BusinessException(400,"用户名或密码错误");
        }
        String token = UUID.randomUUID().toString();
        user.setPassword(null);
        try {
            String json = objectMapper.writeValueAsString(user);
            stringRedisTemplate.opsForValue().set("token:" + token, json, Duration.ofDays(7));
            log.info("用户登录成功，生成的token为" + token);
            Map<String, Object> map = new HashMap<>();
            map.put("token", token);
            map.put("user", user);
            return map;
        } catch (JsonProcessingException e) {
            throw new BusinessException(500, "序列化用户信息失败");
        }
    }
}
