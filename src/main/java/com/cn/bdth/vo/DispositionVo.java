package com.cn.bdth.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 雨纷纷旧故里草木深
 *
 * @author  @github dulaiduwang003
 * @version 1.0
 */
@Data
public class DispositionVo implements Serializable {

    private String openAiUrl;

    private String openAiPlusUrl;

    private String openKey;

    private String openPlusKey;

    private Long gptTextImageFrequency;

    private Long gptFrequency;

    private Long gptPlusFrequency;

    private String linkTopImg;

    private String sdButton;

    private Long sdImageFrequency;

    private String sdUrl;

    private Long incentiveFrequency;

    private Long signInFrequency;

    private Long videoFrequency;

    private String isHadoop;
}
