package com.cn.bdth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 雨纷纷旧故里草木深
 *
 * @author  @github dulaiduwang003
 * @version 1.0
 */
@Data
@Accessors(chain = true)
public class ServerConfigDto {

    @NotBlank(message = "SD_URL不能为空")
    private String sdUrl;

    @NotBlank(message = "OPEN_AI_URL不能为空")
    private String openAiUrl;

    @NotBlank(message = "是否开启SD绘画不能为空")
    private String sdButton;

    @NotBlank(message = "OPEN_AI_PLUS_URL不能为空")
    private String openAiPlusUrl;

    @NotBlank(message = "OPEN_KEY不能为空")
    private String openKey;

    @NotBlank(message = "OPEN_PLUS_KEY不能为空")
    private String openPlusKey;

    @NotNull(message = "GPT_PLUS消耗次数不能为空")
    private Long gptPlusFrequency;

    @NotBlank(message = "顶图链接不能为空")
    private String linkTopImg;

    @NotNull(message = "是否开启hadoop存储")
    private String isHadoop;

    @NotNull(message = "第一次登录奖励次数不能为空")
    private Long incentiveFrequency;

    @NotNull(message = "用户观看视频奖励不能为空")
    private Long videoFrequency;

    @NotNull(message = "签到赠送次数不能为空")
    private Long signInFrequency;

    @NotNull(message = "图生图消耗次数不能为空")
    private Long sdImageFrequency;

    @NotNull(message = "GPT消耗次数不能为空")
    private Long gptFrequency;

    @NotNull(message = "标准文生图消耗次数不能为空")
    private Long gptTextImageFrequency;

}
