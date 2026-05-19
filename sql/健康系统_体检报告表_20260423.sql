create table health_report
(
    report_id           bigint auto_increment comment '报告ID'
        primary key,
    owner_user_id       bigint                    not null comment '归属App用户ID',
    member_id           bigint                    not null comment '家庭成员ID',
    report_name         varchar(100)              not null comment '报告名称',
    report_type         varchar(50)               null comment '报告类型',
    recognized_report_type varchar(50)            null comment '后台识别出的报告细分类型',
    hospital_name       varchar(100)              null comment '医院名称',
    report_date         date                      null comment '报告日期',
    file_url            varchar(255)              not null comment '文件访问地址',
    stored_file_name    varchar(255)              not null comment '存储文件名',
    original_file_name  varchar(255)              not null comment '原始文件名',
    file_size           bigint                    null comment '文件大小（字节）',
    file_extension      varchar(20)               null comment '文件后缀',
    parse_status        smallint      default 0   not null comment '解析状态（0待解析 1解析中 2已解析 3解析失败）',
    ai_parse_cached     smallint      default 0   not null comment 'AI解析结果缓存标记（0未缓存 1已缓存，再次解析时可直接复用已有结构化结果）',
    analysis_summary    varchar(1000)             null comment '解析摘要',
    result_interpretation varchar(2000)           null comment '结果解读，结合本次结果说明主要结论和实验有效性',
    remark              varchar(500)              null comment '备注',
    creator_id          bigint                    null comment '创建者ID',
    create_time         datetime                  null comment '创建时间',
    updater_id          bigint                    null comment '更新者ID',
    update_time         datetime                  null comment '更新时间',
    deleted             tinyint(1)    default 0   not null comment '逻辑删除'
)
    comment '健康系统体检报告表';

create index idx_health_report_owner_user_id on health_report (owner_user_id);
create index idx_health_report_member_id on health_report (member_id);
create index idx_health_report_report_date on health_report (report_date);
create index idx_health_report_parse_status on health_report (parse_status);
