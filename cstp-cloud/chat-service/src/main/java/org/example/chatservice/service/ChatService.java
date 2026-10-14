package org.example.chatservice.service;

import org.example.chatservice.dto.ChatDto;
import org.example.chatservice.dto.MessageDto;
import org.example.chatservice.vo.ConversationsListVo;
import org.example.chatservice.vo.MessagesListVo;

import java.util.List;
import java.util.Map;

public interface ChatService {
    String createChat(ChatDto chatDto);
    List<MessagesListVo> getMessagesList(Long conversationId);
    Map<String, Object> sendMessage(MessageDto messageDto);

    List<ConversationsListVo> getConversationsList();
}
