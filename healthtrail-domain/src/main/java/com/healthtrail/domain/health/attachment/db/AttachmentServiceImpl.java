package com.healthtrail.domain.health.attachment.db;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * 统一附件数据库服务实现。
 */
@Service
public class AttachmentServiceImpl
    extends ServiceImpl<AttachmentMapper, AttachmentEntity>
    implements AttachmentService {
}
