package com.cn.bdth.service.impl;

import cn.dev33.satoken.exception.NotLoginException;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.cn.bdth.entity.*;
import com.cn.bdth.mapper.*;
import com.cn.bdth.service.ForumService;
import com.cn.bdth.utils.BeanUtils;
import com.cn.bdth.utils.UserUtils;
import com.cn.bdth.utils.WechatUtil;
import com.cn.bdth.vo.CommentVo;
import com.cn.bdth.vo.ForumMessageVo;
import com.cn.bdth.vo.ForumVo;

import java.io.IOException;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service
@RequiredArgsConstructor
public class ForumServiceImpl extends ServiceImpl<ForumMapper, Forum> implements ForumService {

    private final ForumMapper forumMapper;

    private final ForumImageMapper forumImageMapper;

    private final UserMapper userMapper;
    private final CommentMapper commentMapper;
    private final CommentLikeMapper commentLikeMapper;
    private final ForumLikeMapper forumLikeMapper;
    private final WechatUtil wechatUtil;

    /**
     * 分页查询
     * @param pageNum
     * @param title
     * @param userId
     * @return
     */
    @Override
    public List<ForumVo> pageBynum(Integer pageNum, String title, Long userId) {
        QueryWrapper<Forum> queryWrapper = new QueryWrapper<>();
        if (title != null && !title.equals("")){
            queryWrapper.lambda().like(Forum::getTitle, title);
        }
        if (userId != null){
            queryWrapper.lambda().eq(Forum::getUserId, userId);
        }
        int pageSize = 10;
        IPage<Forum> page = forumMapper.selectPage(new Page<>(pageNum, pageSize, true), queryWrapper.lambda()
                        .eq(Forum::getStatus, 0)
                .orderByDesc(Forum::getCreatedTime));
        List<ForumVo> forumList = new ArrayList<>();
        for (Forum forum : page.getRecords()) {
            List<ForumImage> ForumImages = forumImageMapper.selectList(new QueryWrapper<ForumImage>().lambda()
                    .eq(ForumImage::getForumId, forum.getId())
                    .select(ForumImage::getImageUrl));
            List<String> imageList = new ArrayList<>();
            for (ForumImage ForumImage : ForumImages) {
                imageList.add(ForumImage.getImageUrl());
            }
            ForumVo forumVo = BeanUtils.copyClassProperTies(forum, ForumVo.class);
            User user = userMapper.selectOne(new QueryWrapper<User>().lambda().eq(User::getUserId, forum.getUserId()));
            forumVo.setUsername(user.getUserName());
            forumVo.setLevel(user.getLevel());
            forumVo.setAvatar(user.getAvatar());
            forumVo.setAvatarFrameUrl(user.getAvatarFrameUrl());
            forumVo.setImageUrl(imageList);
//            ForumLike forumLike = forumLikeMapper.selectOne(new QueryWrapper<ForumLike>().lambda()
//                    .eq(ForumLike::getForumId, forum.getId())
//                    .eq(ForumLike::getUserId, UserUtils.getCurrentLoginId()));
//            if (forumLike != null){
//                forumVo.setIsLiked(true);
//            }else forumVo.setIsLiked(false);

            forumList.add(forumVo);
        }

        return forumList;
    }

    @Value("${forum-check}")
    private Boolean forumCheck;

    /**
     * 发布帖子
     * @param forumVo
     */
    @Override
    public void publish(ForumVo forumVo) {
        Forum forum = BeanUtils.copyClassProperTies(forumVo, Forum.class);
        User user = userMapper.selectOne(new QueryWrapper<User>().lambda()
                .eq(User::getUserId, UserUtils.getCurrentLoginId()));
        String openId = user.getOpenId();
        forum.setLook(0);
        forum.setLikes(0);
        forum.setUserId(UserUtils.getCurrentLoginId());
        forum.setCreatedTime(LocalDateTime.now());
        // 敏感信息检测
        if(forumCheck){
            String s = wechatUtil.textCheck(forum.getContent(), 3, openId == null ? "o8gms7WvEeMMP57_AZ9aKpQUXhu0" : openId);
            if (!s.equals("100")){
                forum.setStatus(1);
                throw new RuntimeException("内容存在敏感信息,请修改");
            }
            //图片为空自动通过
            if (forumVo.getImageUrl() == null){
                forum.setStatus(0);
            }else forum.setStatus(1);
            forumMapper.insert(forum);
            for (String url : forumVo.getImageUrl()){
                String traceId = wechatUtil.mediaCheck(url, 3, openId == null ? "o8gms7WvEeMMP57_AZ9aKpQUXhu0" : openId);
                forumImageMapper.insert(new ForumImage()
                        .setForumId(forum.getId())
                        .setTraceId(traceId)
                        .setImageUrl(url)
                        .setIsPass(false));
            }
            throw new RuntimeException("发布成功,请等待审核");
        }else {
            forum.setStatus(0);
            forumMapper.insert(forum);
            for (String url : forumVo.getImageUrl()){
                ForumImage forumImage = new ForumImage();
                forumImage.setForumId(forum.getId());
                forumImage.setImageUrl(url);
                forumImageMapper.insert(forumImage);
            }
        }
    }

