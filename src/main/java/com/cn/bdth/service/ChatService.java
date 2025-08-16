package com.cn.bdth.service;

import com.cn.bdth.dto.MessageDto;
import com.cn.bdth.entity.ConversationUser;
import com.cn.bdth.entity.SpringAiChatMemory;
import reactor.core.publisher.Flux;

import java.util.List;

public interface ChatService {
    List<SpringAiChatMemory> getHistory(String chatId);

    void deleteHistory(String chatId);

    List<ConversationUser> conversationList();

    Flux<String> aiChat(MessageDto messageDto);
}
