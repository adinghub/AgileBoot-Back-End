package com.healthtrail.domain.health.device.config;

import java.util.HashMap;
import java.util.Map;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 外部 Push 网关配置。
 *
 * <p>这里采用“HTTP 网关 + 设备列表 + 透传业务载荷”的通用协议设计，
 * 原因是不同厂商、不同中台的推送接口差异通常集中在：
 * 1. 鉴权方式
 * 2. 请求头
 * 3. 返回体字段
 *
 * <p>而健康系统真正稳定的业务输入其实只有：
 * 1. 发给哪些设备
 * 2. 标题和正文是什么
 * 3. 点击后应该跳到哪里
 */
@Component
@ConfigurationProperties(prefix = "health.push.gateway")
@Data
public class HealthExternalPushGatewayProperties {

    /**
     * 是否启用外部网关。
     */
    private boolean enabled = false;

    /**
     * 供应商名称，仅用于日志和返回文案。
     */
    private String providerName = "HTTP_PUSH_GATEWAY";

    /**
     * 网关地址。
     */
    private String apiUrl;

    /**
     * 网关密钥。
     */
    private String apiKey;

    /**
     * HTTP 超时时间。
     */
    private Integer timeoutMs = 15000;

    /**
     * 额外请求头。
     */
    private Map<String, String> headers = new HashMap<>();
}
