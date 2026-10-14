package org.example.chatservice.controller;

import jakarta.validation.Valid;
import org.example.chatservice.dto.ChatDto;
import org.example.chatservice.dto.MessageDto;
import org.example.chatservice.entity.Result;
import org.example.chatservice.service.ChatService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/chat")
public class ChatController {
    private final ChatService chatService;
    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping("/create")
    public Result createChat(@Valid @RequestBody ChatDto chatDto) {
        return Result.success(chatService.createChat(chatDto));
    }
    @GetMapping("/messagesList")
    public Result getMessagesList(@RequestParam Long conversationId) {
        return Result.success(chatService.getMessagesList(conversationId));
    }
    @PostMapping("/sendMessage")
    public Result sendMessage(@Valid @RequestBody MessageDto messageDto) {
        return Result.success(chatService.sendMessage(messageDto));
    }
    @GetMapping("/conversationsList")
    public Result getConversationsList() {
        return Result.success(chatService.getConversationsList());
    }
}
