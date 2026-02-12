package com.cn.bdth.api.admin;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.cn.bdth.msg.Result;
import com.cn.bdth.entity.User;
import com.cn.bdth.mapper.UserMapper;

@RequiredArgsConstructor
@RequestMapping("/admin/user")
@RestController
@Slf4j
public class AdminUserController {

    private final UserMapper userMapper;

    @GetMapping("/page")
    public Result userPage(@RequestParam Integer pageNum, @RequestParam String keyword) {
        Page<User> p = new Page<>(pageNum,  10);
        if (keyword != null && !keyword.isEmpty()){
            return Result.data(userMapper.selectPage(p, new QueryWrapper<User>().lambda()
                    .like(User::getUserName, keyword)));
        }
        return Result.data(userMapper.selectPage(p, null));
    }

    @GetMapping("/count")
    public Result userCount() {
        return Result.data(userMapper.selectCount(null));
    }

}
