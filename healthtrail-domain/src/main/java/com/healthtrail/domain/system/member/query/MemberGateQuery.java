package com.healthtrail.domain.system.member.query;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.healthtrail.common.core.page.AbstractPageQuery;
import com.healthtrail.domain.system.member.db.MemberGateEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 会员功能门禁点查询对象。
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class MemberGateQuery extends AbstractPageQuery<MemberGateEntity> {

    private String gateCode;
    private String gateName;
    private String gateScope;
    private String bizModule;
    private String terminalType;
    private Integer status;

    @Override
    public QueryWrapper<MemberGateEntity> addQueryCondition() {
        QueryWrapper<MemberGateEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(StrUtil.isNotBlank(gateCode), "gate_code", gateCode)
            .like(StrUtil.isNotBlank(gateName), "gate_name", gateName)
            .eq(StrUtil.isNotBlank(gateScope), "gate_scope", gateScope)
            .eq(StrUtil.isNotBlank(bizModule), "biz_module", bizModule)
            .eq(StrUtil.isNotBlank(terminalType), "terminal_type", terminalType)
            .eq(status != null, "status", status)
            .orderByAsc("biz_module")
            .orderByAsc("gate_code")
            .orderByDesc("member_gate_id");
        return queryWrapper;
    }
}
