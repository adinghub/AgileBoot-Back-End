package com.healthtrail.domain.health.report.parser;

/**
 * 报告解析可重试异常。
 *
 * <p>这个异常专门表达一种很重要、但不能和“永久失败”混在一起的场景：
 * 当前解析没有成功，并不是因为文件损坏、字段不合法或代码逻辑出错，
 * 而是因为依赖的外部能力暂时不可用，例如：
 * 1. 外部视觉模型请求超时；
 * 2. 外部网关返回 429 / 5xx；
 * 3. 网络瞬时抖动导致本次调用没拿到结果。
 *
 * <p>之所以单独建类型，而不是只靠字符串匹配异常信息，是为了让上层后台队列可以稳定地区分：
 * 1. 哪些失败应该“延迟重试”；
 * 2. 哪些失败应该“立即终止并标记失败”。
 */
public class HealthReportRetryableParseException extends RuntimeException {

    public HealthReportRetryableParseException(String message) {
        super(message);
    }

    public HealthReportRetryableParseException(String message, Throwable cause) {
        super(message, cause);
    }
}
