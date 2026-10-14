package org.example.chatservice.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ConversationsListVo {
    @JsonSerialize(using = ToStringSerializer.class)
    private Long conversationId;
    private String lastMessage;
    private LocalDateTime updateTime;
    private String hisName;
    private String hisAvatar;
    private Long otherId;   // 对方的用户 id（SQL 里算出来，Feign 批量查用）

}
