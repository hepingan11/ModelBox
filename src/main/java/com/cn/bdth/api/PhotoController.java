package com.cn.bdth.api;

import com.cn.bdth.msg.Result;
import com.cn.bdth.service.PhotoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/photo")
@RequiredArgsConstructor
public class PhotoController {

    private final PhotoService photoService;

    @PostMapping(value = "/upload",name = "上传",produces= MediaType.APPLICATION_JSON_VALUE)
    public Result upload(@RequestBody List<MultipartFile> file){
        try{
            photoService.upload(file);
            return Result.ok();
        }catch (Exception e){
            return Result.error(e.getMessage());
        }
    }

    @GetMapping(value = "/get",name = "获取图片",produces= MediaType.APPLICATION_JSON_VALUE)
    public Result getPhoto(@RequestParam Long num){
        try{
            return Result.data(photoService.getPhoto(num));
        }catch (Exception e){
            return Result.error(e.getMessage());
        }
    }

}
