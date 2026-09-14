package org.example.cstp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.example.cstp.dto.ChatDto;
import org.example.cstp.dto.MessageDto;
import org.example.cstp.entity.Conversation;
import org.example.cstp.entity.Message;
import org.example.cstp.entity.User;
import org.example.cstp.exception.BusinessException;
import org.example.cstp.mapper.ConversationMapper;
import org.example.cstp.mapper.MessageMapper;
import org.example.cstp.mapper.UserMapper;
import org.example.cstp.service.ChatService;
import org.example.cstp.util.UserContext;
import org.example.cstp.vo.ConversationsListVo;
import org.example.cstp.vo.MessagesListVo;
import org.example.cstp.ws.ChatWebSocketHandler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class ChatServiceImpl implements ChatService {
    private final ConversationMapper conversationMapper;
    private final MessageMapper messageMapper;
    private final ChatWebSocketHandler chatWebSocketHandler;
    private final UserMapper userMapper;

    public ChatServiceImpl(ConversationMapper conversationMapper,
                           MessageMapper messageMapper,
                           ChatWebSocketHandler chatWebSocketHandler,
                           UserMapper userMapper) {
        this.conversationMapper = conversationMapper;
        this.messageMapper = messageMapper;
        this.chatWebSocketHandler = chatWebSocketHandler;
        this.userMapper = userMapper;
    }

    @Override
    @Transactional
    public String createChat(ChatDto chatDto) {
        Long sellerId = chatDto.getSellerId();
        Long buyerId = currentUserId();
        Conversation conversation = new Conversation();
        conversation.setMinId(Math.min(sellerId, buyerId));
        conversation.setMaxId(Math.max(sellerId, buyerId));
        conversation.setGoodsId(chatDto.getGoodsId());
        LambdaQueryWrapper<Conversation> queryWrapper = new LambdaQueryWrapper<>();
        Conversation existing = conversationMapper.selectOne(queryWrapper.eq(Conversation::getMinId, conversation.getMinId())
                .eq(Conversation::getMaxId, conversation.getMaxId()).eq(Conversation::getIsDelete, 0));
        //检查是否有重复会话
        if (existing == null) {
            conversationMapper.insert(conversation);
            //ws推送买家信息会话给卖家
            Map<String, Object> pushData = new HashMap<>();
            ConversationsListVo conversationsListVo = userMapper.findById(buyerId);
            conversationsListVo.setConversationId(conversation.getId());
            conversationsListVo.setUpdateTime(LocalDateTime.now());
            pushData.put("conversation", conversationsListVo);
            pushData.put("type", "conversation");
            chatWebSocketHandler.pushToUser(sellerId, pushData);
            return conversation.getId().toString();
        }
        log.info("createChat: {}", conversation.getId());
        return existing.getId().toString();
    }
    @Override
    public List<MessagesListVo> getMessagesList(Long conversationId) {
        // 校验当前用户是不是该会话成员
        Long userId = currentUserId();
        Conversation conversation = conversationMapper.selectById(conversationId);
        if (conversation == null || !(conversation.getMinId().equals(userId) || conversation.getMaxId().equals(userId))) {
            throw new BusinessException(403, "无权限查看");
        }
        List<MessagesListVo> messagesListVo = messageMapper.getMessagesList(conversationId);
        // 反转列表顺序
        Collections.reverse(messagesListVo);
        return messagesListVo;
    }
    @Override
    @Transactional
    public Map<String, Object> sendMessage(MessageDto messageDto) {
        Long conversationId = messageDto.getConversationId();
        Long senderId = currentUserId();

        Message message = new Message();
        message.setConversationId(conversationId);
        message.setSenderId(senderId);
        message.setContent(messageDto.getContent());
        messageMapper.insert(message);

        Conversation conversation = conversationMapper.selectById(conversationId);
        if (conversation != null) {
            // 更新会话列表的最后一条消息
            Conversation update = new Conversation();
            update.setId(conversationId);
            update.setLastMessage(message.getContent());
            update.setLastTime(LocalDateTime.now());
            conversationMapper.updateById(update);

            // 通过 minId/maxId 找到会话中的另一方，WebSocket 实时推送
            Long otherId = conversation.getMinId().equals(senderId)
                    ? conversation.getMaxId() : conversation.getMinId();
            Map<String, Object> pushData = new HashMap<>();
            pushData.put("type", "message");
            pushData.put("conversationId", conversationId.toString());
            pushData.put("id", message.getId().toString());        // 雪花id转字符串，避免前端精度丢失
            pushData.put("senderId", senderId.toString());
            pushData.put("content", message.getContent());
            chatWebSocketHandler.pushToUser(otherId, pushData);
        }

        // 回传消息信息，发送方前端据此本地立即渲染
        Map<String, Object> result = new HashMap<>();
        result.put("id", message.getId().toString());
        result.put("senderId", senderId.toString());
        result.put("content", message.getContent());
        return result;
    }
    @Override
    public List<ConversationsListVo> getConversationsList() {
        Long userId = currentUserId();
        return conversationMapper.getConversationsList(userId);
    }
    public Long currentUserId() {
        Long userId = UserContext.getId();
        if(userId == null){
            throw new BusinessException(401, "请先登入");
        }
        return userId;
    }
}
