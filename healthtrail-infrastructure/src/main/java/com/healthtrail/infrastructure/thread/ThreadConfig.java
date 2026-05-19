package com.healthtrail.infrastructure.thread;

/**
 * 线程池配置常量，定义核心线程数、最大线程数、队列容量和保活时间。
 * @author valarchie
 */
public class ThreadConfig {

    /** 核心线程数 */
    public static final int CORE_POOL_SIZE = 50;
    /** 最大线程数 */
    public static final int MAX_POOL_SIZE = 200;
    /** 队列容量 */
    public static final int QUEUE_CAPACITY = 1000;
    /** 线程保活时间（秒） */
    public static final int KEEP_ALIVE_SECONDS = 300;
    /** 操作延迟时间（毫秒） */
    public static final int OPERATE_DELAY_TIME = 10;

    private ThreadConfig() {
    }
}
