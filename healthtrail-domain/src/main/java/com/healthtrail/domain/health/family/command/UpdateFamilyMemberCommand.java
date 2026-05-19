package com.healthtrail.domain.health.family.command;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 修改家庭成员命令对象。
 *
 * <p>除主键外，其余字段与新增命令保持一致，
 * 这样接口风格更统一，前端表单也更容易复用。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class UpdateFamilyMemberCommand extends AddFamilyMemberCommand {

    /**
     * 家庭成员ID。
     */
    private Long memberId;
}
