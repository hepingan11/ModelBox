package com.cn.bdth.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.cn.bdth.entity.Dialogue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public interface DataService {
    List<Dialogue> getDialogueData();

    List<Dialogue> getDialogueDataPage(Integer no, Integer size);

    List<Dialogue> timeFrequency();

    List<Map<String, Object>> getWordFrequency(int v);
}
