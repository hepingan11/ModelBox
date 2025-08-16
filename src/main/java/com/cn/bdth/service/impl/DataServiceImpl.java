package com.cn.bdth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cn.bdth.constants.ServerConstant;
import com.cn.bdth.entity.Dialogue;
import com.cn.bdth.mapper.DialogueMapper;
import com.cn.bdth.service.DataService;
import com.cn.bdth.utils.RedisUtils;
import com.cn.bdth.utils.UserUtils;
import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.seg.common.Term;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
//import org.apache.hadoop.conf.Configuration;
//import org.apache.hadoop.fs.FileSystem;
//import org.apache.hadoop.fs.Path;
//import org.apache.hadoop.io.IOUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class DataServiceImpl implements DataService {

    private final DialogueMapper dialogueMapper;
    private final RedisUtils redisUtils;


    @Override
    public List<Dialogue> getDialogueData() {
        return dialogueMapper.selectList(new QueryWrapper<Dialogue>().lambda().eq(Dialogue::getUserId, UserUtils.getCurrentLoginId()));
    }

    @Override
    public List<Dialogue> getDialogueDataPage(Integer no, Integer size) {
        return dialogueMapper.selectList(new QueryWrapper<Dialogue>().lambda()
                .eq(Dialogue::getUserId, UserUtils.getCurrentLoginId()).orderByDesc(Dialogue::getCreatedTime));
    }

    @Override
    public List<Dialogue> timeFrequency() {
        return List.of();
    }

    @Override
    public List<Map<String, Object>> getWordFrequency(int v) {
        String text = "";
        //mysql
        String value = String.valueOf(redisUtils.getValue(ServerConstant.IS_HADOOP));
        if (value.equals("mysql")){
            List<Dialogue> dialogueList = dialogueMapper.selectList(new QueryWrapper<Dialogue>().lambda().eq(Dialogue::getUserId, UserUtils.getCurrentLoginId()));
            if (v==1){
                for (Dialogue dialogue : dialogueList){
                    StringBuilder sb = new StringBuilder(text);
                    sb.append(dialogue.getContent());
                    text = sb.toString();
                }
            }
            if (v == 2){
                for (Dialogue dialogue : dialogueList){
                    StringBuilder sb = new StringBuilder(text);
                    sb.append(dialogue.getMessage());
                    text = sb.toString();
                }
            }

        }else {
            List<Dialogue> dataPage = getDialogueDataPage(1, 1);
            if (v == 1){
                for (Dialogue dialogue : dataPage){
                    StringBuilder sb = new StringBuilder(text);
                    sb.append(dialogue.getContent());
                    text = sb.toString();
                }
            }
            if (v == 2) {
                for (Dialogue dialogue : dataPage) {
                    StringBuilder sb = new StringBuilder(text);
                    sb.append(dialogue.getMessage());
                    text = sb.toString();
                }
            }
        }
        // 提取频率最高的几个词
        List<Map.Entry<String, Integer>> list =  findTopFrequentWords(text, 10);
        ArrayList<Map<String, Object>> dataList = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            Map<String, Object> item = new HashMap<>();
            item.put("name", list.get(i).getKey());
            item.put("value", list.get(i).getValue());
            dataList.add(item);
        }

        return dataList;
    }

    public static List<Map.Entry<String, Integer>> findTopFrequentWords(String text, int n) {
        // 分词
        List<Term> termList = HanLP.segment(text);
        // 使用优先队列维护频率最高的5个词
        PriorityQueue<Map.Entry<String, Integer>> pq = new PriorityQueue<>(
                n, Map.Entry.comparingByValue(Comparator.reverseOrder())
        );

        // 统计词频
        Map<String, Integer> wordFreqMap = new HashMap<>();
        for (Term term : termList) {
            // 这里只统计名词和动词，可以根据需要调整
            if (term.nature.startsWith('n') || term.nature.startsWith('v')) {
                String word = term.word;
                wordFreqMap.put(word, wordFreqMap.getOrDefault(word, 0) + 1);
            }
        }
        // 遍历词频Map，维护频率最高的5个词
        for (Map.Entry<String, Integer> entry : wordFreqMap.entrySet()) {
            pq.offer(entry);
            if (pq.size() > n) { // 如果超过5个，移除频率最低的词
                pq.poll();
            }
        }
        // 输出频率最高的5个词
        List<Map.Entry<String, Integer>> topWords = new ArrayList<>();
        while (!pq.isEmpty()) {
            topWords.add( pq.poll());
        }
        Collections.reverse(topWords); // 由于是最大堆，需要反转才能得到降序

        return topWords;
    }
}
