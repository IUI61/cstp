package org.example.cstp.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

@Data
public class MessagesListVo {
    @JsonSerialize(using = ToStringSerializer.class)
   private Long id;
    @JsonSerialize(using = ToStringSerializer.class)
   private Long senderId;
   private String content;
}
