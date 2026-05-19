package com.healthtrail.domain.common.cache;


import com.healthtrail.infrastructure.cache.guava.AbstractGuavaCacheTemplate;
import com.healthtrail.domain.system.dept.db.SysDeptEntity;
import com.healthtrail.domain.system.config.db.SysConfigService;
import com.healthtrail.domain.system.dept.db.SysDeptService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/** Guava本地缓存服务，管理基于Guava Cache的JVM本地缓存实例 */
@Component
@Slf4j
@RequiredArgsConstructor
public class GuavaCacheService {

    /** 系统参数配置数据库服务 */
    private final SysConfigService configService;

    /** 部门数据库服务 */
    private final SysDeptService deptService;

    /** 系统参数配置本地缓存，key 为配置项键名，缓存未命中时从数据库加载 */
    public final AbstractGuavaCacheTemplate<String> configCache = new AbstractGuavaCacheTemplate<String>() {
        @Override
        public String getObjectFromDb(Object id) {
            return configService.getConfigValueByKey(id.toString());
        }
    };

    /** 部门实体本地缓存，缓存未命中时从数据库加载 */
    public final AbstractGuavaCacheTemplate<SysDeptEntity> deptCache = new AbstractGuavaCacheTemplate<SysDeptEntity>() {
        @Override
        public SysDeptEntity getObjectFromDb(Object id) {
            return deptService.getById(id.toString());
        }
    };


}
