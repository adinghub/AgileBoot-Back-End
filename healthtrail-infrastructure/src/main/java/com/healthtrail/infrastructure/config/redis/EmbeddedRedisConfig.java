package com.healthtrail.infrastructure.config.redis;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Configuration;
import redis.embedded.RedisServer;

/**
 * 内嵌Redis配置，用于开发测试环境自动启动本地Redis服务。
 * @author valarchie
 */
@Configuration
@ConditionalOnExpression("'${healthtrail.embedded.redis}' == 'true'")
public class EmbeddedRedisConfig {

    @Value("${spring.redis.port}")
    private Integer port;

    private RedisServer redisServer;


    @PostConstruct
    public void postConstruct() {
        RedisServer redisServer = RedisServer.builder().port(port)
            .setting("maxheap 32M")
            .setting("daemonize no")
            .setting("appendonly no").build();
        this.redisServer = redisServer;
        redisServer.start();
    }

    @PreDestroy
    public void preDestroy() {
        redisServer.stop();
    }

}
