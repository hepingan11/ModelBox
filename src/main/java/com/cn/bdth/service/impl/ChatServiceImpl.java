package com.cn.bdth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cn.bdth.constants.AiModelConstant;
import com.cn.bdth.dto.MessageDto;
import com.cn.bdth.entity.ConversationUser;
import com.cn.bdth.entity.SpringAiChatMemory;
import com.cn.bdth.mapper.ConversationUserMapper;
import com.cn.bdth.mapper.SpringAiChatMemoryMapper;
import com.cn.bdth.service.ChatService;
import com.cn.bdth.utils.AliUploadUtils;
import com.cn.bdth.utils.UserUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.modelcontextprotocol.client.McpAsyncClient;
import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.transport.HttpClientSseClientTransport;
import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.spec.McpClientTransport;
import io.modelcontextprotocol.util.DefaultMcpUriTemplateManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.content.Media;
import org.springframework.ai.mcp.AsyncMcpToolCallback;
import org.springframework.ai.mcp.client.autoconfigure.NamedClientMcpTransport;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.definition.ToolDefinition;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.zhipuai.ZhiPuAiChatModel;
import org.springframework.ai.zhipuai.ZhiPuAiChatOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.*;

import static org.springframework.ai.chat.memory.ChatMemory.CONVERSATION_ID;

