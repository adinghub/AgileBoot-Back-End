package com.healthtrail.domain.system.member.query;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.healthtrail.common.core.page.AbstractPageQuery;
import com.healthtrail.domain.system.member.db.MemberRedeemCodeEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 会员兑换码查询对象。
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class MemberRedeemCodeQuery extends AbstractPageQuery<MemberRedeemCodeEntity> {

    private String batchNo;
    private String redeemCode;
    private Long memberLevelId;
    private String codeStatus;

    @Override
    public QueryWrapper<MemberRedeemCodeEntity> addQueryCondition() {
        QueryWrapper<MemberRedeemCodeEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(StrUtil.isNotBlank(batchNo), "batch_no", batchNo)
            .eq(StrUtil.isNotBlank(redeemCode), "redeem_code", redeemCode)
            .eq(memberLevelId != null, "member_level_id", memberLevelId)
            .eq(StrUtil.isNotBlank(codeStatus), "code_status", codeStatus)
            .orderByDesc("member_redeem_code_id");
        return queryWrapper;
    }
}
