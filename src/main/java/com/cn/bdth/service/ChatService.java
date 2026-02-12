package com.cn.bdth.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cn.bdth.dto.MessageDto;
import com.cn.bdth.dto.ChatDto;
import com.cn.bdth.entity.*;
import com.cn.bdth.vo.TransitVo;
import com.cn.bdth.vo.ChatListVo;
import com.cn.bdth.vo.ChatVo;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;

import java.net.MalformedURLException;
import java.util.List;

public interface ChatService {
    Page<SpringAiChatMemory> getHistory(String chatId, Integer pageNum);

    void deleteHistory(String chatId);

    List<ConversationUser> conversationList();

    Flux<String> aiChat(MessageDto messageDto) throws MalformedURLException;

    void updateRag(MultipartFile file);

    List<Mcps> mcpList();

    List<Rag> ragList();

    void deleteRag(Long ragId);

    void toggleRag(Long ragId);

    Flux<String> aiChat2(MessageDto messageDto);

    TransitVo transit(MessageDto messageDto);

    Flux<String> drawCallback(MessageDto messageDto);

    ChatList initiate(Long userId);

    void message(ChatDto chatDto);

    List<ChatVo> messageList(Long chatListId, Integer pageNum);

    Page<ChatListVo> chatList(Integer pageNum);

    Page<Group> groupList(Integer pageNum);
}