    /**
     * 获取帖子评论
     * @param id
     * @return
     */
    @Override
    public List<CommentVo> getComment(Long id) {
        QueryWrapper<Comment> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(Comment::getForumId, id);
        List<Comment> comments = commentMapper.selectList(queryWrapper);
        List<CommentVo> commentVos = new ArrayList<>();
        for (Comment comment : comments){
            CommentVo commentVo = BeanUtils.copyClassProperTies(comment, CommentVo.class);
            try {
                Long currentLoginId = UserUtils.getCurrentLoginId();
                CommentLike commentLike = commentLikeMapper.selectOne(new QueryWrapper<CommentLike>().lambda()
                        .eq(CommentLike::getCommentId, comment.getId())
                        .eq(CommentLike::getUserId, UserUtils.getCurrentLoginId()));
                if (commentLike != null){
                    commentVo.setIsLiked(true);
                }else commentVo.setIsLiked(false);
            }catch (NotLoginException e){
                commentVo.setIsLiked(false);
            }

            User user = userMapper.selectOne(new QueryWrapper<User>().lambda().eq(User::getUserId, comment.getUserId()));
            commentVo.setUsername(user.getUserName());
            commentVo.setAvatar(user.getAvatar());
            commentVo.setAvatarFrameUrl(user.getAvatarFrameUrl());
            commentVo.setLevel(user.getLevel());
            commentVos.add(commentVo);
        }
        return commentVos;
    }

    @Value("${comment-check}")
    private Boolean commentCheck;

    /**
     * 评论
     * @param comment
     */
    @Override
    public String comment(Comment comment) {
        if (commentCheck){
            //一代不知名内容检查api
//            Map<String , String> params = new HashMap<>();
//            params.put("text", comment.getContent());
//            String s = queryAPI(params);
//            if (!s.equals("ok")){
//                return "评论失败:"+s;
//            }
            //二代微信官方内容检测api
            String s = wechatUtil.textCheck(comment.getContent(), 4, "o8gms7WvEeMMP57_AZ9aKpQUXhu0");
            if (!s.equals("100")){
                throw new RuntimeException("内容存在敏感信息,请修改");
            }
        }
        comment.setUserId(UserUtils.getCurrentLoginId());
        comment.setLikes(0);
        comment.setCreatedTime(LocalDateTime.now());
        commentMapper.insert(comment);
        return "评论成功";
    }

    private static String APIUrl = "https://openapi.chinaz.net/v1/1001/violate";
    private static String APIKey = "";
    private static String ChinazVer = "1.0";