@Service
@Slf4j
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final SpringAiChatMemoryMapper springAiChatMemoryMapper;

    private final ConversationUserMapper conversationUserMapper;

    private final VectorStore vectorStore;

    private final ZhiPuAiChatModel zhiPuAiChatModel;

    private final OpenAiChatModel deepseekModel;

    private final OpenAiChatModel qwenModel;

    private final OpenAiChatModel gorkModel;

    private final OpenAiChatModel geminiModel;

    private final AliUploadUtils aliUploadUtils;

    private final OpenAiChatModel openaiModel;

    private final OpenAiChatModel claudeModel;

    private final ToolCallbackProvider toolCallbackProvider;

    private final ChatMemory chatMemory;


    @Value("${ali-oss.domain}")
    private String domin;

    @Override
    public List<SpringAiChatMemory> getHistory(String chatId) {
        return springAiChatMemoryMapper.selectList(new QueryWrapper<SpringAiChatMemory>().lambda()
                .eq(SpringAiChatMemory::getConversationId, chatId));
    }

    @Override
    public void deleteHistory(String chatId) {
        String modifiedChatId = chatId.substring(0, chatId.length() - 1);
        springAiChatMemoryMapper.delete(new QueryWrapper<SpringAiChatMemory>().lambda()
                .eq(SpringAiChatMemory::getConversationId, modifiedChatId));
        conversationUserMapper.delete(new QueryWrapper<ConversationUser>().lambda()
                .eq(ConversationUser::getConversationId, modifiedChatId));

    }

    @Override
    public List<ConversationUser> conversationList() {
        return conversationUserMapper.selectList(new QueryWrapper<ConversationUser>().lambda()
                .eq(ConversationUser::getUserId, UserUtils.getCurrentLoginId())
                .orderByDesc(ConversationUser::getCreatedTime));
    }


    @Override
    public Flux<String> aiChat(MessageDto messageDto) {
        ConversationUser conversationUser = conversationUserMapper.selectOne(new QueryWrapper<ConversationUser>().lambda()
                .eq(ConversationUser::getConversationId, messageDto.getChatId()));
        Long currentLoginId = UserUtils.getCurrentLoginId();
        if (conversationUser == null){
            conversationUserMapper.insert(new ConversationUser()
                    .setConversationId(messageDto.getChatId())
                    .setCreatedTime(LocalDateTime.now())
                    .setTitle(LocalDateTime.now().toString().substring(5,9)+"新对话")
                    .setUserId(currentLoginId));
        }

        if (messageDto.getModel() ==null || messageDto.getModel().isEmpty()){
            messageDto.setModel("DEEPSEEK");
        }


        switch (messageDto.getModel()) {
            case "GPT"->{
                if (messageDto.getFile() == null){
                    return ChatClient.builder(openaiModel)
                            .defaultSystem("你叫何平安，是一个高冷酷酷的IT高手")
                            .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                            .defaultToolCallbacks(toolCallbackProvider)
                            .build()
                            .prompt()
                            .user(messageDto.getMessage())

//                            .advisors(QuestionAnswerAdvisor.builder(vectorStore)
//                                    .searchRequest(SearchRequest.builder()
//                                            .topK(5)//最多 5 个检索结果
//                                            .similarityThreshold(0.6)//最小相似度匹配百分比
//                                            .build()).build())
                            .advisors(advisorSpec -> advisorSpec.param(CONVERSATION_ID, messageDto.getChatId()))
                            .stream()
                            .content();
                }
                String fileContent = "";
                MediaType mediaType = MediaType.valueOf(Objects.requireNonNull(messageDto.getFile().getContentType()));
                String type = mediaType.getType();
                boolean isImage = "image".equals(type);
                String originalFilename = messageDto.getFile().getOriginalFilename();
                String fileExtension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1);
                final String fileName = UUID.randomUUID() + "."+fileExtension;
                String url = domin+aliUploadUtils.uploadFile(messageDto.getFile(), "chat", fileName, isImage);
                Media media = new Media(mediaType, URI.create(url));
                if (isImage){
                    fileContent ="<picture>"+url+"</picture>";
                }else {
                    fileContent ="<file>"+url+"</file>";
                }


                OpenAiChatOptions zhiPuAiChatOptions = OpenAiChatOptions.builder()
                        .model("gpt-5")
                        .build();
                UserMessage userMessage = UserMessage.builder().media(media).text(messageDto.getMessage()).build();
                ChatResponse chatResponseFlux =openaiModel.call(new Prompt(List.of(userMessage), zhiPuAiChatOptions));
                String text = chatResponseFlux.getResult().getOutput().getText();
                String finalFileContent = fileContent;
                springAiChatMemoryMapper.insert(new SpringAiChatMemory()
                        .setContent(messageDto.getMessage()+ finalFileContent)
                        .setType("USER")
                        .setConversationId(messageDto.getChatId())
                        .setTimestamp(LocalDateTime.now()));
                springAiChatMemoryMapper.insert(new SpringAiChatMemory()
                        .setContent(text)
                        .setType("ASSISTANT")
                        .setConversationId(messageDto.getChatId())
                        .setTimestamp(LocalDateTime.now()));
                return Flux.just(text);


            }
            case "CLAUDE" ->{
                    return ChatClient.builder(claudeModel)
                            .defaultSystem("你叫何平安，是一个高冷酷酷的IT高手")
                            .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                            .build()
                            .prompt()
                            .user(messageDto.getMessage())
                            .advisors(advisorSpec -> advisorSpec.param(CONVERSATION_ID, messageDto.getChatId()))
                            .stream()
                            .content();
            }
            case "DEEPSEEK" -> {
                    return ChatClient.builder(deepseekModel)
                            .defaultSystem("你叫何平安，是一个高冷酷酷的IT高手")
                            .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                            .defaultToolCallbacks(toolCallbackProvider)
                            .build()
                            .prompt()
                            .user(messageDto.getMessage())
                            .advisors(QuestionAnswerAdvisor.builder(vectorStore)
                                    .searchRequest(SearchRequest.builder()
                                            .topK(5)//最多 5 个检索结果
                                            .similarityThreshold(0.6)//最小相似度匹配百分比
                                            .build()).build())
                            .advisors(advisorSpec -> advisorSpec.param(CONVERSATION_ID, messageDto.getChatId()))
                            .stream()
                            .content();
            }
            case "QWEN" ->{
                return ChatClient.builder(qwenModel)
                        .defaultSystem("你叫何平安，是一个高冷酷酷的IT高手")
                        .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                        .build()
                        .prompt()
                        .user(messageDto.getMessage())
                        .advisors(advisorSpec -> advisorSpec.param(CONVERSATION_ID, messageDto.getChatId()))
                        .stream()
                        .content();
            }
            case "GEMINI" -> {
                    return ChatClient.builder(geminiModel)
                            .defaultSystem("你叫何平安，是一个高冷酷酷的IT高手")
                            .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                            .build()
                            .prompt()
                            .user(messageDto.getMessage())
                            .advisors(QuestionAnswerAdvisor.builder(vectorStore)
                                    .searchRequest(SearchRequest.builder()
                                            .topK(5)//最多 5 个检索结果
                                            .similarityThreshold(0.6)//最小相似度匹配百分比
                                            .build()).build())
                            .advisors(advisorSpec -> advisorSpec.param(CONVERSATION_ID, messageDto.getChatId()))
                            .stream()
                            .content();
            }
            case "GROK" ->{
                return ChatClient.builder(gorkModel)
                        .defaultSystem("你叫何平安，是一个高冷酷酷的IT高手")
                        .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                        .build()
                        .prompt()
                        .user(messageDto.getMessage())
                        .advisors(QuestionAnswerAdvisor.builder(vectorStore)
                                .searchRequest(SearchRequest.builder()
                                        .topK(5)//最多 5 个检索结果
                                        .similarityThreshold(0.6)//最小相似度匹配百分比
                                        .build()).build())
                        .advisors(advisorSpec -> advisorSpec.param(CONVERSATION_ID, messageDto.getChatId()))
                        .stream()
                        .content();
            }
            case "GLM" ->{
                if (messageDto.getFile() == null){
                    return ChatClient.builder(zhiPuAiChatModel)
                            .defaultSystem("你名字叫何平安，是一个高冷酷酷的IT高手")
                            .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                            .build()
                            .prompt()
                            .user(messageDto.getMessage())
                            .advisors(QuestionAnswerAdvisor.builder(vectorStore)
                                    .searchRequest(SearchRequest.builder()
                                            .topK(5)//最多 5 个检索结果
                                            .similarityThreshold(0.6)//最小相似度匹配百分比
                                            .build()).build())
                            .advisors(advisorSpec -> advisorSpec.param(CONVERSATION_ID, messageDto.getChatId()))
                            .stream()
                            .content();
                }
                String fileContent = "";
                MediaType mediaType = MediaType.valueOf(Objects.requireNonNull(messageDto.getFile().getContentType()));
                String type = mediaType.getType();
                boolean isImage = "image".equals(type);
                String originalFilename = messageDto.getFile().getOriginalFilename();
                String fileExtension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1);
                final String fileName = UUID.randomUUID() + "."+fileExtension;
                String url = domin+aliUploadUtils.uploadFile(messageDto.getFile(), "chat", fileName, isImage);
                Media media = new Media(mediaType, URI.create(url));
                if (isImage){
                    fileContent ="<picture>"+url+"</picture>";
                }else {
                    fileContent ="<file>"+url+"</file>";
                }

                ZhiPuAiChatOptions zhiPuAiChatOptions = ZhiPuAiChatOptions.builder()
                        .model(AiModelConstant.GLM)
                        .build();
                UserMessage userMessage = UserMessage.builder().media(media).text(messageDto.getMessage()).build();
                Flux<ChatResponse> chatResponseFlux =zhiPuAiChatModel.stream(new Prompt(List.of(userMessage), zhiPuAiChatOptions));
                Flux<String> map = chatResponseFlux.mapNotNull(chatResponse -> chatResponse.getResult().getOutput().getText());
                String finalFileContent = fileContent;
                map.doOnNext(chunk -> {}).reduce((s1, s2) -> s1 + s2).subscribe(
                        result -> {
                            // 流式结束后，
                            springAiChatMemoryMapper.insert(new SpringAiChatMemory()
                                    .setContent(messageDto.getMessage()+ finalFileContent)
                                    .setType("USER")
                                    .setConversationId(messageDto.getChatId())
                                    .setTimestamp(LocalDateTime.now()));
                            springAiChatMemoryMapper.insert(new SpringAiChatMemory()
                                    .setContent(result)
                                    .setType("ASSISTANT")
                                    .setConversationId(messageDto.getChatId())
                                    .setTimestamp(LocalDateTime.now()));
                        }
                );
                return map;

            }
        }
        return Flux.empty();
    }
}
