package com.healthtrail.domain.system.notice.model;

import cn.hutool.core.bean.BeanUtil;
import com.healthtrail.domain.system.notice.command.NoticeAddCommand;
import com.healthtrail.domain.system.notice.command.NoticeUpdateCommand;
import com.healthtrail.common.enums.common.NoticeTypeEnum;
import com.healthtrail.common.enums.common.StatusEnum;
import com.healthtrail.common.enums.BasicEnumUtil;
import com.healthtrail.domain.system.notice.db.SysNoticeEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 通知公告领域模型，负责公告的增删改命令装载和字段合法性校验。
 *
 * @author valarchie
 */
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
public class NoticeModel extends SysNoticeEntity {

    public NoticeModel(SysNoticeEntity entity) {
        if (entity != null) {
            BeanUtil.copyProperties(entity, this);
        }
    }

    /** 从新增命令装载公告字段 */
    public void loadAddCommand(NoticeAddCommand command) {
        if (command != null) {
            BeanUtil.copyProperties(command, this, "noticeId");
        }
    }

    /** 从更新命令装载公告字段 */
    public void loadUpdateCommand(NoticeUpdateCommand command) {
        if (command != null) {
            loadAddCommand(command);
        }
    }

    /** 校验公告类型和状态是否为合法的枚举值 */
    public void checkFields() {
        BasicEnumUtil.fromValue(NoticeTypeEnum.class, getNoticeType());
        BasicEnumUtil.fromValue(StatusEnum.class, getStatus());
    }

}
