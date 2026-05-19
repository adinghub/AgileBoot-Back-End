package com.healthtrail.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 文件存储配置。
 *
 * <p>这里把“本地文件目录”和“MinIO 对象存储”统一抽象到一个配置对象里，
 * 目的是让上传、下载、报告解析等调用方只关心“当前用哪种存储”，
 * 不再把 endpoint、bucket、accessKey 这些底层细节散落到业务代码里。
 */
@Component
@ConfigurationProperties(prefix = "healthtrail.storage")
@Data
public class HealthTrailStorageConfig {

    /**
     * 存储类型。
     *
     * <p>当前支持：
     * 1. LOCAL：沿用项目原有的本地磁盘 + /profile/** 静态资源映射；
     * 2. MINIO：上传到 MinIO/S3 兼容对象存储，并返回对象访问地址。
     */
    private String type = "LOCAL";

    /**
     * MinIO 配置。
     *
     * <p>即使当前环境走 LOCAL，也保留该对象，
     * 这样配置结构始终稳定，切换到 MINIO 时不需要再改代码结构。
     */
    private Minio minio = new Minio();

    @Data
    public static class Minio {

        /**
         * MinIO 服务地址，例如 oss-api.dingqidong.com。
         */
        private String endpoint;

        /**
         * 桶名称。
         *
         * <p>当前项目约定：
         * - 开发环境：healthtrail-dev
         * - 生产环境：healthtrail-prod
         */
        private String bucket;

        /**
         * MinIO AccessKey。
         */
        private String accessKey;

        /**
         * MinIO SecretKey。
         */
        private String secretKey;

        /**
         * 是否使用 HTTPS。
         */
        private Boolean useSsl = true;

        /**
         * 对外访问前缀。
         *
         * <p>如果这里为空，系统会按
         * http(s)://endpoint/bucket/
         * 自动拼接默认访问前缀。
         */
        private String publicUrlPrefix;
    }
}
