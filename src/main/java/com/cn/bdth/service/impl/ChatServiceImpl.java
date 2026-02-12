package com.cn.bdth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cn.bdth.common.MysqlChatMemory;
import com.cn.bdth.config.AiConfig;
import com.cn.bdth.config.McpListConfig;
import com.cn.bdth.constants.AiModelConstant;
import com.cn.bdth.dto.MessageDto;
import com.cn.bdth.dto.ChatDto;
import com.cn.bdth.entity.*;
import com.cn.bdth.mapper.*;
import com.cn.bdth.service.ChatService;
import com.cn.bdth.utils.AliUploadUtils;
import com.cn.bdth.utils.BeanUtils;
import com.cn.bdth.utils.UserUtils;
import com.cn.bdth.vo.TransitVo;
import com.cn.bdth.vo.ChatListVo;
import com.cn.bdth.vo.ChatVo;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.content.Media;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.zhipuai.ZhiPuAiChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;

import java.net.MalformedURLException;
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

    private final McpListConfig mcpListConfig;

    private final ZhiPuAiChatModel zhiPuAiChatModel;

    private final AliUploadUtils aliUploadUtils;

    private final OpenAiChatModel openaiModel;

    private final MysqlChatMemory chatMemory;

    private final AiConfig aiConfig;

    private final RagMapper ragMapper;

    private final McpsMapper mcpsMapper;

    private final DrawingMapper drawingMapper;

    private final UserMapper userMapper;

    private final ChatMapper chatMapper;

    private final ChatListMapper chatListMapper;

    private final ProjectMapper projectMapper;

    private final ProjectMemberMapper projectMemberMapper;

    private final GroupMapper groupMapper;

    @Value("${ali-oss.domain}")
    private String domin;

    @Override
    public Page<SpringAiChatMemory> getHistory(String chatId,Integer pageNum) {
        Page<SpringAiChatMemory> p = new Page<>(pageNum, 20);
        return springAiChatMemoryMapper.selectPage(p,new QueryWrapper<SpringAiChatMemory>().lambda()
                .eq(SpringAiChatMemory::getConversationId, chatId)
                .orderByDesc(SpringAiChatMemory::getId));
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
                .orderByDesc(ConversationUser::getUpdatedTime));
    }


    @Override
    public Flux<String> aiChat(MessageDto messageDto) throws MalformedURLException {
        ConversationUser conversationUser = conversationUserMapper.selectOne(new QueryWrapper<ConversationUser>().lambda()
                .eq(ConversationUser::getConversationId, messageDto.getChatId()));
        Long currentLoginId = UserUtils.getCurrentLoginId();
        User user = userMapper.selectById(currentLoginId);
        if (messageDto.getModel().contains(AiModelConstant.PAY_MODEL)){
            if (user.getFrequency() < 2){
                return Flux.just("使用付费模型，您的积分不足2，请先充值");
            }
        }
        if (conversationUser == null){
            conversationUserMapper.insert(new ConversationUser()
                    .setConversationId(messageDto.getChatId())
                    .setCreatedTime(LocalDateTime.now())
                            .setUpdatedTime(LocalDateTime.now())
                    .setTitle(LocalDateTime.now().toString().substring(5,10)+"新对话")
                    .setUserId(currentLoginId));
        }

        if (messageDto.getIsRag()){
            List<Rag> rags = ragMapper.selectList(new QueryWrapper<Rag>().lambda()
                    .eq(Rag::getUserId, currentLoginId).eq(Rag::getIsEnable, true));
            if (rags != null && !rags.isEmpty()){
                for (Rag rag : rags){
                    Resource resource = new UrlResource(domin+rag.getRagUrl());
                    vectorStore.write(new TokenTextSplitter().transform(new TextReader(resource).read()));
                }
            }
        }

        //添加mcp
        List<ToolCallback> toolCallbacksFromAsyncClients = new ArrayList<>();
        if (messageDto.getMcpList() != null && messageDto.getIsMcp()){
            for (Long mcpId : messageDto.getMcpList()){
                String methodName = mcpsMapper.selectById(mcpId).getMethodName();
                List<ToolCallback> toolCallbackByName = mcpListConfig.getToolCallbackByName(methodName);
                if (toolCallbackByName != null) {
                    toolCallbacksFromAsyncClients.addAll(toolCallbackByName);
                }
            }
        }
        ToolCallbackProvider toolCallbackProvider =ToolCallbackProvider.from(toolCallbacksFromAsyncClients);


        // 如果messageDto中的role字段为null，则设置默认角色描述；否则使用传入的role值
        String role="你名字叫何平安，是一个高冷酷酷的IT高手";
        if (conversationUser != null) {
            if (conversationUser.getRole() != null && !conversationUser.getRole().isEmpty()){
                role = conversationUser.getRole();
            }
        }

        if (messageDto.getModel() ==null || messageDto.getModel().isEmpty()){
            messageDto.setModel("GLM");
        }

        if (conversationUser != null) {
            conversationUserMapper.update(conversationUser.setUpdatedTime(LocalDateTime.now()),new QueryWrapper<ConversationUser>().lambda()
                    .eq(ConversationUser::getConversationId, messageDto.getChatId()));
        }


        switch (messageDto.getModel()) {
            case "GPT"->{
                if (messageDto.getFile() == null){
                    return ChatWithoutFile(messageDto, openaiModel, AiModelConstant.GPT,role,toolCallbackProvider);
                }else {
                    return ChatWithFile(messageDto, openaiModel, AiModelConstant.GPT, role);
                }
            }
            case "CLAUDE" ->{
                OpenAiChatModel claudeModel = aiConfig.claudeModel();
                if (messageDto.getFile() == null){
                    return ChatWithoutFile(messageDto, claudeModel, AiModelConstant.CLAUDE,role,toolCallbackProvider);
                }else {
                    return ChatWithFile(messageDto, claudeModel, AiModelConstant.CLAUDE,role);
                }
            }
            case "DEEPSEEK" -> {
                if (messageDto.getFile() == null){
                    return ChatWithoutFile(messageDto,aiConfig.deepseekModel(),AiModelConstant.DEEPSEEK,role,toolCallbackProvider);
                }else {
                    return ChatWithFile(messageDto,aiConfig.deepseekModel(),AiModelConstant.DEEPSEEK,role);
                }
            }
            case "QWEN" ->{
                if (messageDto.getFile() == null){
                    return ChatWithoutFile(messageDto,aiConfig.qwenModel(),AiModelConstant.QWEN,role,toolCallbackProvider);
                }else {
                    return ChatWithFile(messageDto,aiConfig.qwenModel(),AiModelConstant.QWEN,role);
                }
            }
            case "GEMINI" -> {
                if (messageDto.getFile() == null){
                    return ChatWithoutFile(messageDto,aiConfig.geminiModel(),AiModelConstant.GEMINI,role,toolCallbackProvider);
                }else {
                    return ChatWithFile(messageDto,aiConfig.geminiModel(),AiModelConstant.GEMINI,role);
                }
            }
            case "GROK" ->{
                if (messageDto.getFile() == null){
                    return ChatWithoutFile(messageDto,aiConfig.gorkModel(),AiModelConstant.GROK,role,toolCallbackProvider);
                }else {
                    return ChatWithFile(messageDto,aiConfig.gorkModel(),AiModelConstant.GROK,role);
                }
            }
            case "DEEPSEEK_R"->{
                if (messageDto.getFile() == null){
                    return ChatWithoutFile(messageDto,aiConfig.deepseekModel(),AiModelConstant.DEEPSEEK_R,role,toolCallbackProvider);
                }else {
                    return ChatWithFile(messageDto,aiConfig.deepseekModel(),AiModelConstant.DEEPSEEK_R,role);
                }
            }
            case "COMMAND" ->{
                if (messageDto.getFile() == null){
                    return ChatWithoutFile(messageDto,aiConfig.commandModel(),AiModelConstant.COMMAND,role,toolCallbackProvider);
                }else {
                    return ChatWithFile(messageDto,aiConfig.commandModel(),AiModelConstant.COMMAND,role);
                }
            }
            case "DOUBAO" ->{
                OpenAiChatModel doubaoModel = aiConfig.doubaoModel();
                if (messageDto.getFile() == null){
                    return ChatWithoutFile(messageDto, doubaoModel, AiModelConstant.DOUBAO,role,toolCallbackProvider);
                }else {
                    return ChatWithFile(messageDto, doubaoModel, AiModelConstant.DOUBAO,role);
                }
            }
            case "GLM" ->{
                if (messageDto.getFile() == null){
                    return ChatWithoutFile(messageDto, zhiPuAiChatModel, AiModelConstant.GLM, role, toolCallbackProvider);
                }else {
                    return ChatWithFile(messageDto, zhiPuAiChatModel, AiModelConstant.GLM,role);
                }
            }
        }

        return null;
    }

    //检测是否包含绘画信息
    private IsDraw containDrawInfo(String message) {
        IsDraw entity = ChatClient.builder(zhiPuAiChatModel).build()
                .prompt().user(message).call().entity(IsDraw.class);
        if (entity != null){
            return entity;
        }else {
            return new IsDraw(false, "0x0");
        }

    }

    private record IsDraw(@JsonProperty("isDraw")
    @JsonPropertyDescription("用户意图是否想要绘画") Boolean isDraw,
                          @JsonProperty("size")
                          @JsonPropertyDescription("如果用户想要绘制图片，输出想要绘制图片像素比例大小，像素长宽限制在512-2048，以'长x宽'格式输出；若不是绘制图片则输出0x0；若没有要求比例则默认1024x1024")
                          String size){}

    @Override
    public void updateRag(MultipartFile file) {
        List<Rag> rags = ragMapper.selectList(new QueryWrapper<Rag>().lambda().eq(Rag::getUserId, UserUtils.getCurrentLoginId()));
        if (rags.size() == 3){
            throw new RuntimeException("最多只能添加3个知识库");
        }
        String originalFilename = file.getOriginalFilename();
        String fileExtension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1);
        final String fileName = UUID.randomUUID() + "."+fileExtension;
        String url = aliUploadUtils.uploadFile(file, "chat", fileName, false);
        ragMapper.insert(new Rag().setRagName(originalFilename)
                .setRagUrl( url)
                .setIsEnable(true)
                .setUserId(UserUtils.getCurrentLoginId()));
    }

    @Override
    public List<Mcps> mcpList() {
        return mcpsMapper.selectList(null);
    }

    @Override
    public List<Rag> ragList() {
        return ragMapper.selectList(new QueryWrapper<Rag>().lambda()
                .eq(Rag::getUserId, UserUtils.getCurrentLoginId()));
    }

    @Override
    public void deleteRag(Long ragId) {
        aliUploadUtils.deleteFile(ragMapper.selectById(ragId).getRagUrl());
        ragMapper.deleteById(ragId);
    }

    @Override
    public void toggleRag(Long ragId) {
        ragMapper.updateById(new Rag().setRagId(ragId).setIsEnable(!ragMapper.selectById(ragId).getIsEnable()));
    }

    @Override
    public Flux<String> aiChat2(MessageDto messageDto) {
        return ChatClient.builder(zhiPuAiChatModel)
                .defaultSystem("你是一个乐于帮助解决问题的小助手")
                .build().prompt()
                .user(messageDto.getMessage())
                .stream()
                .content();
    }

    //中转
    @Override
    public TransitVo transit(MessageDto messageDto) {
        IsDraw isDraw = containDrawInfo(messageDto.getMessage());
        if(isDraw.isDraw()){
            User user = userMapper.selectById(UserUtils.getCurrentLoginId());
            if (user.getFrequency() < 10){
                throw new RuntimeException("检测到您需要进行绘画，你的积分不足10，请先充值");
            }
            new TransitVo().setType("DRAW");
            return new TransitVo().setType("DRAW").setContent("正在为您绘制图片:"+messageDto.getMessage())
                    .setSize(isDraw.size());
        }else {
            return new TransitVo().setType("TEXT");
        }
    }

    /**
     * 绘画回调
     * @param messageDto
     * @return
     */
    @Override
    public Flux<String> drawCallback(MessageDto messageDto) {
        //TODO 后面要不要考虑加入mcp和rag
        ConversationUser conversationUser = conversationUserMapper.selectOne(new QueryWrapper<ConversationUser>().lambda()
                .eq(ConversationUser::getConversationId, messageDto.getChatId()));
        Long currentLoginId = UserUtils.getCurrentLoginId();

        if (conversationUser == null){
            conversationUserMapper.insert(new ConversationUser()
                    .setConversationId(messageDto.getChatId())
                    .setCreatedTime(LocalDateTime.now())
                    .setUpdatedTime(LocalDateTime.now())
                    .setTitle(LocalDateTime.now().toString().substring(5,10)+"新对话")
                    .setUserId(currentLoginId));
        }

        // 如果messageDto中的role字段为null，则设置默认角色描述；否则使用传入的role值
        String role="你名字叫何平安，是一个高冷酷酷的IT高手";
        if (conversationUser != null) {
            if (conversationUser.getRole() != null && !conversationUser.getRole().isEmpty()){
                role = conversationUser.getRole();
            }
        }

        if(messageDto.getImageUrl().contains(domin)){
            messageDto.setImageUrl(messageDto.getImageUrl().replace(domin,""));
        }

        switch (messageDto.getModel()) {
            case "GPT"->{
                return DrawCallbackChat(messageDto, openaiModel,AiModelConstant.DEEPSEEK,role);
            }
            case "CLAUDE" ->{
                return DrawCallbackChat(messageDto, aiConfig.claudeModel(),AiModelConstant.DEEPSEEK,role);
            }
            case "DEEPSEEK" -> {
                return DrawCallbackChat(messageDto, aiConfig.deepseekModel(),AiModelConstant.DEEPSEEK,role);
            }
            case "QWEN" ->{
                return DrawCallbackChat(messageDto,aiConfig.qwenModel(),AiModelConstant.QWEN,role);
            }
            case "GEMINI" -> {
                return DrawCallbackChat(messageDto,aiConfig.geminiModel(),AiModelConstant.GEMINI,role);
            }
            case "GROK" ->{
                return DrawCallbackChat(messageDto,aiConfig.gorkModel(),AiModelConstant.GROK,role);
            }
            case "DEEPSEEK_R"->{
                return DrawCallbackChat(messageDto,aiConfig.deepseekModel(),AiModelConstant.DEEPSEEK_R,role);
            }
            case "DOUBAO" ->{
                return DrawCallbackChat(messageDto, aiConfig.doubaoModel(), AiModelConstant.DOUBAO,role);
            }
            case "COMMAND" ->{
                return DrawCallbackChat(messageDto, aiConfig.commandModel(), AiModelConstant.COMMAND, role);
            }
            case "GLM" ->{
                return DrawCallbackChat(messageDto, zhiPuAiChatModel, AiModelConstant.GLM, role);
            }
        }
        return Flux.empty();
    }

    /**
     * 绘制回调对话
     * @param messageDto
     * @param model
     * @param modelName
     * @param role
     * @return
     */
    public Flux<String> DrawCallbackChat(MessageDto messageDto, ChatModel model, String modelName, String role) {
        ChatClient chatClient = ChatClient.builder(model)
                .defaultSystem(role)
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                .build();
        Drawing drawing = drawingMapper.selectOne(new QueryWrapper<Drawing>().lambda()
                .eq(Drawing::getGenerateUrl, messageDto.getImageUrl()));

        String question = "(以下属于系统命令并严格执行)现在用户已经绘制完一张图片的状态，请输出绘制完该图的结束总结，并可以优化下提示词；这是用户绘图时的提示词输入："+messageDto.getMessage();
        StringBuilder fullContent = new StringBuilder();

        Flux<String> content = chatClient.prompt()
                .user(question)
                .advisors(advisorSpec -> advisorSpec.param(CONVERSATION_ID, messageDto.getChatId()))
                .stream()
                .content()
                .doOnNext(fragment -> fullContent.append(fragment))
                .doOnComplete(() -> {
                    String completeContent = fullContent.toString();
                    if (drawing != null && drawing.getImage() != null){
                        springAiChatMemoryMapper.insert(new SpringAiChatMemory()
                                .setContent(messageDto.getMessage())
                                .setType("USER")
                                .setMedia(domin+drawing.getImage())
                                .setModel(modelName)
                                .setIsMcp(messageDto.getIsMcp())
                                .setIsRag(messageDto.getIsRag())
                                .setConversationId(messageDto.getChatId())
                                .setTimestamp(LocalDateTime.now()));
                    }else {
                        springAiChatMemoryMapper.insert(new SpringAiChatMemory()
                                .setContent(messageDto.getMessage())
                                .setType("USER")
                                .setModel(modelName)
                                .setIsMcp(messageDto.getIsMcp())
                                .setIsRag(messageDto.getIsRag())
                                .setConversationId(messageDto.getChatId())
                                .setTimestamp(LocalDateTime.now()));
                    }
                    springAiChatMemoryMapper.insert(new SpringAiChatMemory()
                            .setContent(completeContent)
                            .setModel(modelName)
                            .setType("ASSISTANT")
                            .setMedia(domin+messageDto.getImageUrl())
                            .setIsMcp(messageDto.getIsMcp())
                            .setIsRag(messageDto.getIsRag())
                            .setConversationId(messageDto.getChatId())
                            .setTimestamp(LocalDateTime.now()));
                });

        return content;
    }

    /**
     * 无文件聊天
     *
     * @param messageDto messageDto
     * @param model      模型
     * @param modelName  模型名称
     * @return Flux<String>
     */
    public Flux<String> ChatWithoutFile(MessageDto messageDto, ChatModel model, String modelName, String role, ToolCallbackProvider toolCallbackProvider) {
        ChatClient chatClient;
        //是否开启MCP
        if (messageDto.getIsMcp()){
            //是否开启了RAG
            if (messageDto.getIsRag()){

                chatClient = ChatClient.builder(model)
                        .defaultSystem(role)
                        .defaultAdvisors(QuestionAnswerAdvisor.builder(vectorStore)
                                .searchRequest(SearchRequest.builder()
                                        .topK(5)//最多 5 个检索结果
                                        .similarityThreshold(0.6)//最小相似度匹配百分比
                                        .build()).build())
                        .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                        .defaultToolCallbacks(toolCallbackProvider)
                        .build();
            }else {
                chatClient = ChatClient.builder(model)
                        .defaultSystem(role)
                        .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                        .defaultToolCallbacks(toolCallbackProvider)
                        .build();
            }
        }else {
            if (messageDto.getIsRag()){
                chatClient = ChatClient.builder(model)
                        .defaultSystem(role)
                        .defaultAdvisors(QuestionAnswerAdvisor.builder(vectorStore)
                                .searchRequest(SearchRequest.builder()
                                        .topK(5)//最多 5 个检索结果
                                        .similarityThreshold(0.6)//最小相似度匹配百分比
                                        .build()).build())
                        .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                        .build();
            }else {
                chatClient = ChatClient.builder(model)
                        .defaultSystem(role)
                        .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                        .build();
            }
        }

        StringBuilder fullContent = new StringBuilder();
        Flux<String> content = chatClient.prompt()
                .user(messageDto.getMessage())
                .advisors(advisorSpec -> advisorSpec.param(CONVERSATION_ID, messageDto.getChatId()))
                .stream()
                .content()
                .doOnNext(fragment -> fullContent.append(fragment))
                .doOnComplete(() -> {
                    String completeContent = fullContent.toString();
                    springAiChatMemoryMapper.insert(new SpringAiChatMemory()
                            .setContent(messageDto.getMessage())
                            .setType("USER")
                            .setModel(modelName)
                            .setIsMcp(messageDto.getIsMcp())
                            .setIsRag(messageDto.getIsRag())
                            .setConversationId(messageDto.getChatId())
                            .setTimestamp(LocalDateTime.now()));
                    springAiChatMemoryMapper.insert(new SpringAiChatMemory()
                            .setContent(completeContent)
                            .setModel(modelName)
                            .setType("ASSISTANT")
                            .setIsMcp(messageDto.getIsMcp())
                            .setIsRag(messageDto.getIsRag())
                            .setConversationId(messageDto.getChatId())
                            .setTimestamp(LocalDateTime.now()));
                });

        return content;
    }

    /**
     * 文件聊天
     *
     * @param messageDto messageDto
     * @param model      模型
     * @param modelName  模型名称
     * @return Flux<String>
     */
    public Flux<String> ChatWithFile(MessageDto messageDto, ChatModel model, String modelName, String role) {
        MediaType mediaType = MediaType.valueOf(Objects.requireNonNull(messageDto.getFile().getContentType()));
        String type = mediaType.getType();
        boolean isImage = "image".equals(type);
        String originalFilename = messageDto.getFile().getOriginalFilename();
        String fileExtension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1);
        final String fileName = UUID.randomUUID() + "."+fileExtension;
        String url = domin+aliUploadUtils.uploadFile(messageDto.getFile(), "chat", fileName, isImage);
        Media media = new Media(mediaType, URI.create(url));


        OpenAiChatOptions openAiChatOptions = OpenAiChatOptions.builder()
                .model(modelName)
                .build();
        SystemMessage systemMessage = new SystemMessage(role);
        List<Message> messages = getMessages(messageDto.getChatId());
        messages.add(0,systemMessage);
        UserMessage userMessage = UserMessage.builder().media(media).text(messageDto.getMessage()).build();
        messages.add(userMessage);
        Flux<ChatResponse> chatResponseFlux =model.stream(new Prompt(messages, openAiChatOptions));
        Flux<String> map = chatResponseFlux.mapNotNull(chatResponse ->
                chatResponse.getResult()!=null?
                        chatResponse.getResult().getOutput().getText():null)
                .mapNotNull(text -> text!=null?text:"");
        map.doOnNext(chunk -> {
        }).reduce((s1, s2) -> s1 + s2).subscribe(
                result -> {
                    // 流式结束后，
                    springAiChatMemoryMapper.insert(new SpringAiChatMemory()
                            .setContent(messageDto.getMessage())
                            .setType("USER")
                            .setMedia(url)
                            .setIsMcp(messageDto.getIsMcp())
                            .setIsRag(messageDto.getIsRag())
                            .setModel(modelName)
                            .setConversationId(messageDto.getChatId())
                            .setTimestamp(LocalDateTime.now()));
                    springAiChatMemoryMapper.insert(new SpringAiChatMemory()
                            .setContent(result)
                            .setType("ASSISTANT")
                            .setIsMcp(messageDto.getIsMcp())
                            .setIsRag(messageDto.getIsRag())
                            .setModel(modelName)
                            .setConversationId(messageDto.getChatId())
                            .setTimestamp(LocalDateTime.now()));
                }
        );
        return map;

    }

    public List<Message> getMessages(String conversationId) {
        List<SpringAiChatMemory> springAiChatMemories = springAiChatMemoryMapper.selectList(new QueryWrapper<SpringAiChatMemory>().lambda()
                .eq(SpringAiChatMemory::getConversationId, conversationId)
                .orderByDesc(SpringAiChatMemory::getTimestamp)
                .last("LIMIT 14"));
        List<Message> messages = new ArrayList<>();
        Collections.reverse(springAiChatMemories);
        for (SpringAiChatMemory springAiChatMemory : springAiChatMemories){
            String type = springAiChatMemory.getType();
            switch (type){
                case "USER" -> messages.add(new UserMessage(springAiChatMemory.getContent()));
                case "ASSISTANT" -> messages.add(new AssistantMessage(springAiChatMemory.getContent()));
                case "SYSTEM" -> messages.add(new SystemMessage(springAiChatMemory.getContent()));
                default -> throw new IllegalArgumentException("Unknown message type:"+type);
            }
        }
        return messages;
    }



    /**
     * 创建/查询会话
     * @param userId
     * @return
     */
    @Override
    public ChatList initiate(Long userId) {
        if (Objects.equals(userId, UserUtils.getCurrentLoginId())){
            throw new RuntimeException("不能与自己聊天");
        }
        ChatList cl = chatListMapper.selectOne(new QueryWrapper<ChatList>().lambda()
                .eq(ChatList::getUser1Id, UserUtils.getCurrentLoginId())
                .eq(ChatList::getUser2Id, userId)
                .or()
                .eq(ChatList::getUser1Id, userId)
                .eq(ChatList::getUser2Id, UserUtils.getCurrentLoginId()));
        if (cl == null){
            ChatList chatList = new ChatList().setUser1Id(UserUtils.getCurrentLoginId())
                    .setUser2Id(userId)
                    .setCreatedTime(LocalDateTime.now())
                    .setUser1Message(0)
                    .setLastMessage("-")
                    .setLastTime(LocalDateTime.now())
                    .setUser2Message(1);
            chatListMapper.insert(chatList);
            return chatList;
        }else {
            return cl;
        }
    }

    /**
     * 发送消息
     * @param chatDto
     * @return
     */
    @Override
    public void message(ChatDto chatDto) {
        Chat chat = new Chat().setChatListId( chatDto.getChatListId())
                .setType( chatDto.getType())
                .setContent( chatDto.getContent())
                .setUserId(UserUtils.getCurrentLoginId())
                .setCreatedTime(LocalDateTime.now());
        chatMapper.insert(chat);
        ChatList chatList = chatListMapper.selectOne(new QueryWrapper<ChatList>().lambda()
                .eq(ChatList::getId, chatDto.getChatListId()));
        if (chat.getContent().startsWith("https://img-hepingan.oss")){
            chat.setContent("[图片]");
        }
        if (chat.getContent().length()>30){
            chat.setContent(chat.getContent().substring(0,30)+"...");
        }
        //判断发送消息的用户是user1还是user2
        if (chatList.getUser1Id().equals(UserUtils.getCurrentLoginId())){
            chatList.setUser2Message(chatList.getUser2Message()+1);
            chatList.setLastMessage(chat.getContent());
            chatList.setLastTime(LocalDateTime.now());
            chatListMapper.updateById(chatList);
        }else {
            chatList.setUser1Message(chatList.getUser1Message()+1);
            chatList.setLastMessage(chat.getContent());
            chatList.setLastTime(LocalDateTime.now());
            chatListMapper.updateById(chatList);
        }
    }

    /**
     * 获取消息列表
     * @param chatListId
     * @return
     */
    @Override
    public List<ChatVo> messageList(Long chatListId, Integer pageNum) {
        Page<Chat> page = new Page<>(pageNum, 15);
        List<Chat> chats = chatMapper.selectPage(page, new QueryWrapper<Chat>().lambda()
                .eq(Chat::getChatListId, chatListId)).getRecords();
        List<ChatVo> chatsVo = chats.stream().map(chat -> {
            ChatVo chatVo = BeanUtils.copyClassProperTies(chat, ChatVo.class);
            if (chat.getUserId().equals(UserUtils.getCurrentLoginId())){
                chatVo.setIsUser(true);
            }else {
                chatVo.setIsUser(false);
            }
            return chatVo;
        }).toList();
        ChatList chatList = chatListMapper.selectById(chatListId);
        if (chatList.getUser1Id().equals(UserUtils.getCurrentLoginId())){
            chatListMapper.updateById(chatList.setUser1Message(0));
        }else {
            chatListMapper.updateById(chatList.setUser2Message(0));
        }
        return chatsVo;
    }

    /**
     * 获取会话列表
     * @return
     */
    @Override
    public Page<ChatListVo> chatList(Integer pageNum) {
        Page<ChatList> chatLists = chatListMapper.selectPage(new Page<>(pageNum, 15),new QueryWrapper<ChatList>().lambda()
                .eq(ChatList::getUser1Id, UserUtils.getCurrentLoginId())
                .or()
                .eq(ChatList::getUser2Id, UserUtils.getCurrentLoginId()));
        Page<ChatListVo> chatListVos = BeanUtils.copyClassProperTies(chatLists,Page.class);
        List<ChatListVo> list = new ArrayList<>();
        for (ChatList chatList : chatLists.getRecords()){
            ChatListVo chatListVo = BeanUtils.copyClassProperTies(chatList, ChatListVo.class);
            if (chatList.getUser1Id().equals(UserUtils.getCurrentLoginId())){
                User user = userMapper.selectOne(new QueryWrapper<User>().lambda()
                        .eq(User::getUserId, chatList.getUser2Id()));
                chatListVo.setInitiateUserId(chatList.getUser1Id())
                        .setInitiateMessage(chatList.getUser1Message())
                        .setTargetUserName(user.getUserName())
                        .setTargetUserAvatar(user.getAvatar())
                        .setTargetUserId(chatList.getUser2Id())
                        .setTargetId(chatList.getUser2Id());
            }else if (chatList.getUser2Id().equals(UserUtils.getCurrentLoginId())){
                User user = userMapper.selectOne(new QueryWrapper<User>().lambda()
                        .eq(User::getUserId, chatList.getUser1Id()));
                chatListVo.setInitiateUserId(chatList.getUser2Id())
                        .setInitiateMessage(chatList.getUser2Message())
                        .setTargetUserName(user.getUserName())
                        .setTargetUserAvatar(user.getAvatar())
                        .setTargetUserId(chatList.getUser1Id())
                        .setTargetId(chatList.getUser1Id());
            }
            list.add(chatListVo);
        }
        chatListVos.setRecords(list);
        return chatListVos;
    }

    @Override
    public Page<Group> groupList(Integer pageNum) {
        Page<ProjectMember> p = new Page<>(pageNum, 12);
        List<Long> projectList = new ArrayList<>();
        if (pageNum == 1){
            projectList = projectMapper.selectList(new QueryWrapper<Project>().lambda()
                            .eq(Project::getUserId, UserUtils.getCurrentLoginId()))
                    .stream().map(Project::getProjectId).toList();
            p.setSize(12-projectList.size());
        }
        List<Long> list = projectMemberMapper.selectPage(p,new QueryWrapper<ProjectMember>().lambda()
                        .eq(ProjectMember::getUserId, UserUtils.getCurrentLoginId())).getRecords()
                .stream().map(ProjectMember::getProjectId).toList();
        projectList.addAll(list);
        for (Long projectId : projectList){
            groupMapper.selectOne(new QueryWrapper<Group>().lambda()
                    .eq(Group::getProjectId, projectId));
        }
        return null;
    }

}

