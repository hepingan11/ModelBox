package com.cn.bdth.api.admin;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import com.cn.bdth.msg.Result;
import com.cn.bdth.dto.FrameLockDto;
import com.cn.bdth.entity.Frame;
import com.cn.bdth.entity.FrameUnlock;
import com.cn.bdth.mapper.FrameMapper;
import com.cn.bdth.mapper.FrameUnlockMapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>头像框管理</p>
 */
@RequiredArgsConstructor
@RequestMapping("/admin/frame")
@RestController
@Slf4j
public class AdminFrameController {

    private final FrameMapper frameMapper;

    private final FrameUnlockMapper frameUnlockMapper;

    @GetMapping("/list")
    public Result avatarList(@RequestParam Integer pageNum, @RequestParam String name) {
        Page<Frame> p = new Page<>(pageNum,  20);
        if (name != null && !name.isEmpty()){
            return Result.data(frameMapper.selectPage(p,new QueryWrapper<Frame>().lambda()
                    .like(Frame::getName, name)).getRecords());
        }else return Result.data(frameMapper.selectPage(p,null).getRecords());
    }

    @GetMapping("/listAll")
    public Result frameListAll() {
        return Result.data(frameMapper.selectList(null));
    }

    @PostMapping("/add")
    public Result addFrame(@RequestBody Frame frame) {
        frame.setCreatedTime(LocalDateTime.now());
        return frameMapper.insert(frame) > 0 ? Result.ok() : Result.error();
    }

    @PostMapping("/unlock")
    public Result unlockFrame(@RequestBody FrameLockDto frameLockDto) {
        FrameUnlock frameUnlock = new FrameUnlock()
                .setFrameId(frameLockDto.getId())
                .setUserId(frameLockDto.getUserId())
                .setCreatedTime(LocalDateTime.now());
        return frameUnlockMapper.insert(frameUnlock) > 0 ? Result.ok() : Result.error();
    }

    @PostMapping("/lock")
    public Result lockFrame(@RequestBody FrameLockDto frameLockDto) {
        return frameUnlockMapper.delete(new QueryWrapper<FrameUnlock>().lambda()
                .eq(FrameUnlock::getUserId, frameLockDto.getUserId())
                .eq(FrameUnlock::getFrameId, frameLockDto.getId())) > 0 ? Result.ok() : Result.error();
    }

    @GetMapping("/lockList")
    public Result lockList(@RequestParam Long userId) {
        List<FrameUnlock> frameUnlocks = frameUnlockMapper.selectList(new QueryWrapper<FrameUnlock>().lambda()
                .eq(FrameUnlock::getUserId, userId));
        List<Long> frameIds = frameUnlocks.stream().map(FrameUnlock::getFrameId).collect(Collectors.toList());
        return Result.data(frameIds);
    }

    @GetMapping("/detail")
    public Result frameDetail(@RequestParam Long id) {
        Frame frame = frameMapper.selectById(id);
        return Result.data(frame);
    }

    @PostMapping("/update")
    public Result updateFrame(@RequestBody Frame frame) {
        return frameMapper.updateById(frame) > 0 ? Result.ok() : Result.error();
    }
}
