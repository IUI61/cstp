package org.example.cstp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.example.cstp.entity.Conversation;
import org.example.cstp.vo.ConversationsListVo;

import java.util.List;

public interface ConversationMapper extends BaseMapper<Conversation> {
    List<ConversationsListVo> getConversationsList(Long userId);
}
