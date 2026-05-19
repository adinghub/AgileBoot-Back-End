package com.healthtrail.domain.health.device.push;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.ContentType;
import cn.hutool.http.Header;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.healthtrail.domain.health.device.config.HealthExternalPushGatewayProperties;
import com.healthtrail.domain.health.device.db.HealthAppDeviceEntity;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 外部 Push 网关客户端。
 *
 * <p>这个客户端不绑定具体厂商 SDK，而是统一向外部 HTTP 网关发送：
 * 1. 设备列表
 * 2. 展示文案
 * 3. 业务透传载荷
 *
 * <p>这样后端业务层可以继续围绕“健康提醒 / 消息重发”组织数据，
 * 真正接华为、小米、极光还是企业自建中台，都可以通过网关层适配。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class HealthExternalPushGatewayClient {

    /** 外部Push网关配置属性 */
    private final HealthExternalPushGatewayProperties properties;

    /**
     * 当前网关是否可用。
     */
    public boolean isReady() {
        return properties.isEnabled()
            && StrUtil.isAllNotBlank(properties.getApiUrl(), properties.getApiKey());
    }

    /**
     * 批量发送 Push。
     */
    public HealthExternalPushGatewayResult pushBatch(List<HealthAppDeviceEntity> devices, String title,
        String content, Object payload) {
        if (!isReady()) {
            return HealthExternalPushGatewayResult.builder()
                .success(false)
                .channel("APP_PUSH")
                .message("外部Push网关未启用或配置不完整")
                .build();
        }
        if (devices == null || devices.isEmpty()) {
            return HealthExternalPushGatewayResult.builder()
                .success(false)
                .channel("APP_PUSH")
                .message("没有可发送的设备")
                .build();
        }

        JSONObject requestBody = new JSONObject();
        requestBody.set("providerName", properties.getProviderName());
        requestBody.set("title", StrUtil.blankToDefault(title, "健康提醒"));
        requestBody.set("content", StrUtil.blankToDefault(content, ""));
        requestBody.set("payload", payload == null ? new JSONObject() : JSONUtil.parse(payload));
        requestBody.set("devices", buildDeviceArray(devices));

        try (HttpResponse response = buildRequest(requestBody).execute()) {
            if (response == null) {
                return HealthExternalPushGatewayResult.builder()
                    .success(false)
                    .channel("APP_PUSH")
                    .message("外部Push网关未返回响应")
                    .build();
            }
            if (response.getStatus() < 200 || response.getStatus() >= 300) {
                log.warn("外部Push网关调用失败，status={}, body={}", response.getStatus(), limitLength(response.body(), 1000));
                return HealthExternalPushGatewayResult.builder()
                    .success(false)
                    .channel("APP_PUSH")
                    .message(StrUtil.blankToDefault(limitLength(response.body(), 255), "外部Push网关调用失败"))
                    .vendorCode(String.valueOf(response.getStatus()))
                    .vendorMessage(limitLength(response.body(), 255))
                    .build();
            }
            return parseResult(response.body(), devices.size());
        } catch (Exception ex) {
            log.error("调用外部Push网关失败", ex);
            return HealthExternalPushGatewayResult.builder()
                .success(false)
                .channel("APP_PUSH")
                .message(limitLength(ex.getMessage(), 255))
                .vendorMessage(limitLength(ex.getMessage(), 255))
                .build();
        }
    }

    private HttpRequest buildRequest(JSONObject requestBody) {
        HttpRequest request = HttpRequest.post(properties.getApiUrl())
            .timeout(properties.getTimeoutMs() == null ? 15000 : properties.getTimeoutMs())
            .header(Header.CONTENT_TYPE, ContentType.JSON.getValue())
            .header(Header.AUTHORIZATION, "Bearer " + properties.getApiKey())
            .body(requestBody.toString());
        if (properties.getHeaders() != null && !properties.getHeaders().isEmpty()) {
            for (Map.Entry<String, String> entry : properties.getHeaders().entrySet()) {
                if (StrUtil.isNotBlank(entry.getKey()) && StrUtil.isNotBlank(entry.getValue())) {
                    request.header(entry.getKey(), entry.getValue());
                }
            }
        }
        return request;
    }

    private JSONArray buildDeviceArray(List<HealthAppDeviceEntity> devices) {
        JSONArray deviceArray = new JSONArray();
        devices.forEach(device -> deviceArray.add(JSONUtil.createObj()
            .set("deviceCode", device.getDeviceCode())
            .set("deviceToken", device.getDeviceToken())
            .set("pushPlatform", device.getPushPlatform())));
        return deviceArray;
    }

    private HealthExternalPushGatewayResult parseResult(String responseBody, int deviceCount) {
        if (StrUtil.isBlank(responseBody)) {
            return HealthExternalPushGatewayResult.builder()
                .success(true)
                .channel("APP_PUSH_GATEWAY")
                .message("外部Push网关已接受 " + deviceCount + " 个设备的发送请求")
                .build();
        }
        JSONObject jsonObject = JSONUtil.parseObj(responseBody);
        boolean success = jsonObject.containsKey("success")
            ? jsonObject.getBool("success", true)
            : jsonObject.getInt("code", 0) == 0;
        String channel = StrUtil.blankToDefault(jsonObject.getStr("channel"), "APP_PUSH_GATEWAY");
        String vendorCode = jsonObject.containsKey("code") ? String.valueOf(jsonObject.get("code")) : null;
        String message = StrUtil.blankToDefault(jsonObject.getStr("message"),
            "外部Push网关已接受 " + deviceCount + " 个设备的发送请求");
        return HealthExternalPushGatewayResult.builder()
            .success(success)
            .channel(channel)
            .message(limitLength(message, 255))
            .vendorCode(limitLength(vendorCode, 50))
            .vendorMessage(limitLength(message, 255))
            .build();
    }

    private String limitLength(String text, int maxLength) {
        if (text == null || text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength);
    }
}
