package com.cn.bdth.api;


import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cn.bdth.dto.PersonalityDto;
import com.cn.bdth.dto.StarDialogueDto;
import com.cn.bdth.entity.Frame;
import com.cn.bdth.entity.FrameUnlock;
import com.cn.bdth.entity.User;
import com.cn.bdth.exceptions.UploadException;
import com.cn.bdth.mapper.FrameMapper;
import com.cn.bdth.mapper.FrameUnlockMapper;
import com.cn.bdth.mapper.UserMapper;
import com.cn.bdth.msg.Result;
import com.cn.bdth.service.GptService;
import com.cn.bdth.service.StarService;
import com.cn.bdth.service.UserService;
import com.cn.bdth.utils.AliUploadUtils;
import com.cn.bdth.utils.UserUtils;
import com.cn.bdth.utils.WechatUtil;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;


@Slf4j
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    private final StarService starService;

    private final GptService gptService;

    private final UserMapper userMapper;

    private final AliUploadUtils aliUploadUtils;



    /**
     * 获取用户个性GPT配置
     *
     * @return the result
     */
    @GetMapping(value = "/personality/get/config", name = "获取用户个性GPT配置", produces = MediaType.APPLICATION_JSON_VALUE)
    public Result getPersonalityGptConfig() {
        return Result.data(gptService.getPersonalityConfig(null));
    }

    /**
     * 设置用户个性GPT配置
     *
     * @return the result
     */
    @PostMapping(value = "/personality/put/config", name = "设置用户个性GPT配置", produces = MediaType.APPLICATION_JSON_VALUE)
    public Result putPersonalityGptConfig(@RequestBody @Validated PersonalityDto dto) {
        gptService.putPersonalityConfig(dto);
        return Result.ok();
    }

    /**
     * 当前用户信息结果。
     *
     * @return the result
     */
    @PostMapping(value = "/current/info", name = "用户当前用户信息", produces = MediaType.APPLICATION_JSON_VALUE)
    public Result currentUserInfo() {

        return Result.data(userService.getCurrentUserInfo());

    }


    /**
     * 用户更新头像
     *
     * @param avatar the avatar
     * @return the result
     */
    @PostMapping(value = "/upload/avatar", name = "用户更新头像", consumes = "multipart/form-data")
    public Result userUploadAvatar(@Valid @NotNull(message = "用户头像不能为空") final MultipartFile avatar) {
        try {
            userService.editUserAvatar(avatar);
            return Result.ok();
        } catch (UploadException e) {
            return Result.error(e.getMessage());
        }

    }

    /**
     * 修改用户昵称
     *
     * @param userName the username
     * @return the result
     */
    @PostMapping(value = "/upload/username", name = "用户更新用户名")
    public Result userUpdateName(@NotBlank(message = "用户昵称不能为空") @RequestParam String userName) {
        userService.editUserName(userName);
        return Result.ok();
    }


    /**
     * 分页获取收藏
     *
     * @param pageNum the page num
     * @return the result
     */
    @GetMapping(value = "/stat/page", name = "分页获取收藏", produces = MediaType.APPLICATION_JSON_VALUE)
    public Result getStarPage(@RequestParam(defaultValue = "1") final int pageNum) {
        return Result.data(starService.getUserStarVoPage(pageNum));
    }


    /**
     * 删除指定收藏
     *
     * @param id the id
     * @return the result
     */
    @PostMapping(value = "/stat/delete/{id}", name = "删除指定收藏", produces = MediaType.APPLICATION_JSON_VALUE)
    public Result deleteStarById(@PathVariable final Long id) {
        starService.deleteById(id);
        return Result.ok();
    }


    /**
     * 查看指定收藏
     *
     * @param starId the star id
     * @return the result
     */
    @GetMapping(value = "/stat/get/data", name = "查看指定收藏", produces = MediaType.APPLICATION_JSON_VALUE)
    public Result getStarDetailById(@RequestParam final Long starId) {

        return Result.data(starService.getUserStarDetail(starId));
    }


    /**
     * 添加收藏
     *
     * @param dto the dto
     * @return the result
     */
    @PostMapping(value = "/stat/put/data", name = "添加收藏", produces = MediaType.APPLICATION_JSON_VALUE)
    public Result putStarDialogue(@RequestBody @Validated final StarDialogueDto dto) {
        return Result.data(starService.starDialogue(dto));
    }


    /**
     * 获取我的作品管理
     *
     * @param pageNum the page num
     * @return the result
     */
    @GetMapping(value = "/drawing/page", name = "获取我的作品", produces = MediaType.APPLICATION_JSON_VALUE)
    public Result getUserOpsPage(@RequestParam(defaultValue = "1") final int pageNum) {
        return Result.data(null);
    }

    /**
     * 获取我的收藏(兼容WEB接口)
     *
     * @return the result
     */
    @GetMapping(value = "/stat/get/web", name = "获取我的收藏", produces = MediaType.APPLICATION_JSON_VALUE)
    public Result getUserStarWeb() {
        return Result.data(starService.getUserStarWeb());
    }


    @PostMapping("/updateUser")
    public Result updateUser(@RequestBody User user){
        try {
            userMapper.updateById(user.setUserId(UserUtils.getCurrentLoginId()));
            return Result.ok();
        }catch (Exception e){
            return Result.error("更新用户信息失败");
        }
    }

    @GetMapping("/getUserInfo")
    public Result getUserInfo(){
        User u = userMapper.selectById(UserUtils.getCurrentLoginId());
        return Result.data(u);
    }

    @GetMapping("/getUserInfoById")
    public Result getUserInfoById(Long userId){
        User u = userMapper.selectById(userId);
        return Result.data(u);
    }



    /**
     * 上传背景图
     * @param file
     * @return
     */
    @PostMapping("/updateBackgroundImage")
    public Result updateBackgroundImage(@RequestBody MultipartFile file){
        String newFileName = generateRandomString();
        String url = aliUploadUtils.uploadFile(file, "link", newFileName+".jpg", true);
        User user = userMapper.selectOne(new QueryWrapper<User>().lambda().eq(User::getUserId, UserUtils.getCurrentLoginId()));
        user.setBackgroundImage(url);
        userMapper.update(user, new QueryWrapper<User>().lambda().eq(User::getUserId, UserUtils.getCurrentLoginId()));
        return Result.data(url);
    }

    /**
     * 更新头像
     * @param file
     * @return
     */
    @PostMapping(value = "/uploadAvatar")
    public Result uploadImg(@RequestBody MultipartFile file){
        String newFileName = generateRandomString();
        String url = aliUploadUtils.uploadFile(file, "cr", newFileName+".jpg", true);
        User user = userMapper.selectOne(new QueryWrapper<User>().lambda().eq(User::getUserId, UserUtils.getCurrentLoginId()));
        aliUploadUtils.deleteFile(user.getAvatar());
        user.setAvatar(url);
        userMapper.update(user, new QueryWrapper<User>().lambda().eq(User::getUserId, UserUtils.getCurrentLoginId()));
        return Result.data(url);
    }

    private String generateRandomString() {
        String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        int STRING_LENGTH = 12;
        Random random = new Random();
        StringBuilder sb = new StringBuilder(STRING_LENGTH);
        for (int i = 0; i < STRING_LENGTH; i++) {
            int index = random.nextInt(CHARACTERS.length());
            sb.append(CHARACTERS.charAt(index));
        }
        return sb.toString();
    }

    private final FrameMapper frameMapper;

    private final FrameUnlockMapper frameUnlockMapper;
    @GetMapping("/frameList")
    public Result frameList(){
        List<Frame> frameList = frameMapper.selectList(new QueryWrapper<Frame>().lambda()
                .orderByAsc(Frame::getCreatedTime));
        return Result.data(frameList);
    }

    @GetMapping("/userFrameList")
    public Result UserFrameList(){
        List<FrameUnlock> frameUnlocks = frameUnlockMapper.selectList(new QueryWrapper<FrameUnlock>().lambda()
                .eq(FrameUnlock::getUserId, UserUtils.getCurrentLoginId()));
        List<Long> frameIds = frameUnlocks.stream().map(FrameUnlock::getFrameId).collect(Collectors.toList());
        return Result.data(frameIds);
    }

    @PostMapping("/updateFrame")
    public Result updateFrame(@RequestBody String url){
        User user = userMapper.selectOne(new QueryWrapper<User>().lambda().eq(User::getUserId, UserUtils.getCurrentLoginId()));
        user.setAvatarFrameUrl(url);
        userMapper.updateById(user);
        return Result.ok();
    }

    /**
     * 签到
     * @return
     */
    @GetMapping("/sign")
    public Result sign(){
        User user = userMapper.selectOne(new QueryWrapper<User>().lambda().eq(User::getUserId, UserUtils.getCurrentLoginId()));
        user.setExperience(user.getExperience()+5);
        checkLevel(user);
        userMapper.updateById(user);
        return Result.ok();
    }


    private void checkLevel(User user){
        if (user.getExperience() >= 100){
            user.setLevel(2);
        }
        if (user.getExperience() >= 300){
            user.setLevel(3);
        }
        if (user.getExperience() >= 500){
            user.setLevel(4);
        }
        if (user.getExperience() >= 1000){
            user.setLevel(5);
        }
        if (user.getExperience() >= 2000){
            user.setLevel(6);
        }
    }
}
