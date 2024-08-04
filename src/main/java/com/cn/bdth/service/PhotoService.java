package com.cn.bdth.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.cn.bdth.entity.Photo;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface PhotoService {
    void upload(List<MultipartFile> file);

    IPage<Photo> getPhoto(Long num);
}
