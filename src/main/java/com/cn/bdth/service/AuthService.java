package com.cn.bdth.service;

import com.cn.bdth.dto.EmailCodeDto;
import com.cn.bdth.dto.EmailContentDto;
import com.cn.bdth.dto.EmailLoginDto;
import com.cn.bdth.dto.WechatLoginDto;
import com.cn.bdth.entity.User;

/**
 * 登录接口
 *
 * @author 时间海 @github dulaiduwang003
 * @version 1.0
 */
public interface AuthService {


    /**
     * 邮箱注册
     */
    void emailEnroll(final EmailCodeDto dto);


    /**
     * 找回密码
     */
    void emailBack(final EmailCodeDto dto);


    /**
     * 获取邮箱二维码
     */
    void getEmailEnrollCode(final String email);

    /**
     * 邮箱登录
     */
    String emailLogin(final EmailLoginDto dto);


    /**
     * 注销
     */
    void logout();

    void submitEmailContent(EmailContentDto emailContentDto);

    /**
     * 微信登录
     */
    User wechatLogin(WechatLoginDto wechatLoginDto);
}
