package com.healthtrail.domain.system.notice;

import com.healthtrail.common.core.page.PageDTO;
import com.healthtrail.domain.common.command.BulkOperationCommand;
import com.healthtrail.domain.system.notice.command.NoticeAddCommand;
import com.healthtrail.domain.system.notice.command.NoticeUpdateCommand;
import com.healthtrail.domain.system.notice.dto.NoticeDTO;
import com.healthtrail.domain.system.notice.model.NoticeModel;
import com.healthtrail.domain.system.notice.model.NoticeModelFactory;
import com.healthtrail.domain.system.notice.query.NoticeQuery;
import com.healthtrail.domain.system.notice.db.SysNoticeEntity;
import com.healthtrail.domain.system.notice.db.SysNoticeService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/** 系统通知公告应用服务 */
@Service
@RequiredArgsConstructor
public class NoticeApplicationService {

    /** 通知公告数据库服务 */
    private final SysNoticeService noticeService;

    /** 通知公告领域模型工厂 */
    private final NoticeModelFactory noticeModelFactory;

    public PageDTO<NoticeDTO> getNoticeList(NoticeQuery query) {
        Page<SysNoticeEntity> page = noticeService.getNoticeList(query);
        List<NoticeDTO> records = page.getRecords().stream().map(NoticeDTO::new).collect(Collectors.toList());
        return new PageDTO<>(records, page.getTotal());
    }


    public NoticeDTO getNoticeInfo(Long id) {
        NoticeModel noticeModel = noticeModelFactory.loadById(id);
        return new NoticeDTO(noticeModel);
    }


    public void addNotice(NoticeAddCommand addCommand) {
        NoticeModel noticeModel = noticeModelFactory.create();
        noticeModel.loadAddCommand(addCommand);

        noticeModel.checkFields();

        noticeModel.insert();
    }


    public void updateNotice(NoticeUpdateCommand updateCommand) {
        NoticeModel noticeModel = noticeModelFactory.loadById(updateCommand.getNoticeId());
        noticeModel.loadUpdateCommand(updateCommand);

        noticeModel.checkFields();

        noticeModel.updateById();
    }

    public void deleteNotice(BulkOperationCommand<Integer> deleteCommand) {
        noticeService.removeBatchByIds(deleteCommand.getIds());
    }




}
