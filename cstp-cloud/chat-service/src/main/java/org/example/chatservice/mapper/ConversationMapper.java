package org.example.chatservice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.example.chatservice.entity.Conversation;
import org.example.chatservice.vo.ConversationsListVo;

import java.util.List;

public interface ConversationMapper extends BaseMapper<Conversation> {
    List<ConversationsListVo> getConversationsList(Long userId);
}
