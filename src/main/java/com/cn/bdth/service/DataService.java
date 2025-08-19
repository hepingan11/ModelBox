package com.cn.bdth.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.cn.bdth.entity.Dialogue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public interface DataService {

    List<Map<String,Integer>> timeFrequency(Integer type);

    List<Map<String, Object>> getWordFrequency(int v);

    List<Map<String,Integer>> getModelFrequency();
}
