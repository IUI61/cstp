package org.example.chatservice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.example.chatservice.entity.Message;
import org.example.chatservice.vo.MessagesListVo;

import java.util.List;

public interface MessageMapper extends BaseMapper<Message> {
    List<MessagesListVo> getMessagesList(Long conversationId);
}
