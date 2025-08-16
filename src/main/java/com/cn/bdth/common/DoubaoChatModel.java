package com.cn.bdth.common;

import jakarta.annotation.Resource;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.model.Generation;

import java.util.List;
import java.util.stream.Collectors;

public class DoubaoChatModel implements ChatModel {

    @Resource
    private WebClient webClient;

    public DoubaoChatModel(String baseUrl, String apiKey) {
        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .build();
    }

    @Override
    public ChatResponse call(Prompt prompt) {
        // 提取 Spring AI 的消息为 API 兼容格式
        List<DoubaoModelRequest.Message> requestMessages = prompt.getInstructions().stream()
                .map(m -> new DoubaoModelRequest.Message(toRole(m), m.getText()))
                .collect(Collectors.toList());

        DoubaoModelRequest request = new DoubaoModelRequest("doubao-seed-1-6-250615", requestMessages, 0.7);

        DoubaoModelResponse response = webClient.post()
                .uri("/v3/chat/completions")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(DoubaoModelResponse.class)
                .block();

        String output = response.getChoices().get(0).getMessage().getContent();
        AssistantMessage assistantMessage = new AssistantMessage(output);
        return new ChatResponse(List.of(new Generation(assistantMessage)));
    }

    private String toRole(Message m) {
        switch (m.getMessageType()) {
            case USER -> { return "user"; }
            case SYSTEM -> { return "system"; }
            case ASSISTANT -> { return "assistant"; }
            default -> throw new IllegalArgumentException("Unknown role");
        }
    }
}
