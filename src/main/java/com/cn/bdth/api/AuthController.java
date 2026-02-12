package com.cn.bdth.api;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cn.bdth.constants.user.AuthConstant;
import com.cn.bdth.dto.EmailCodeDto;
import com.cn.bdth.dto.EmailLoginDto;
import com.cn.bdth.dto.WechatLoginDto;
import com.cn.bdth.entity.User;
import com.cn.bdth.exceptions.EmailBackException;
import com.cn.bdth.exceptions.LoginPasswordException;
import com.cn.bdth.exceptions.RegistrationException;
import com.cn.bdth.msg.Result;
import com.cn.bdth.service.AuthService;
import com.cn.bdth.utils.WechatUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * 登录授权接口
 *
 * @author  @github dulaiduwang003
 * @version 1.0
 */
@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {


    private final AuthService authService;

    /**
     * 获取注册验证码
     *
     * @param email 邮箱
     * @return the result
     */
    @GetMapping(value = "/email/code/{email}", name = "网页获取邮箱码", produces = MediaType.APPLICATION_JSON_VALUE)
    public Result getEmailLoginCode(@PathVariable final String email) {
        try {
            authService.getEmailEnrollCode(email);
            return Result.ok();
        } catch (RegistrationException e) {
            return Result.error(e.getMessage());
        }
    }


    /**
     * 找回密码
     *
     * @param dto 邮箱
     * @return the result
     */
    @PostMapping(value = "/email/password/back", name = "找回密码", produces = MediaType.APPLICATION_JSON_VALUE)
    public Result RetrieveEmailPassword(@RequestBody @Validated final EmailCodeDto dto) {
        try {
            authService.emailBack(dto);
            return Result.ok();
        } catch (EmailBackException e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 注册账号
     *
     * @param dto dto
     * @return the result
     */
    @PostMapping(value = "/email/enroll", name = "注册账号", produces = MediaType.APPLICATION_JSON_VALUE)
    public Result emailEnroll(@RequestBody @Validated final EmailCodeDto dto) {
        try {
            authService.emailEnroll(dto);
            return Result.ok();
        } catch (RegistrationException e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 登陆账号
     *
     * @param dto dto
     * @return the result
     */
    @PostMapping(value = "/email/login", name = "登录", produces = MediaType.APPLICATION_JSON_VALUE)
    public Result emailLogin(@RequestBody @Validated final EmailLoginDto dto) {
        try {
            return Result.data(authService.emailLogin(dto));
        } catch (LoginPasswordException e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 退出登录
     *
     * @return the result
     */
    @PostMapping(value = "/wechat/logout", name = "退出登录", produces = MediaType.APPLICATION_JSON_VALUE)
    public Result wechatLogout() {
        try {
            authService.logout();
            return Result.ok();
        } catch (LoginPasswordException e) {
            return Result.error(e.getMessage(), e.getCode());
        }
    }


    /**
     * 微信小程序登录
     * @return 登录结果
     */
    @PostMapping("/wechat/login")
    public Result wxLogin(@RequestBody WechatLoginDto wechatLoginDto) {
        try {
            User user = authService.wechatLogin(wechatLoginDto);
            StpUtil.login(user.getUserId());
            //设置具体TOKEN Session权限
            StpUtil.getSession()
                    .set(AuthConstant.ROLE, user.getType());
            return Result.data(StpUtil.getTokenInfo());
        }catch (LoginPasswordException e){
            return Result.error(e.getMessage(), e.getCode());
        }
    }




}
