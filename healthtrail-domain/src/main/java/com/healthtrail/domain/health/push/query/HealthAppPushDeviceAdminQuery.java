package com.healthtrail.domain.health.push.query;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.healthtrail.common.core.page.AbstractPageQuery;
import com.healthtrail.common.enums.common.StatusEnum;
import com.healthtrail.common.enums.health.HealthAppPushDeviceActiveStatusEnum;
import com.healthtrail.domain.health.device.db.HealthAppDeviceEntity;
import java.util.Date;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 后台 App 设备管理查询对象。
 *
 * <p>该查询对象只承接设备表本身可以直接过滤的字段。
 * 像手机号、昵称这类需要先查 App 用户表的条件，
 * 由应用服务在进入分页查询前补充处理。
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class HealthAppPushDeviceAdminQuery extends AbstractPageQuery<HealthAppDeviceEntity> {

    private Long ownerUserId;

    private String mobile;

    private String nickname;

    private String deviceCode;

    private String pushPlatform;

    private Integer status;

    private String activeStatus;

    private String keyword;

    @Override
    public QueryWrapper<HealthAppDeviceEntity> addQueryCondition() {
        QueryWrapper<HealthAppDeviceEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(ownerUserId != null, "owner_user_id", ownerUserId)
            .eq(StrUtil.isNotBlank(deviceCode), "device_code", deviceCode)
            .eq(StrUtil.isNotBlank(pushPlatform), "push_platform", pushPlatform)
            .eq(status != null, "status", status)
            .orderByDesc("device_id");

        addActiveStatusCondition(queryWrapper);
        return queryWrapper;
    }

    /**
     * 将派生活跃状态翻译成真实 SQL 条件。
     *
     * <p>这样分页查询可以在数据库层直接过滤，
     * 不需要先查一页再在内存里二次裁剪，避免总数和页数失真。
     */
    private void addActiveStatusCondition(QueryWrapper<HealthAppDeviceEntity> queryWrapper) {
        if (StrUtil.isBlank(activeStatus)) {
            return;
        }
        Date sevenDaysAgo = DateUtil.offsetDay(new Date(), -7);
        Date thirtyDaysAgo = DateUtil.offsetDay(new Date(), -30);
        if (HealthAppPushDeviceActiveStatusEnum.ACTIVE.getValue().equals(activeStatus)) {
            queryWrapper.eq("status", StatusEnum.ENABLE.getValue())
                .ge("last_active_time", sevenDaysAgo);
            return;
        }
        if (HealthAppPushDeviceActiveStatusEnum.SILENT.getValue().equals(activeStatus)) {
            queryWrapper.eq("status", StatusEnum.ENABLE.getValue())
                .lt("last_active_time", sevenDaysAgo)
                .ge("last_active_time", thirtyDaysAgo);
            return;
        }
        if (HealthAppPushDeviceActiveStatusEnum.STALE.getValue().equals(activeStatus)) {
            queryWrapper.eq("status", StatusEnum.ENABLE.getValue())
                .and(wrapper -> wrapper.lt("last_active_time", thirtyDaysAgo).or().isNull("last_active_time"));
            return;
        }
        if (HealthAppPushDeviceActiveStatusEnum.DISABLED.getValue().equals(activeStatus)) {
            queryWrapper.eq("status", StatusEnum.DISABLE.getValue());
        }
    }
}
