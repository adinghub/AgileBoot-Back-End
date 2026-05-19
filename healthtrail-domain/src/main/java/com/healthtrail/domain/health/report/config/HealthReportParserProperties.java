package com.healthtrail.domain.health.report.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 检查报告后台解析配置。
 *
 * <p>当前配置只保留“是否自动解析”这一类流程开关。
 * OCR 相关命令行配置已经移除，图片报告统一由 AI 视觉解析或人工补录处理。
 */
@Component
@ConfigurationProperties(prefix = "health.report.parser")
@Data
public class HealthReportParserProperties {

    /**
     * 上传后是否自动触发后台解析。
     */
    private boolean autoParseOnUpload = true;

    /**
     * 是否启用“后台解析失败后自动重试”。
     *
     * <p>这里的“重试”不是针对所有失败都盲目重来，而是只给那些典型的临时性问题兜底：
     * 1. 外部视觉模型调用超时；
     * 2. 外部模型网关限流；
     * 3. 第三方服务短暂抖动。
     *
     * <p>关闭后，解析任务仍然走异步线程，但一旦失败就直接结束，不再自动回队列。
     */
    private boolean asyncRetryEnabled = true;

    /**
     * 后台解析最大自动重试次数。
     *
     * <p>这里不包含首次执行本身，只表示“失败后额外还能再试几次”。
     *
     * <p>约定：
     * 1. 大于 0：最多重试指定次数；
     * 2. 小于等于 0：视为不限次数，持续留在队列中等待成功。
     *
     * <p>虽然业务上可以支持“不限次数”，但默认值仍然建议保守一些，
     * 避免某份异常文件长期占用后台资源却一直没有恢复希望。
     */
    private int maxAsyncRetryCount = 8;

    /**
     * 第一次自动重试前的等待时间，单位毫秒。
     *
     * <p>不立刻秒重试，是为了给：
     * 1. 外部模型网关限流窗口；
     * 2. 网络短暂抖动；
     * 3. 第三方服务瞬时恢复；
     * 留出一点自然缓冲时间。
     */
    private long asyncRetryInitialDelayMs = 30_000L;

    /**
     * 自动重试的最大退避时间，单位毫秒。
     *
     * <p>后台重试采用指数退避，如果不设上限，等待时间会很快膨胀得过大，
     * 既不利于联调，也不便于业务理解任务到底什么时候会再试。
     */
    private long asyncRetryMaxDelayMs = 10 * 60_000L;
}
