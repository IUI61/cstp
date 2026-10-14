package org.example.chatservice.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserBrief {
    private Long id;
    private String name;
    private String avatar;
}

