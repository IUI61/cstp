package org.example.cstp.service;

import org.example.cstp.dto.ChatDto;
import org.example.cstp.dto.MessageDto;
import org.example.cstp.vo.ConversationsListVo;
import org.example.cstp.vo.MessagesListVo;
import java.util.List;
import java.util.Map;

public interface ChatService {
    String createChat(ChatDto chatDto);
    List<MessagesListVo> getMessagesList(Long conversationId);
    Map<String, Object> sendMessage(MessageDto messageDto);

    List<ConversationsListVo> getConversationsList();
}
