package com.cn.bdth.service;

import com.cn.bdth.vo.NewsVo;

import java.util.List;

public interface PassService {
    List<NewsVo> getNews(Integer page);

    String login(String account, String password);
}
