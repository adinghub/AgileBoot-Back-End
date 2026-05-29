package com.healthtrail.domain.system.notice.db;

import com.healthtrail.domain.system.notice.query.NoticeQuery;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 通知公告表 服务类
 * </p>
 *
 * @author valarchie
 * @since 2022-06-16
 */
public interface SysNoticeService extends IService<SysNoticeEntity> {

    /**
     * 获取公告列表
     *
     * @param query 查询对象
     * @return 分页处理后的公告列表
     */
    Page<SysNoticeEntity> getNoticeList(NoticeQuery query);

}
