package com.healthtrail.infrastructure.annotations.ratelimit.implementation;

import com.healthtrail.infrastructure.annotations.ratelimit.RateLimit;

/**
 * 限流检查器抽象基类，定义统一的限流校验方法。
 * @author valarchie
 */
public abstract class AbstractRateLimitChecker {

    /**
     * 检查是否超出限流
     *
     * @param rateLimiter RateLimit
     */
    public abstract void check(RateLimit rateLimiter);

}
