package org.example.chatservice.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CurrentUser {
    private Long id;
    private String username;
    private String phone;
    private String avatar;
}

