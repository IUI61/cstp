package org.example.cstp.interceptor;

import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.cstp.entity.User;
import org.example.cstp.util.UserContext;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.data.redis.core.StringRedisTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class LoginInterceptor implements HandlerInterceptor {
    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    public LoginInterceptor(StringRedisTemplate stringRedisTemplate,
                            ObjectMapper objectMapper) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.objectMapper = objectMapper;

    }

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {
        String token = request.getHeader("Authorization");
        String json = token == null ? null : stringRedisTemplate.opsForValue().get("token:" + token);
        if(json == null){
            response.setStatus(401);
            return false;
        }
       try{
           UserContext.setUser(objectMapper.readValue(json, User.class));
           return true;
       }catch (JsonProcessingException e){
           response.setStatus(401);
           return false;
       }
    }
    @Override
    public void afterCompletion(HttpServletRequest request,
                                HttpServletResponse response,
                                Object handler,
                                Exception ex) throws Exception {
        UserContext.removeUser();
    }
}
