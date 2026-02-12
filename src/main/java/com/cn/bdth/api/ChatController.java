package com.cn.bdth.api;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cn.bdth.dto.MessageDto;
import com.cn.bdth.dto.ChatDto;
import com.cn.bdth.entity.ChatList;
import com.cn.bdth.entity.ConversationUser;
import com.cn.bdth.entity.Group;
import com.cn.bdth.mapper.ConversationUserMapper;
import com.cn.bdth.msg.Result;
import com.cn.bdth.service.ChatService;
import com.cn.bdth.utils.UserUtils;
import com.cn.bdth.vo.TransitVo;
import com.cn.bdth.vo.ChatListVo;
import com.cn.bdth.vo.ChatVo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;

import java.net.MalformedURLException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/chat")
public class ChatController {

    private final ChatService chatService;

    private final ConversationUserMapper conversationUserMapper;

    /**
     * AI聊天
     * @return
     */
    @PostMapping(value = "/chat",produces = "text/html; charset=UTF-8")
    public Flux<String> chat(@RequestParam("message") String message,
                                  @RequestParam("chatId") String chatId,
                                  @RequestParam("model") String model,
                                  @RequestParam("isMcp") Boolean isMcp,
                                  @RequestParam("isRag") Boolean isRag,
                                  @RequestParam("mcpList") String  mcpList,
                                  @RequestPart(value = "file", required = false) MultipartFile file){
        MessageDto messageDto = new MessageDto()
                .setMessage(message)
                .setChatId(chatId)
                .setModel(model)
                .setIsMcp(isMcp)
                .setIsRag(isRag)
                .setMcpList(parseMcpList(mcpList))
                .setFile(file);
        try {
            return chatService.aiChat(messageDto);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    //中转
    @PostMapping(value = "/transit",produces = MediaType.APPLICATION_JSON_VALUE)
    public Result transit(@RequestBody MessageDto messageDto) {
        try {
            TransitVo transit = chatService.transit(messageDto);
            return Result.data(transit);
        }catch (Exception e){
            return Result.error("中转失败");
        }
    }


    @PostMapping(value = "/draw/callback",produces = "text/html; charset=UTF-8")
    public Flux<String> drawCallback(@RequestBody MessageDto messageDto){
        return chatService.drawCallback(messageDto);
    }

    /**
     * 无上下文AI聊天
     * @return
     */
    @PostMapping(value = "/chat2",produces = "text/html; charset=UTF-8")
    public Flux<String> chat2(@RequestParam("message") String message,
                             @RequestParam("chatId") String chatId,
                             @RequestParam("model") String model,
                             @RequestParam("isMcp") Boolean isMcp,
                             @RequestParam("isRag") Boolean isRag,
                             @RequestParam("mcpList") String  mcpList,
                             @RequestPart(value = "file", required = false) MultipartFile file){
        MessageDto messageDto = new MessageDto()
                .setMessage(message)
                .setChatId(chatId)
                .setModel(model)
                .setIsMcp(isMcp)
                .setIsRag(isRag)
                .setMcpList(parseMcpList(mcpList))
                .setFile(file);
        return chatService.aiChat2(messageDto);
    }

    private List<Long> parseMcpList(String mcpListStr) {
        if (mcpListStr == null || mcpListStr.isEmpty()) {
            return List.of();
        }
        // 移除方括号并按逗号分割
        String cleaned = mcpListStr.replaceAll("[\\[\\]]", "");
        if (cleaned.isEmpty()) {
            return List.of();
        }
        return Arrays.stream(cleaned.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(Long::valueOf)
                .collect(Collectors.toList());
    }


    /**
     * 获取聊天记录
     * @param chatId
     * @return
     */
    @GetMapping(value = "/history")
    public Result getHistory(@RequestParam String chatId,@RequestParam Integer pageNum){
        return Result.data(chatService.getHistory(chatId,pageNum));
    }

    /**
    删除聊天记录
     */
    @PostMapping(value = "/delete")
    public Result delete(@RequestBody String chatId){
        try {
            chatService.deleteHistory(chatId);
            return Result.ok();
        }catch (Exception e){
            return Result.error("删除失败");
        }
    }

    /**
     * 获取会话列表
     * @return
     */
    @GetMapping(value = "/conversation/list")
    public Result getConversationList(){
        return Result.data(chatService.conversationList());
    }

    /**
     * 添加会话
     * @param messageDto
     * @return
     */
    @PostMapping(value = "/conversation/add")
    public Result addConversation(@RequestBody MessageDto messageDto){
        try {
            conversationUserMapper.insert(new ConversationUser()
                    .setConversationId(messageDto.getChatId())
                    .setCreatedTime(LocalDateTime.now())
                    .setTitle(messageDto.getTitle())
                    .setUserId(UserUtils.getCurrentLoginId()));
            return Result.ok();
        }catch (Exception e){
            return Result.error("添加失败");
        }
    }


    /**
     * 修改会话
     * @param messageDto
     * @return
     */
    @PostMapping(value = "/conversation/update")
    public Result updateConversation(@RequestBody MessageDto messageDto){
        try {
            conversationUserMapper.update(new ConversationUser()
                            .setRole(messageDto.getRole())
                    .setTitle(messageDto.getTitle()),new QueryWrapper<ConversationUser>().lambda()
                    .eq(ConversationUser::getConversationId, messageDto.getChatId())
            );
            return Result.ok();
        }catch (Exception e){
            return Result.error("修改失败");
        }
    }

    /**
     * 上传 rag文件
     * @param file
     * @return
     */
    @PostMapping(value = "/rag/upload")
    public Result updateRag(@RequestParam("file") MultipartFile file){
        try {
            chatService.updateRag(file);
            return Result.ok();
        }catch (Exception e){
            return Result.error("修改失败");
        }
    }

    /**
     * rag列表
     * @return
     */
    @GetMapping(value = "/rag/list")
    public Result ragList(){
        return Result.data(chatService.ragList());
    }

    /**
     * 删除 rag
     * @param ragId
     * @return
     */
    @GetMapping(value = "/rag/delete/{ragId}")
    public Result deleteRag(@PathVariable Long ragId){
        try {
            chatService.deleteRag(ragId);
            return Result.ok();
        }catch (Exception e){
            return Result.error("删除失败");
        }
    }

    /**
     * 修改是否启用 rag
     * @param ragId
     * @return
     */
    @PostMapping(value = "/rag/toggle/{ragId}")
    public Result toggleRag(@PathVariable Long ragId){
        try {
            chatService.toggleRag(ragId);
            return Result.ok();
        }catch (Exception e){
            return Result.error("删除失败");
        }
    }

    /**
     * mcp列表
     * @return
     */
    @GetMapping(value = "/mcp/list")
    public Result mcpList(){
        return Result.data(chatService.mcpList());
    }


    //  发起聊天
    @GetMapping("/initiate")
    public Result initiate(Long userId) {
        try {
            ChatList initiate = chatService.initiate(userId);
            return Result.data(initiate);
        }catch (Exception e){
            return Result.error(e.getMessage());
        }
    }

    //  发送消息
    @PostMapping("/message")
    public Result message(@RequestBody ChatDto chatDto) {
        try {
            chatService.message(chatDto);
            return Result.ok();
        }catch (Exception e){
            return Result.error("发送失败");
        }
    }

    //  获取聊天记录
    @GetMapping("/message/list")
    public Result messageList(@RequestParam Long id,@RequestParam Integer pageNum) {
        try {
            List<ChatVo> chatVos = chatService.messageList(id, pageNum);
            return Result.data(chatVos);
        }catch (Exception e){
            return Result.error("获取失败");
        }
    }

    //获取会话列表
    @GetMapping("/list")
    public Result list(@RequestParam Integer pageNum) {
        try {
            Page<ChatListVo> chatListVos = chatService.chatList(pageNum);
            return Result.data(chatListVos);
        }catch (Exception e){
            return Result.error("获取失败");
        }
    }

    @GetMapping("/group/list")
    public Result groupList(@RequestParam Integer pageNum) {
        try {
            Page<Group> chatListVos = chatService.groupList(pageNum);
            return Result.data(chatListVos);
        }catch (Exception e){
            return Result.error("获取失败");
        }
    }

}
