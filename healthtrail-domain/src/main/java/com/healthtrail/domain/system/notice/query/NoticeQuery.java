package com.healthtrail.domain.system.notice.query;

import cn.hutool.core.util.StrUtil;
import com.healthtrail.common.core.page.AbstractPageQuery;
import com.healthtrail.domain.system.notice.db.SysNoticeEntity;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * @author valarchie
 */
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
public class NoticeQuery extends AbstractPageQuery<SysNoticeEntity> {

    private String noticeType;

    private String noticeTitle;

    private String creatorName;


    @Override
    public QueryWrapper<SysNoticeEntity> addQueryCondition() {
        QueryWrapper<SysNoticeEntity> queryWrapper = new QueryWrapper<SysNoticeEntity>()
            .like(StrUtil.isNotEmpty(noticeTitle), "notice_title", noticeTitle)
            .eq(StrUtil.isNotEmpty(noticeType), "notice_type", noticeType);
        return queryWrapper;
    }
}
