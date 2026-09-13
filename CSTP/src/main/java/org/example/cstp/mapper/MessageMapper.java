package org.example.cstp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.example.cstp.entity.Message;
import org.example.cstp.vo.MessagesListVo;

import java.util.List;

public interface MessageMapper extends BaseMapper<Message> {
    List<MessagesListVo> getMessagesList(Long conversationId);
}
