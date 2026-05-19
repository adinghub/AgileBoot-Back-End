package com.healthtrail.common.constant;


/**
 * 通用常量信息
 *
 * @author valarchie
 */
public class Constants {
    private Constants() {
    }

    /**
     * 字节单位：KB
     */
    public static final int KB = 1024;

    /**
     * 字节单位：MB
     */
    public static final int MB = KB * 1024;

    /**
     * 字节单位：GB
     */
    public static final int GB = MB * 1024;

    /**
     * http请求
     */
    public static final String HTTP = "http://";

    /**
     * https请求
     */
    public static final String HTTPS = "https://";


    public static class Token {

        private Token() {
        }

        /**
         * 令牌前缀
         */
        public static final String PREFIX = "Bearer ";

        /**
         * 令牌前缀
         */
        public static final String LOGIN_USER_KEY = "login_user_key";

        /**
         * refresh token 对应的 Redis 会话主键。
         *
         * <p>access token 通过 `LOGIN_USER_KEY` 找到当前登录缓存；
         * refresh token 则通过这个 key 找到“刷新会话”，两者职责分开后，
         * 就能支持 access token 过期后静默续签，同时保留主动失效能力。
         */
        public static final String REFRESH_TOKEN_KEY = "refresh_token_key";

        /**
         * token 类型标记字段。
         *
         * <p>当前同时签发 access token 和 refresh token，
         * 因此 JWT 里必须显式区分类型，避免 refresh token 被误当作访问令牌使用。
         */
        public static final String TOKEN_SCENE_KEY = "token_scene";

        /**
         * access token 场景值。
         */
        public static final String ACCESS_TOKEN_SCENE = "access";

        /**
         * refresh token 场景值。
         */
        public static final String REFRESH_TOKEN_SCENE = "refresh";

    }

    public static class Captcha {

        private Captcha() {
        }

        /**
         * 令牌
         */
        public static final String MATH_TYPE = "math";

        /**
         * 令牌前缀
         */
        public static final String CHAR_TYPE = "char";

    }

    /**
     * 资源映射路径 前缀
     */
    public static final String RESOURCE_PREFIX = "profile";

    public static class UploadSubDir {

        private UploadSubDir() {
        }

        /**
         * 导入文件目录
         */
        public static final String IMPORT_PATH = "import";

        /**
         * 头像文件目录
         */
        public static final String AVATAR_PATH = "avatar";

        /**
         * 下载文件目录
         */
        public static final String DOWNLOAD_PATH = "download";

        /**
         * 上传文件目录
         */
        public static final String UPLOAD_PATH = "upload";

        /**
         * 体检报告文件独立目录。
         * 单独分目录可以避免和普通上传文件混放，方便后续做清理、迁移和权限隔离。
         */
        public static final String REPORT_PATH = "report";

        /**
         * 药品图片附件目录。
         *
         * <p>药品主图单独分目录，方便后续清理和迁移时按业务快速定位。
         */
        public static final String DRUG_IMAGE_PATH = "drug-image";

        /**
         * 药品单位图标目录。
         *
         * <p>单位图标和药品图片分开存放，避免后续做资源审计时混在一起。
         */
        public static final String DRUG_UNIT_ICON_PATH = "drug-unit-icon";

    }



}
