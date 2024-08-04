package com.cn.bdth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cn.bdth.entity.Photo;
import com.cn.bdth.enums.FileEnum;
import com.cn.bdth.mapper.PhotoMapper;
import com.cn.bdth.service.PhotoService;
import com.cn.bdth.utils.AliUploadUtils;
import com.cn.bdth.utils.UserUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PhotoServiceImpl implements PhotoService {

    private final PhotoMapper photoMapper;

    private final AliUploadUtils aliUploadUtils;

    @Override
    public void upload(List<MultipartFile> file) {
        for (MultipartFile item : file) {
            final Long currentLoginId = UserUtils.getCurrentLoginId();
            final String fileName = UUID.randomUUID() + ".jpg";
            String url=aliUploadUtils.uploadFile(item, FileEnum.LINK.getDec(),fileName,true);
            LocalDateTime time = LocalDateTime.now();
            Long year = (long) time.getYear();
            Long month = (long) time.getMonthValue();
            Long day = (long) time.getDayOfMonth();
            Photo photo = new Photo().setName(fileName)
                    .setUrl(url)
                    .setUserId(currentLoginId)
                    .setYear(year)
                    .setMonth(month)
                    .setDay(day)
                    .setCreatedTime(time);
            photoMapper.insert(photo);
        }

    }

    @Override
    public IPage<Photo> getPhoto(Long num) {
        return photoMapper.selectPage(new Page<>(num, 30), new QueryWrapper<Photo>()
                .lambda()
                .select(Photo::getUrl, Photo::getUserId,Photo::getName, Photo::getCreatedTime,Photo::getPhotoId)
        ).convert(u -> {
            final Photo photo = new Photo()
                            .setUrl(u.getUrl())
                            .setPhotoId(u.getPhotoId())
                            .setName(u.getName())
                    .setUserId(u.getUserId())
                            .setCreatedTime(u.getCreatedTime());
            return photo;
        });
    }



    //...{
}
