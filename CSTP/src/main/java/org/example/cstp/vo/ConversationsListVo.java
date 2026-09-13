package org.example.cstp.vo;

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
}
