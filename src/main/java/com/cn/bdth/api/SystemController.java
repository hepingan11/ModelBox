package com.cn.bdth.api;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.cn.bdth.msg.Result;
import com.cn.bdth.dto.ImgResult;
import com.cn.bdth.dto.ReviceDto;
import com.cn.bdth.entity.Forum;
import com.cn.bdth.entity.ForumImage;
import com.cn.bdth.mapper.CarouselMapper;
import com.cn.bdth.mapper.ForumImageMapper;
import com.cn.bdth.mapper.ForumMapper;
import com.cn.bdth.utils.AliUploadUtils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

@RequestMapping("/system")
@RestController
@Slf4j
@RequiredArgsConstructor
public class SystemController {

    private final AliUploadUtils aliUploadUtils;
    private final CarouselMapper carouselMapper;
    private final ForumImageMapper forumImageMapper;
    private final ForumMapper forumMapper;

    /**
     * 纯上传图片
     * @param file
     * @return
     */
    @PostMapping(value = "/uploadImg")
    public Result uploadImg(@RequestBody MultipartFile file){
        String newFileName = generateRandomString();
        String url = aliUploadUtils.uploadFile(file, "cr", newFileName+".jpg", true);
        return Result.data(url);
    }

    @GetMapping("/deleteImg")
    public Result deleteImg(@RequestParam String url) {
        aliUploadUtils.deleteFile(url);
        return Result.ok();
    }

    /**
     * 获取轮播图列表
     * @return
     */
    @GetMapping("/carousel/list")
    public Result carouselList() {
        return Result.data(carouselMapper.selectList(null));
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


    /**
     * 接收微信推送消息
     * @param signature
     * @param timestamp
     * @param nonce
     * @param echostr
     * @param reviceDto
     * @return
     */
    @GetMapping("/wx/revice")
    public String revice(@RequestParam String signature, @RequestParam String timestamp, @RequestParam String nonce, @RequestParam String echostr, @RequestBody ReviceDto reviceDto) {
        String token = "hepingan";
        //字典排序
        Dictionary<String, String> dictionary = new Hashtable<>();
        dictionary.put("token", token);
        dictionary.put("timestamp", timestamp);
        dictionary.put("nonce", nonce);
        StringBuilder sortedParams = new StringBuilder();
        dictionary.keys().asIterator().forEachRemaining(key -> sortedParams.append(key).append(dictionary.get(key)));
        String combinedString = sortedParams.toString();
        String sha1Combined = sha1(combinedString);

        if (Objects.equals(sha1Combined, signature)) {
            //验证成功逻辑处理
            ImgResult result = reviceDto.getResult();
            ForumImage forumImage = forumImageMapper.selectOne(new QueryWrapper<ForumImage>().lambda()
                    .eq(ForumImage::getForumId, reviceDto.getTrace_id()));
            if (result.getSuggest().equals("pass")){
                forumImage.setIsPass(true);
                forumImageMapper.updateById(forumImage);
                List<ForumImage> forumImages = forumImageMapper.selectList(new QueryWrapper<ForumImage>().lambda()
                        .eq(ForumImage::getForumId, forumImage.getForumId()));
                boolean isPass = true;
                for (ForumImage fi : forumImages){
                    if (!fi.getIsPass()) {
                        isPass = false;
                        break;
                    }
                }
                if (isPass){
                    Forum forum = forumMapper.selectOne(new QueryWrapper<Forum>().lambda().eq(Forum::getId, forumImage.getForumId()));
                    forum.setStatus(0);
                }
            }
            return "success";
        } else {
            //验证失败逻辑处理
            return "error";
        }
    }

    /**
     * 使用SHA1算法计算字符串的签名
     * @param str 需要签名的字符串
     * @return 签名结果（十六进制字符串）
     */
    public static String sha1(String str) {
        if (str == null || str.length() == 0) {
            return null;
        }

        try {
            // 获取SHA1算法实例
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            // 更新要计算的数据
            md.update(str.getBytes());
            // 计算哈希值
            byte[] digest = md.digest();
            // 将字节数组转换为十六进制字符串
            return bytesToHex(digest);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 将字节数组转换为十六进制字符串
     * @param bytes 字节数组
     * @return 十六进制字符串
     */
    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(b & 0xFF);
            if (hex.length() == 1) {
                sb.append('0');
            }
            sb.append(hex);
        }
        return sb.toString();
    }
}
