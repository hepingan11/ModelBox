package com.cn.bdth.config;

import cn.hutool.core.io.IoUtil;
import cn.hutool.core.io.resource.ResourceUtil;
import com.wechat.pay.java.core.RSAAutoCertificateConfig;
import com.wechat.pay.java.core.notification.NotificationParser;
import com.wechat.pay.java.core.notification.RequestParam;
import com.wechat.pay.java.service.payments.jsapi.JsapiServiceExtension;
import com.wechat.pay.java.service.payments.nativepay.NativePayService;
import com.wechat.pay.java.service.payments.model.Transaction;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Slf4j
public class PayConfig {

    @Value("${wx.apiKey}")
    private String apiKey;

    @Value("${wx.mchId}")
    private String mchId;

    @Value("${wx.wechatPayPublicKeyId}")
    private String wechatPayPublicKeyId;

    private static final String privateKey = ResourceUtil.readUtf8Str("crt/apiclient_key.pem");

    public JsapiServiceExtension service;

    public NativePayService nativeService;

    public RSAAutoCertificateConfig config;

    @PostConstruct
    public void init() {
        this.config = new RSAAutoCertificateConfig.Builder()
                .merchantId(mchId)
                .privateKey(privateKey)
                .merchantSerialNumber(wechatPayPublicKeyId)
                .apiV3Key(apiKey) // 确保 apiKey 不为 null
                .build();

        this.service = new JsapiServiceExtension.Builder()
                .config(config)
                .build();

        this.nativeService = new NativePayService.Builder()
                .config(config)
                .build();
    }

    /** 获取请求体 */
    public String getRequestBody(HttpServletRequest request) {
        StringBuilder requestBody = new StringBuilder();
        try {
            // hutool工具读取输入流内容
            requestBody.append(IoUtil.readUtf8(request.getInputStream()));
        } catch (IOException e) {
            log.error("读取请求体发生了异常.........");
            e.printStackTrace();
        }
        return requestBody.toString();
    }

    public Transaction parseNotification(HttpServletRequest request) {
        String signature = request.getHeader("Wechatpay-Signature");
        String nonce = request.getHeader("Wechatpay-Nonce");
        String timestamp = request.getHeader("Wechatpay-Timestamp");
        String serial = request.getHeader("Wechatpay-Serial");
        String signatureType = request.getHeader("Wechatpay-Signature-Type");
        String requestBody = getRequestBody(request);// 1.采用输入流方式接收请求体

        // 构建com.wechat.pay.java.core.notification.RequestParam
        RequestParam requestParam = new RequestParam.Builder()
                .serialNumber(serial)
                .nonce(nonce)
                .signType(signatureType)
                .signature(signature)
                .timestamp(timestamp)
                .body(requestBody)
                .build();
        // 构建通知解析器com.wechat.pay.java.core.notification.NotificationParser
        NotificationParser parser = new NotificationParser(config);
        // 验签、解密并转换成 com.wechat.pay.java.service.payments.model.Transaction
        return parser.parse(requestParam, Transaction.class);// 获取通知结果
    }

}
