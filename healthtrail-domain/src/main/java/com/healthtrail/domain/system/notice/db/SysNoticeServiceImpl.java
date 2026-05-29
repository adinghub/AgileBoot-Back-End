package com.healthtrail.domain.system.notice.db;

import cn.hutool.core.util.StrUtil;
import com.healthtrail.domain.system.notice.query.NoticeQuery;
import com.healthtrail.domain.system.user.db.SysUserEntity;
import com.healthtrail.domain.system.user.db.SysUserMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 通知公告表 服务实现类
 * </p>
 *
 * @author valarchie
 * @since 2022-06-16
 */
@Service
@RequiredArgsConstructor
public class SysNoticeServiceImpl extends ServiceImpl<SysNoticeMapper, SysNoticeEntity> implements SysNoticeService {

    private final SysUserMapper userMapper;

    @Override
    public Page<SysNoticeEntity> getNoticeList(NoticeQuery query) {
        QueryWrapper<SysNoticeEntity> queryWrapper = query.toQueryWrapper();
        Set<Long> creatorIds = getCreatorIds(query.getCreatorName());
        if (creatorIds.isEmpty() && StrUtil.isNotEmpty(query.getCreatorName())) {
            Page<SysNoticeEntity> emptyPage = query.toPage();
            emptyPage.setRecords(Collections.emptyList());
            emptyPage.setTotal(0);
            return emptyPage;
        }

        queryWrapper.in(!creatorIds.isEmpty(), "creator_id", creatorIds);
        return this.page(query.toPage(), queryWrapper);
    }

    private Set<Long> getCreatorIds(String creatorName) {
        if (StrUtil.isEmpty(creatorName)) {
            return Collections.emptySet();
        }

        QueryWrapper<SysUserEntity> userQuery = new QueryWrapper<>();
        userQuery.like("username", creatorName);
        List<SysUserEntity> users = userMapper.selectList(userQuery);
        return users.stream()
            .map(SysUserEntity::getUserId)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());
    }

}
