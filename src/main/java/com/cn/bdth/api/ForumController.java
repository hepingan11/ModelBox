package com.cn.bdth.api;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import com.cn.bdth.msg.Result;
import com.cn.bdth.entity.*;
import com.cn.bdth.mapper.ForumImageMapper;
import com.cn.bdth.mapper.ForumLikeMapper;
import com.cn.bdth.mapper.UserMapper;
import com.cn.bdth.service.ForumService;
import com.cn.bdth.utils.BeanUtils;
import com.cn.bdth.utils.UserUtils;
import com.cn.bdth.vo.CommentVo;
import com.cn.bdth.vo.ForumMessageVo;
import com.cn.bdth.vo.ForumVo;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@RequestMapping("/forum")
@RestController
@Slf4j
@RequiredArgsConstructor
public class ForumController {

    private final ForumService forumService;

    private final UserMapper userMapper;

    private final ForumImageMapper forumImageMapper;

    private final ForumLikeMapper forumLikeMapper;

    /**
     * 根据标题分页查询帖子
     * @param pageNum
     * @param title
     * @return
     */
    @GetMapping("/list")
    public Result list(@RequestParam Integer pageNum,@RequestParam String title) {
        List<ForumVo> forumList = forumService.pageBynum(pageNum,title,null);
        return Result.data(forumList);
    }

    /**
     * 根据用户id分页查询帖子
     * @param pageNum
     * @param userId
     * @return
     */
    @GetMapping("/listByUserId")
    public Result listByUserId(@RequestParam Integer pageNum,@RequestParam Long userId) {
        List<ForumVo> forumList = forumService.pageBynum(pageNum,null,userId);
        return Result.data(forumList);
    }

    /**
     * 查询当前用户发布的帖子
     * @param pageNum
     * @return
     */
    @GetMapping("/listByMy")
    public Result listByMy(@RequestParam Integer pageNum) {
        List<ForumVo> forumList = forumService.pageBynum(pageNum,null,UserUtils.getCurrentLoginId());
        return Result.data(forumList);
    }

    /**
     * 发布帖子
     * @param forumVo
     * @return
     */
    @PostMapping(value = "/publish")
    public Result publish(@RequestBody ForumVo forumVo) {
        User user = userMapper.selectOne(new QueryWrapper<User>().lambda().eq(User::getUserId, UserUtils.getCurrentLoginId()));
//        if (user.getOpenId() == null){
//            return Result.error("请先绑定微信");
//        }
        try {
            forumService.publish(forumVo);
            return Result.ok("发布成功");
        }catch (Exception e){
            return Result.error("发布失败:"+e);
        }
    }

    /**
     * 根据帖子id查询评论
     * @param id
     * @return
     */
    @GetMapping("/commentById")
    public Result commentById(@RequestParam Long id) {
        List<CommentVo> comment = forumService.getComment(id);
        return Result.data(comment);
    }

    /**
     * 评论
     * @param comment
     * @return
     */
    @PostMapping(value = "/comment")
    public Result comment(@RequestBody Comment comment) {
        String result = forumService.comment(comment);
        if (Objects.equals(result, "评论成功")){
            return Result.ok();
        }else return Result.error(result);
    }

    /**
     * 根据id查询帖子
     * @param id
     * @return
     */
    @GetMapping("/getById")
    public Result getById(@RequestParam Long id) {
        Forum forum = forumService.getById(id);
        ForumVo forumVo = BeanUtils.copyClassProperTies(forum, ForumVo.class);
        List<ForumImage> forumImages = forumImageMapper.selectList(new QueryWrapper<ForumImage>().lambda().eq(ForumImage::getForumId, forum.getId()));
        List<String> imageList = forumImages.stream().map(ForumImage::getImageUrl).toList();

        User user = userMapper.selectOne(new QueryWrapper<User>().lambda().eq(User::getUserId, forum.getUserId()));
        forumVo.setUsername(user.getUserName());
        forumVo.setLevel(user.getLevel());
        forumVo.setImageUrl(imageList);
        forumVo.setAvatar(user.getAvatar());
        forumVo.setAvatarFrameUrl(user.getAvatarFrameUrl());
        //增加点击量
        forumVo.setLook(forum.getLook()+1);
        forum.setLook(forumVo.getLook());
        try {
            Long currentLoginId = UserUtils.getCurrentLoginId();
            ForumLike forumLike = forumLikeMapper.selectOne(new QueryWrapper<ForumLike>().lambda()
                    .eq(ForumLike::getForumId, forum.getId())
                    .eq(ForumLike::getUserId, currentLoginId));
            forumVo.setIsLiked(forumLike != null);
        }catch (Exception e){
            forumVo.setIsLiked(false);
        }
        forumService.updateById(forum);
        return Result.data(forumVo);
    }

    /**
     * 评论点赞或取消点赞
     * @param id
     * @return
     */
    @PostMapping(value = "/commentLike")
    public Result commentLike(@RequestBody Long id) {
        try {
            forumService.commentLike(id);
            return Result.ok("点赞成功");
        }catch (Exception e){
            return Result.error("点赞失败");
        }
    }

    /**
     * 帖子点赞或取消点赞
     * @param id
     * @return
     */
    @PostMapping(value = "/like")
    public Result forumLike(@RequestBody Long id) {
        try {
            ForumLike forumLike = forumLikeMapper.selectOne(new QueryWrapper<ForumLike>().lambda()
                    .eq(ForumLike::getForumId, id)
                    .eq(ForumLike::getUserId, UserUtils.getCurrentLoginId()));
            if (forumLike == null){
                ForumLike forumLike1 = new ForumLike();
                forumLike1.setForumId(id);
                forumLike1.setUserId(UserUtils.getCurrentLoginId());
                forumLike1.setCreatedTime(LocalDateTime.now());
                forumLikeMapper.insert(forumLike1);
                Forum forum = forumService.getById(id);
                 forum.setLikes(forum.getLikes() + 1);
                 forumService.updateById(forum);
            }else {
                forumLikeMapper.deleteById(forumLike);
                Forum forum = forumService.getById(id);
                forum.setLikes(forum.getLikes() - 1);
                forumService.updateById(forum);
            }
            return Result.ok();
        }catch (Exception e){
            return Result.error("点赞失败");
        }
    }


    @GetMapping("/deleteById")
    public Result deleteById(@RequestParam Long id) {
        try {
            forumService.removeById(id);
            return Result.ok("删除成功");
        }catch (Exception e){
            return Result.error("删除失败");
        }
    }

    @GetMapping("/mylike")
    public Result mylike(@RequestParam Integer pageNum) {
        List<ForumVo> forumList = forumService.pageMyLike(pageNum);

        return Result.data(forumList);
    }

    @GetMapping("/weeklyHot")
    public Result weeklyHot() {
        List<ForumVo> forumList = forumService.weeklyHot();
        return Result.data(forumList);
    }

    @GetMapping("/monthlyHot")
    public Result monthlyHot() {
        List<ForumVo> forumList = forumService.monthlyHot();
        return Result.data(forumList);
    }

    @GetMapping("/message/likes")
    public Result messageLikes(@RequestParam Integer pageNum) {
        Page<ForumMessageVo> forumMessageVoPage = forumService.messageLikes(pageNum);
        return Result.data(forumMessageVoPage);
    }

    @GetMapping("/message/comments")
    public Result messageComments(@RequestParam Integer pageNum) {
        Page<ForumMessageVo> forumMessageVoPage = forumService.messageComments(pageNum);
        return Result.data(forumMessageVoPage);
    }


}
