package com.healthtrail.infrastructure.thread;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;

import java.util.TimerTask;
import java.util.concurrent.*;

/**
 * 异步任务管理器
 *
 * @author valarchie
 */
@Slf4j
public class ThreadPoolManager {

    private static final ThreadPoolExecutor THREAD_EXECUTOR = new ThreadPoolExecutor(
            ThreadConfig.CORE_POOL_SIZE, ThreadConfig.MAX_POOL_SIZE,
            ThreadConfig.KEEP_ALIVE_SECONDS, TimeUnit.SECONDS,
            new SynchronousQueue<>(), new ThreadPoolExecutor.CallerRunsPolicy());
    private static final ScheduledExecutorService SCHEDULED_EXECUTOR = new ScheduledThreadPoolExecutor(
            ThreadConfig.CORE_POOL_SIZE,
            new BasicThreadFactory.Builder().namingPattern("schedule-pool-%d").daemon(true).build(),
            new ThreadPoolExecutor.CallerRunsPolicy()) {
        @Override
        protected void afterExecute(Runnable r, Throwable t) {
            if (t == null && r instanceof Future<?>) {
                try {
                    Future<?> future = (Future<?>) r;
                    if (future.isDone()) {
                        future.get();
                    }
                } catch (CancellationException ce) {
                    t = ce;
                } catch (ExecutionException ee) {
                    t = ee.getCause();
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            }
            if (t != null) {
                log.error(t.getMessage(), t);
            }
        }
    };

    private ThreadPoolManager() {
    }


    /**
     * 执行schedule任务
     */
    public static void schedule(TimerTask task) {
        SCHEDULED_EXECUTOR.schedule(task, ThreadConfig.OPERATE_DELAY_TIME, TimeUnit.MILLISECONDS);
    }

    /**
     * 按指定延迟执行一次后台任务。
     *
     * <p>相比历史只支持固定 10ms 延迟的 `schedule(TimerTask)`，
     * 这个重载更适合“失败后退避重试”场景：
     * 1. 调用方可以按指数退避动态拉开重试间隔；
     * 2. 不需要额外创建新的定时器线程或 while 轮询；
     * 3. 任务到期后仍由统一的调度线程池接管执行。
     */
    public static void schedule(Runnable task, long delayMs) {
        long safeDelayMs = Math.max(delayMs, 0L);
        SCHEDULED_EXECUTOR.schedule(task, safeDelayMs, TimeUnit.MILLISECONDS);
    }

    /**
     * 执行异步任务任务
     */
    public static void execute(Runnable task) {
        THREAD_EXECUTOR.execute(task);
    }

    /**
     * 停止任务线程池
     */
    public static void shutdown() {
        THREAD_EXECUTOR.shutdown();
        SCHEDULED_EXECUTOR.shutdown();
    }
}
