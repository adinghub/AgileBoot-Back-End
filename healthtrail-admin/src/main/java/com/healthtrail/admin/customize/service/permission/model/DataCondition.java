package com.healthtrail.admin.customize.service.permission.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 数据权限校验条件模型，封装目标部门ID和目标用户ID供检查器使用。
 * @author valarchie
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DataCondition {

    /** 目标部门ID */
    private Long targetDeptId;
    /** 目标用户ID */
    private Long targetUserId;

}
