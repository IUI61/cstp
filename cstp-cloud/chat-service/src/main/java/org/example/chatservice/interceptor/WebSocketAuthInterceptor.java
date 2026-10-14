package org.example.chatservice.interceptor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;

@Slf4j
@Component
public class WebSocketAuthInterceptor implements HandshakeInterceptor {
    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;
    public WebSocketAuthInterceptor(StringRedisTemplate stringRedisTemplate,
                                    ObjectMapper objectMapper) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.objectMapper = objectMapper;
    }
    @Override
    public boolean beforeHandshake(ServerHttpRequest request,
                                   ServerHttpResponse response,
                                   WebSocketHandler wsHandler,
                                   Map<String, Object> attributes){
        String token = UriComponentsBuilder.fromUri(request.getURI()).build().getQueryParams().getFirst("token");
        if (token == null){
            log.info("WebSocket握手失败：缺少token");
            return false;
        }
        String json = stringRedisTemplate.opsForValue().get("token:" + token);
        if (json == null){
            log.info("WebSocket握手失败：token无效");
            return false;
        }
        try{
            Long userId = objectMapper.readTree(json).get("id").asLong();
            //attributes是WebSocket会话的属性，用于存储用户信息
            attributes.put("userId", userId);
            return true;
        } catch (JsonProcessingException e){
            log.info("WebSocket握手失败：解析用户信息异常", e);
            return false;
        }
    }
    @Override
    public void afterHandshake(ServerHttpRequest request,
                               ServerHttpResponse response,
                               WebSocketHandler wsHandler,
                               Exception exception){
    }
}
