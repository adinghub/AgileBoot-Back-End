package com.healthtrail.domain.health.family.dto;

import lombok.Builder;
import lombok.Data;

/**
 * 家庭成员访问上下文。
 *
 * <p>该对象主要供应用服务内部流转，
 * 用来统一表达“当前用户对某个成员是什么来源、什么角色、是否可编辑”。
 */
@Data
@Builder
public class FamilyMemberAccessContextDTO {

    /** 成员ID。 */
    private Long memberId;

    /** 归属用户ID */
    private Long ownerUserId;

    /** 当前操作用户ID */
    private Long currentUserId;

    /** 访问来源类型 */
    private String accessSource;

    /** 访问来源名称 */
    private String accessSourceName;

    /** 访问角色 */
    private String accessRole;

    /** 访问角色名称 */
    private String accessRoleName;

    /** 是否为成员归属人 */
    private boolean owner;

    /** 是否可编辑 */
    private boolean canEdit;

    /** 共享关系ID */
    private Long shareId;
}