    /**
     * 根据传进来的文本查询违禁词，返回分别高风险、中风险、低风险违禁词。
     * @param params
     */
    public static String queryAPI(Map<String, String> params) {
        params.put("APIKey", APIKey);
        params.put("ChinazVer", ChinazVer);
        Map<String, Object> response = doPost(APIUrl, params);
        try {
            Object statusCode = response.get("StatusCode");
            if (!statusCode.toString().equals("200")) {
                return "违禁词检测服务失败,请联系管理员";
            }
            String  resultData = response.get("ResultData").toString();
            // 解析JSON数据
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(resultData);

            // 获取Result字段（数组类型）
            String  resultArray = String.valueOf(jsonNode.get("Result"));
            boolean isResultEmpty = resultArray.equals("[]");
            if (!isResultEmpty) {
                String s = checkLevelNameContainsMediumRisk(resultArray);
                return s;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "ok";
    }

    public static String checkLevelNameContainsMediumRisk(String jsonData) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(jsonData);

            // 检查根节点是否为数组
            if (rootNode.isArray()) {
                // 遍历数组中的每个元素
                for (JsonNode element : rootNode) {
                    // 检查当前元素是否包含levelName字段
                    if (element.has("levelName") && element.get("levelName").isTextual()) {
                        String levelName = element.get("levelName").asText();
                        // 判断levelName是否包含"中风险"
                        if (levelName.contains("中风险")) {
                            return element.get("item").toString();
                        }
                        if (levelName.contains("高风险")){
                            return element.get("item").toString();
                        }
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("解析JSON时发生错误: " + e.getMessage());
        }
        return null;
    }



    /**
     * post方式的http请求
     * @param url 请求地址
     * @return 返回结果
     */
    public static Map<String, Object> doPost(String url, Map<String, String> param) {
        Map<String, Object> returnMap = new HashMap<>();
        // 创建Httpclient对象
        CloseableHttpClient httpclient = HttpClients.createDefault();
        CloseableHttpResponse response = null;
        try {
            // 创建uri
            URIBuilder builder = new URIBuilder(url);
            if (param != null) {
                for (String key : param.keySet()) {
                    builder.addParameter(key, param.get(key));
                }
            }
            URI uri = builder.build();
            // 创建http POST请求
            HttpPost httpPost = new HttpPost(uri);
            List<NameValuePair> pairs = new ArrayList<NameValuePair>();
            for (Map.Entry<String, String> entry : param.entrySet()) {
                pairs.add(new BasicNameValuePair(entry.getKey(), entry.getValue().toString()));
            }
            httpPost.setEntity(new UrlEncodedFormEntity(pairs, "UTF-8"));
            RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(60000).setConnectionRequestTimeout(60000).setSocketTimeout(60000).build();
            httpPost.setConfig(requestConfig);
            // 执行请求
            response = httpclient.execute(httpPost);
            returnMap.put("StatusCode", response.getStatusLine().getStatusCode());
            returnMap.put("ResultData", EntityUtils.toString(response.getEntity(), "UTF-8"));
        } catch (Exception e) {
            returnMap.put("StatusCode", "500");
            returnMap.put("ResultData", e.toString());
        } finally {
            try {
                if (response != null) {
                    response.close();
                }
                httpclient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return returnMap;
    }

    /**
     * 评论点赞
     * @param id
     */
    @Override
    public void commentLike(Long id) {
        CommentLike commentLike = commentLikeMapper.selectOne(new QueryWrapper<CommentLike>().lambda().eq(CommentLike::getCommentId, id).eq(CommentLike::getUserId, UserUtils.getCurrentLoginId()));
        if (commentLike == null){
            CommentLike commentLike1 = new CommentLike();
            commentLike1.setCommentId(id);
            commentLike1.setUserId(UserUtils.getCurrentLoginId());
            commentLike1.setCreatedTime(LocalDateTime.now());
            commentLikeMapper.insert(commentLike1);
            Comment comment = commentMapper.selectOne(new QueryWrapper<Comment>().lambda().eq(Comment::getId, id));
            comment.setLikes(comment.getLikes() + 1);
            commentMapper.updateById(comment);
        }else {
            commentLikeMapper.delete(new QueryWrapper<CommentLike>().lambda().eq(CommentLike::getCommentId,id).eq(CommentLike::getUserId,UserUtils.getCurrentLoginId()));
            Comment comment = commentMapper.selectOne(new QueryWrapper<Comment>().lambda().eq(Comment::getId, id));
            comment.setLikes(comment.getLikes() - 1);
            commentMapper.updateById(comment);
        }
    }

    /**
     * 获取我点赞的帖子
     * @param pageNum
     * @return
     */
    @Override
    public List<ForumVo> pageMyLike(Integer pageNum) {
        Page<ForumLike> p = new Page<>(pageNum,10);
        List<ForumLike> records = forumLikeMapper.selectPage(p, new QueryWrapper<ForumLike>().lambda()
                .eq(ForumLike::getUserId, UserUtils.getCurrentLoginId())
                .orderByDesc(ForumLike::getCreatedTime)).getRecords();
        List<ForumVo> forumVos = new ArrayList<>();
        for(ForumLike forumLike : records) {
            Forum forum = forumMapper.selectOne(new QueryWrapper<Forum>().lambda()
                    .eq(Forum::getId, forumLike.getForumId()));
            ForumVo forumVo = BeanUtils.copyClassProperTies(forum, ForumVo.class);
            List<ForumImage> ForumImages = forumImageMapper.selectList(new QueryWrapper<ForumImage>().lambda()
                    .eq(ForumImage::getForumId, forum.getId())
                    .select(ForumImage::getImageUrl));
            List<String> imageList = new ArrayList<>();
            for (ForumImage ForumImage : ForumImages) {
                imageList.add(ForumImage.getImageUrl());
            }
            User user = userMapper.selectOne(new QueryWrapper<User>().lambda()
                    .eq(User::getUserId, forum.getUserId()));
            forumVo.setUsername(user.getUserName());
            forumVo.setLevel(user.getLevel());
            forumVo.setAvatar(user.getAvatar());
            forumVo.setAvatarFrameUrl(user.getAvatarFrameUrl());
            forumVo.setImageUrl(imageList);
            forumVo.setIsLiked(true);
            forumVos.add(forumVo);
        };
        return forumVos;
    }

    /**
     * 周热门
     * @return
     */
    @Override
    public List<ForumVo> weeklyHot() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime oneWeekAgo = now.minusDays(7);
        return getForumVos(oneWeekAgo);
    }

    /**
     * 月热门
     * @return
     */
    @Override
    public List<ForumVo> monthlyHot() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime oneMonthAgo = now.minusMonths(1);
        return getForumVos(oneMonthAgo);
    }

    /**
     * 总帖子
     * @param pageNum
     * @param title
     * @return
     */
    @Override
    public List<ForumVo> pageTotal(Integer pageNum, String title) {
        QueryWrapper<Forum> queryWrapper = new QueryWrapper<>();
        if (title != null && !title.equals("")){
            queryWrapper.lambda().like(Forum::getTitle, title);
        }
        int pageSize = 10;
        IPage<Forum> page = forumMapper.selectPage(new Page<>(pageNum, pageSize, true), queryWrapper.lambda()
                .orderByDesc(Forum::getCreatedTime));
        List<ForumVo> forumList = new ArrayList<>();
        for (Forum forum : page.getRecords()) {
            List<ForumImage> ForumImages = forumImageMapper.selectList(new QueryWrapper<ForumImage>().lambda()
                    .eq(ForumImage::getForumId, forum.getId())
                    .select(ForumImage::getImageUrl));
            List<String> imageList = new ArrayList<>();
            for (ForumImage ForumImage : ForumImages) {
                imageList.add(ForumImage.getImageUrl());
            }
            ForumVo forumVo = BeanUtils.copyClassProperTies(forum, ForumVo.class);
            User user = userMapper.selectOne(new QueryWrapper<User>().lambda().eq(User::getUserId, forum.getUserId()));
            forumVo.setUsername(user.getUserName());
            forumVo.setLevel(user.getLevel());
            forumVo.setAvatar(user.getAvatar());
            forumVo.setAvatarFrameUrl(user.getAvatarFrameUrl());
            forumVo.setImageUrl(imageList);
            ForumLike forumLike = forumLikeMapper.selectOne(new QueryWrapper<ForumLike>().lambda()
                    .eq(ForumLike::getForumId, forum.getId())
                    .eq(ForumLike::getUserId, UserUtils.getCurrentLoginId()));
            if (forumLike != null){
                forumVo.setIsLiked(true);
            }else forumVo.setIsLiked(false);

            forumList.add(forumVo);
        }

        return forumList;
    }

    /**
     * 获取我收到的点赞消息
     * @param pageNum
     * @return
     */
    @Override
    public Page<ForumMessageVo> messageLikes(Integer pageNum) {
        List<Long> forumIds = forumMapper.selectList(new QueryWrapper<Forum>().lambda()
                .eq(Forum::getUserId, UserUtils.getCurrentLoginId())).stream().map(Forum::getId).toList();
        Page<ForumLike> forumLikePage = forumLikeMapper.selectPage(new Page<>(pageNum, 15), new QueryWrapper<ForumLike>().lambda()
                .eq(ForumLike::getUserId, UserUtils.getCurrentLoginId())
                .in(ForumLike::getForumId, forumIds)
                .orderByDesc(ForumLike::getId));
        //TODO 评论点赞信息
        Page<ForumMessageVo> forumMessageVos = BeanUtils.copyClassProperTies(forumLikePage, Page.class);
        List<ForumMessageVo> list = new ArrayList<>();
        for (ForumLike forumLike : forumLikePage.getRecords()) {
            Forum forum = forumMapper.selectById(forumLike.getForumId());
            User user = userMapper.selectById(forumLike.getUserId());
            list.add(new ForumMessageVo()
                    .setForumId(forum.getId())
                    .setForumImg(forumImageMapper.selectList(new QueryWrapper<ForumImage>().lambda()
                            .eq(ForumImage::getForumId, forum.getId())).get(0).getImageUrl())
                    .setTitle(forum.getTitle())
                    .setUserId(forum.getUserId())
                    .setUsername(user.getUserName())
                    .setCreatedTime(forum.getCreatedTime())
                    .setAvatar(user.getAvatar()));
        }
        return forumMessageVos.setRecords(list);
    }

    /**
     * 获取用户评论消息
     * @param pageNum
     * @return
     */
    @Override
    public Page<ForumMessageVo> messageComments(Integer pageNum) {
        // 先获取用户发布的所有帖子ID
        List<Long> forumIds = forumMapper.selectList(new QueryWrapper<Forum>().lambda()
                        .eq(Forum::getUserId, UserUtils.getCurrentLoginId()))
                .stream()
                .map(Forum::getId)
                .toList();

        // 先获取用户自己的评论ID列表
        List<Long> userCommentIds = commentMapper.selectList(new QueryWrapper<Comment>().lambda()
                        .eq(Comment::getUserId, UserUtils.getCurrentLoginId()))
                .stream()
                .map(Comment::getId)
                .toList();

        // 如果用户没有发表过评论，则直接返回空页面
        if (userCommentIds.isEmpty()) {
            return new Page<ForumMessageVo>().setRecords(new ArrayList<>());
        }

        // 查询回复用户评论的其他评论
        Page<Comment> commentPage = commentMapper.selectPage(
                new Page<>(pageNum, 15),
                new QueryWrapper<Comment>().lambda()
                        .in(Comment::getForumId, forumIds)
                        .in(Comment::getParentId, userCommentIds)
                        .orderByDesc(Comment::getId)
        );
        Page<ForumMessageVo> forumMessageVos = BeanUtils.copyClassProperTies(commentPage, Page.class);
        List<ForumMessageVo> list = new ArrayList<>();
        for (Comment comment : commentPage.getRecords()) {
            Forum forum = forumMapper.selectById(comment.getForumId());
            User user = userMapper.selectById(comment.getUserId());
            list.add(new ForumMessageVo()
                    .setForumId(forum.getId())
                    .setContent(comment.getContent())
                    .setForumImg(forumImageMapper.selectList(new QueryWrapper<ForumImage>().lambda()
                            .eq(ForumImage::getForumId, forum.getId())).get(0).getImageUrl())
                    .setTitle(forum.getTitle())
                    .setUserId(forum.getUserId())
                    .setCreatedTime(comment.getCreatedTime())
                    .setUsername(user.getUserName())
                    .setAvatar(user.getAvatar()));
        }
        return forumMessageVos.setRecords(list);
    }

    /**
     * 获取帖子
     * @param oneMonthAgo
     * @return
     */
    private List<ForumVo> getForumVos(LocalDateTime oneMonthAgo) {
        List<Forum> forums = forumMapper.selectList(new QueryWrapper<Forum>().lambda()
                .ge(Forum::getCreatedTime, oneMonthAgo) // 筛选最近一个月内的帖子
                .orderByDesc(Forum::getLikes) // 按点赞数降序排列
                .last("LIMIT 10")); // 只取前10条数据

        List<ForumVo> forumVoList = new ArrayList<>();
        for (Forum forum : forums) {
            List<ForumImage> ForumImages = forumImageMapper.selectList(new QueryWrapper<ForumImage>().lambda()
                    .eq(ForumImage::getForumId, forum.getId())
                    .select(ForumImage::getImageUrl));
            List<String> imageList = new ArrayList<>();
            for (ForumImage ForumImage : ForumImages) {
                imageList.add(ForumImage.getImageUrl());
            }
            ForumVo forumVo = BeanUtils.copyClassProperTies(forum, ForumVo.class);
            User user = userMapper.selectOne(new QueryWrapper<User>().lambda().eq(User::getUserId, forum.getUserId()));
            forumVo.setUsername(user.getUserName());
            forumVo.setLevel(user.getLevel());
            forumVo.setAvatar(user.getAvatar());
            forumVo.setAvatarFrameUrl(user.getAvatarFrameUrl());
            forumVo.setImageUrl(imageList);
            ForumLike forumLike = forumLikeMapper.selectOne(new QueryWrapper<ForumLike>().lambda().eq(ForumLike::getForumId, forum.getId()).eq(ForumLike::getUserId, UserUtils.getCurrentLoginId()));
            if (forumLike != null) {
                forumVo.setIsLiked(true);
            } else forumVo.setIsLiked(false);

            forumVoList.add(forumVo);
        }

        return forumVoList;
    }
}
