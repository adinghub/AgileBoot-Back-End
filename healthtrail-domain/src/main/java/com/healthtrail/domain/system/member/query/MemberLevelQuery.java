package com.healthtrail.domain.system.member.query;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.healthtrail.common.core.page.AbstractPageQuery;
import com.healthtrail.domain.system.member.db.MemberLevelEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 会员等级查询对象。
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class MemberLevelQuery extends AbstractPageQuery<MemberLevelEntity> {

    private String levelCode;
    private String levelName;
    private Integer status;

    @Override
    public QueryWrapper<MemberLevelEntity> addQueryCondition() {
        QueryWrapper<MemberLevelEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(StrUtil.isNotBlank(levelCode), "level_code", levelCode)
            .like(StrUtil.isNotBlank(levelName), "level_name", levelName)
            .eq(status != null, "status", status)
            .orderByAsc("level_sort")
            .orderByDesc("member_level_id");
        return queryWrapper;
    }
}
