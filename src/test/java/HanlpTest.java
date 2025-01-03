import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.seg.common.Term;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.*;

import static com.cn.bdth.service.impl.DataServiceImpl.findTopFrequentWords;


public class HanlpTest {


    public static void main(String[] args) {
        String text = "你的文本字符串，比如：今天天气真好，适合出去散步。哈哈哈哈哈哈";
        // 分词
        List<Term> termList = HanLP.segment(text);
        // 使用优先队列维护频率最高的5个词
        PriorityQueue<Map.Entry<String, Integer>> pq = new PriorityQueue<>(
                3, Map.Entry.comparingByValue(Comparator.reverseOrder())
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
            if (pq.size() > 3) { // 如果超过5个，移除频率最低的词
                pq.poll();
            }
        }

        // 输出频率最高的5个词
        List<Map.Entry<String, Integer>> topWords = new ArrayList<>();
        while (!pq.isEmpty()) {
            topWords.add(pq.poll());
        }
        Collections.reverse(topWords); // 由于是最大堆，需要反转才能得到降序

        for (Map.Entry<String, Integer> entry : topWords) {
            System.out.println("词语: " + entry.getKey() + ", 频率: " + entry.getValue());
        }
    }
}
