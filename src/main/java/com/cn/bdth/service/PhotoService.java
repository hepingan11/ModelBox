package com.cn.bdth.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.cn.bdth.entity.Photo;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface PhotoService {
    String upload(List<MultipartFile> file);

    List<Photo> getPhoto();

    void deletePhoto(Long id);

    void setPhotoPublic(Long id);

    List<Photo> getPublicPhoto();
}
