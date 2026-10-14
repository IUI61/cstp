package org.example.chatservice.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MessageDto {
    @NotNull(message = "会话ID不能为空")
    private Long conversationId;
    private String content;
}
