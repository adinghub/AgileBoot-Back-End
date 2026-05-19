-- 健康系统后台菜单与按钮权限增量脚本
--
-- 设计目标：
-- 1. 给健康系统后台能力补齐可直接落库的 sys_menu / sys_role_menu 配置
-- 2. 覆盖当前已经完成前后端联调的七个后台页面：
--    - 系统药品管理
--    - 消息审计
--    - 指标模板管理
--    - 家庭共享管理
--    - 用药历史与依从率管理
--    - 首页运营任务管理
--    - 体检报告管理
-- 3. 脚本尽量按“幂等执行”设计，避免重复导入后生成重复菜单
--
-- 使用建议：
-- 1. 在 MySQL 环境执行
-- 2. 建议先在测试环境验证，再同步到正式环境
-- 3. 如果你们线上已经存在“健康系统”目录菜单，脚本会优先复用该目录，不会重复创建

-- ----------------------------
-- 1. 健康系统目录菜单
-- ----------------------------
SET @health_root_menu_id := (
    SELECT menu_id
    FROM sys_menu
    WHERE deleted = 0
      AND parent_id = 0
      AND path = '/health'
    LIMIT 1
);

SET @health_root_menu_id := IFNULL(
    @health_root_menu_id,
    (SELECT IFNULL(MAX(menu_id), 0) + 1 FROM sys_menu)
);

INSERT INTO sys_menu (
    menu_id, menu_name, menu_type, router_name, parent_id, path, is_button,
    permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted
)
SELECT
    @health_root_menu_id,
    '健康系统',
    2,
    '',
    0,
    '/health',
    0,
    '',
    '{"title":"健康系统","icon":"ep:first-aid-kit","showParent":1,"rank":10}',
    1,
    '健康系统后台目录菜单',
    1,
    NOW(),
    1,
    NOW(),
    0
FROM dual
WHERE NOT EXISTS (
    SELECT 1
    FROM sys_menu
    WHERE deleted = 0
      AND parent_id = 0
      AND path = '/health'
);

-- ----------------------------
-- 2. 系统药品管理菜单与按钮
-- ----------------------------
SET @health_drug_menu_id := (
    SELECT menu_id
    FROM sys_menu
    WHERE deleted = 0
      AND parent_id = @health_root_menu_id
      AND path = '/health/drug'
    LIMIT 1
);

SET @health_drug_menu_id := IFNULL(
    @health_drug_menu_id,
    (SELECT IFNULL(MAX(menu_id), 0) + 1 FROM sys_menu)
);

INSERT INTO sys_menu (
    menu_id, menu_name, menu_type, router_name, parent_id, path, is_button,
    permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted
)
SELECT
    @health_drug_menu_id,
    '系统药品管理',
    1,
    'HealthDrug',
    @health_root_menu_id,
    '/health/drug',
    0,
    'health:drug:list',
    '{"title":"系统药品管理","showParent":1}',
    1,
    '健康系统后台系统药品管理菜单',
    1,
    NOW(),
    1,
    NOW(),
    0
FROM dual
WHERE NOT EXISTS (
    SELECT 1
    FROM sys_menu
    WHERE deleted = 0
      AND parent_id = @health_root_menu_id
      AND path = '/health/drug'
);

SET @health_drug_query_button_id := (
    SELECT menu_id
    FROM sys_menu
    WHERE deleted = 0
      AND parent_id = @health_drug_menu_id
      AND permission = 'health:drug:query'
    LIMIT 1
);

SET @health_drug_query_button_id := IFNULL(
    @health_drug_query_button_id,
    (SELECT IFNULL(MAX(menu_id), 0) + 1 FROM sys_menu)
);

INSERT INTO sys_menu (
    menu_id, menu_name, menu_type, router_name, parent_id, path, is_button,
    permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted
)
SELECT
    @health_drug_query_button_id,
    '药品详情',
    0,
    ' ',
    @health_drug_menu_id,
    '',
    1,
    'health:drug:query',
    '{"title":"药品详情"}',
    1,
    '系统药品管理-详情按钮权限',
    1,
    NOW(),
    1,
    NOW(),
    0
FROM dual
WHERE NOT EXISTS (
    SELECT 1
    FROM sys_menu
    WHERE deleted = 0
      AND parent_id = @health_drug_menu_id
      AND permission = 'health:drug:query'
);

SET @health_drug_add_button_id := (
    SELECT menu_id
    FROM sys_menu
    WHERE deleted = 0
      AND parent_id = @health_drug_menu_id
      AND permission = 'health:drug:add'
    LIMIT 1
);

SET @health_drug_add_button_id := IFNULL(
    @health_drug_add_button_id,
    (SELECT IFNULL(MAX(menu_id), 0) + 1 FROM sys_menu)
);

INSERT INTO sys_menu (
    menu_id, menu_name, menu_type, router_name, parent_id, path, is_button,
    permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted
)
SELECT
    @health_drug_add_button_id,
    '药品新增',
    0,
    ' ',
    @health_drug_menu_id,
    '',
    1,
    'health:drug:add',
    '{"title":"药品新增"}',
    1,
    '系统药品管理-新增按钮权限',
    1,
    NOW(),
    1,
    NOW(),
    0
FROM dual
WHERE NOT EXISTS (
    SELECT 1
    FROM sys_menu
    WHERE deleted = 0
      AND parent_id = @health_drug_menu_id
      AND permission = 'health:drug:add'
);

SET @health_drug_edit_button_id := (
    SELECT menu_id
    FROM sys_menu
    WHERE deleted = 0
      AND parent_id = @health_drug_menu_id
      AND permission = 'health:drug:edit'
    LIMIT 1
);

SET @health_drug_edit_button_id := IFNULL(
    @health_drug_edit_button_id,
    (SELECT IFNULL(MAX(menu_id), 0) + 1 FROM sys_menu)
);

INSERT INTO sys_menu (
    menu_id, menu_name, menu_type, router_name, parent_id, path, is_button,
    permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted
)
SELECT
    @health_drug_edit_button_id,
    '药品修改',
    0,
    ' ',
    @health_drug_menu_id,
    '',
    1,
    'health:drug:edit',
    '{"title":"药品修改"}',
    1,
    '系统药品管理-修改按钮权限',
    1,
    NOW(),
    1,
    NOW(),
    0
FROM dual
WHERE NOT EXISTS (
    SELECT 1
    FROM sys_menu
    WHERE deleted = 0
      AND parent_id = @health_drug_menu_id
      AND permission = 'health:drug:edit'
);

SET @health_drug_remove_button_id := (
    SELECT menu_id
    FROM sys_menu
    WHERE deleted = 0
      AND parent_id = @health_drug_menu_id
      AND permission = 'health:drug:remove'
    LIMIT 1
);

SET @health_drug_remove_button_id := IFNULL(
    @health_drug_remove_button_id,
    (SELECT IFNULL(MAX(menu_id), 0) + 1 FROM sys_menu)
);

INSERT INTO sys_menu (
    menu_id, menu_name, menu_type, router_name, parent_id, path, is_button,
    permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted
)
SELECT
    @health_drug_remove_button_id,
    '药品删除',
    0,
    ' ',
    @health_drug_menu_id,
    '',
    1,
    'health:drug:remove',
    '{"title":"药品删除"}',
    1,
    '系统药品管理-删除按钮权限',
    1,
    NOW(),
    1,
    NOW(),
    0
FROM dual
WHERE NOT EXISTS (
    SELECT 1
    FROM sys_menu
    WHERE deleted = 0
      AND parent_id = @health_drug_menu_id
      AND permission = 'health:drug:remove'
);

-- ----------------------------
-- 3. 消息审计菜单与按钮
-- ----------------------------
SET @health_message_menu_id := (
    SELECT menu_id
    FROM sys_menu
    WHERE deleted = 0
      AND parent_id = @health_root_menu_id
      AND path = '/health/message'
    LIMIT 1
);

SET @health_message_menu_id := IFNULL(
    @health_message_menu_id,
    (SELECT IFNULL(MAX(menu_id), 0) + 1 FROM sys_menu)
);

INSERT INTO sys_menu (
    menu_id, menu_name, menu_type, router_name, parent_id, path, is_button,
    permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted
)
SELECT
    @health_message_menu_id,
    '消息审计',
    1,
    'HealthMessage',
    @health_root_menu_id,
    '/health/message',
    0,
    'health:message:list',
    '{"title":"消息审计","showParent":1}',
    1,
    '健康系统后台消息审计菜单',
    1,
    NOW(),
    1,
    NOW(),
    0
FROM dual
WHERE NOT EXISTS (
    SELECT 1
    FROM sys_menu
    WHERE deleted = 0
      AND parent_id = @health_root_menu_id
      AND path = '/health/message'
);

SET @health_message_query_button_id := (
    SELECT menu_id
    FROM sys_menu
    WHERE deleted = 0
      AND parent_id = @health_message_menu_id
      AND permission = 'health:message:query'
    LIMIT 1
);

SET @health_message_query_button_id := IFNULL(
    @health_message_query_button_id,
    (SELECT IFNULL(MAX(menu_id), 0) + 1 FROM sys_menu)
);

INSERT INTO sys_menu (
    menu_id, menu_name, menu_type, router_name, parent_id, path, is_button,
    permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted
)
SELECT
    @health_message_query_button_id,
    '消息详情',
    0,
    ' ',
    @health_message_menu_id,
    '',
    1,
    'health:message:query',
    '{"title":"消息详情"}',
    1,
    '消息审计-详情按钮权限',
    1,
    NOW(),
    1,
    NOW(),
    0
FROM dual
WHERE NOT EXISTS (
    SELECT 1
    FROM sys_menu
    WHERE deleted = 0
      AND parent_id = @health_message_menu_id
      AND permission = 'health:message:query'
);

SET @health_message_resend_button_id := (
    SELECT menu_id
    FROM sys_menu
    WHERE deleted = 0
      AND parent_id = @health_message_menu_id
      AND permission = 'health:message:resend'
    LIMIT 1
);

SET @health_message_resend_button_id := IFNULL(
    @health_message_resend_button_id,
    (SELECT IFNULL(MAX(menu_id), 0) + 1 FROM sys_menu)
);

INSERT INTO sys_menu (
    menu_id, menu_name, menu_type, router_name, parent_id, path, is_button,
    permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted
)
SELECT
    @health_message_resend_button_id,
    '消息重发',
    0,
    ' ',
    @health_message_menu_id,
    '',
    1,
    'health:message:resend',
    '{"title":"消息重发"}',
    1,
    '消息审计-人工重发与批量重发按钮权限',
    1,
    NOW(),
    1,
    NOW(),
    0
FROM dual
WHERE NOT EXISTS (
    SELECT 1
    FROM sys_menu
    WHERE deleted = 0
      AND parent_id = @health_message_menu_id
      AND permission = 'health:message:resend'
);

-- ----------------------------
-- 4. 指标模板管理菜单与按钮
-- ----------------------------
SET @health_indicator_template_menu_id := (
    SELECT menu_id
    FROM sys_menu
    WHERE deleted = 0
      AND parent_id = @health_root_menu_id
      AND path = '/health/indicatorTemplate'
    LIMIT 1
);

SET @health_indicator_template_menu_id := IFNULL(
    @health_indicator_template_menu_id,
    (SELECT IFNULL(MAX(menu_id), 0) + 1 FROM sys_menu)
);

INSERT INTO sys_menu (
    menu_id, menu_name, menu_type, router_name, parent_id, path, is_button,
    permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted
)
SELECT
    @health_indicator_template_menu_id,
    '指标模板管理',
    1,
    'HealthIndicatorTemplate',
    @health_root_menu_id,
    '/health/indicatorTemplate',
    0,
    'health:indicatorTemplate:list',
    '{"title":"指标模板管理","showParent":1}',
    1,
    '健康系统后台指标模板管理菜单',
    1,
    NOW(),
    1,
    NOW(),
    0
FROM dual
WHERE NOT EXISTS (
    SELECT 1
    FROM sys_menu
    WHERE deleted = 0
      AND parent_id = @health_root_menu_id
      AND path = '/health/indicatorTemplate'
);

SET @health_indicator_template_query_button_id := (
    SELECT menu_id
    FROM sys_menu
    WHERE deleted = 0
      AND parent_id = @health_indicator_template_menu_id
      AND permission = 'health:indicatorTemplate:query'
    LIMIT 1
);

SET @health_indicator_template_query_button_id := IFNULL(
    @health_indicator_template_query_button_id,
    (SELECT IFNULL(MAX(menu_id), 0) + 1 FROM sys_menu)
);

INSERT INTO sys_menu (
    menu_id, menu_name, menu_type, router_name, parent_id, path, is_button,
    permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted
)
SELECT
    @health_indicator_template_query_button_id,
    '模板详情',
    0,
    ' ',
    @health_indicator_template_menu_id,
    '',
    1,
    'health:indicatorTemplate:query',
    '{"title":"模板详情"}',
    1,
    '指标模板管理-详情按钮权限',
    1,
    NOW(),
    1,
    NOW(),
    0
FROM dual
WHERE NOT EXISTS (
    SELECT 1
    FROM sys_menu
    WHERE deleted = 0
      AND parent_id = @health_indicator_template_menu_id
      AND permission = 'health:indicatorTemplate:query'
);

SET @health_indicator_template_add_button_id := (
    SELECT menu_id
    FROM sys_menu
    WHERE deleted = 0
      AND parent_id = @health_indicator_template_menu_id
      AND permission = 'health:indicatorTemplate:add'
    LIMIT 1
);

SET @health_indicator_template_add_button_id := IFNULL(
    @health_indicator_template_add_button_id,
    (SELECT IFNULL(MAX(menu_id), 0) + 1 FROM sys_menu)
);

INSERT INTO sys_menu (
    menu_id, menu_name, menu_type, router_name, parent_id, path, is_button,
    permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted
)
SELECT
    @health_indicator_template_add_button_id,
    '模板新增',
    0,
    ' ',
    @health_indicator_template_menu_id,
    '',
    1,
    'health:indicatorTemplate:add',
    '{"title":"模板新增"}',
    1,
    '指标模板管理-新增按钮权限',
    1,
    NOW(),
    1,
    NOW(),
    0
FROM dual
WHERE NOT EXISTS (
    SELECT 1
    FROM sys_menu
    WHERE deleted = 0
      AND parent_id = @health_indicator_template_menu_id
      AND permission = 'health:indicatorTemplate:add'
);

SET @health_indicator_template_edit_button_id := (
    SELECT menu_id
    FROM sys_menu
    WHERE deleted = 0
      AND parent_id = @health_indicator_template_menu_id
      AND permission = 'health:indicatorTemplate:edit'
    LIMIT 1
);

SET @health_indicator_template_edit_button_id := IFNULL(
    @health_indicator_template_edit_button_id,
    (SELECT IFNULL(MAX(menu_id), 0) + 1 FROM sys_menu)
);

INSERT INTO sys_menu (
    menu_id, menu_name, menu_type, router_name, parent_id, path, is_button,
    permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted
)
SELECT
    @health_indicator_template_edit_button_id,
    '模板修改',
    0,
    ' ',
    @health_indicator_template_menu_id,
    '',
    1,
    'health:indicatorTemplate:edit',
    '{"title":"模板修改"}',
    1,
    '指标模板管理-修改按钮权限',
    1,
    NOW(),
    1,
    NOW(),
    0
FROM dual
WHERE NOT EXISTS (
    SELECT 1
    FROM sys_menu
    WHERE deleted = 0
      AND parent_id = @health_indicator_template_menu_id
      AND permission = 'health:indicatorTemplate:edit'
);

SET @health_indicator_template_remove_button_id := (
    SELECT menu_id
    FROM sys_menu
    WHERE deleted = 0
      AND parent_id = @health_indicator_template_menu_id
      AND permission = 'health:indicatorTemplate:remove'
    LIMIT 1
);

SET @health_indicator_template_remove_button_id := IFNULL(
    @health_indicator_template_remove_button_id,
    (SELECT IFNULL(MAX(menu_id), 0) + 1 FROM sys_menu)
);

INSERT INTO sys_menu (
    menu_id, menu_name, menu_type, router_name, parent_id, path, is_button,
    permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted
)
SELECT
    @health_indicator_template_remove_button_id,
    '模板删除',
    0,
    ' ',
    @health_indicator_template_menu_id,
    '',
    1,
    'health:indicatorTemplate:remove',
    '{"title":"模板删除"}',
    1,
    '指标模板管理-删除按钮权限',
    1,
    NOW(),
    1,
    NOW(),
    0
FROM dual
WHERE NOT EXISTS (
    SELECT 1
    FROM sys_menu
    WHERE deleted = 0
      AND parent_id = @health_indicator_template_menu_id
      AND permission = 'health:indicatorTemplate:remove'
);

-- ----------------------------
-- 5. 家庭共享管理菜单与按钮
-- ----------------------------
SET @health_family_share_menu_id := (
    SELECT menu_id
    FROM sys_menu
    WHERE deleted = 0
      AND parent_id = @health_root_menu_id
      AND path = '/health/familyShare'
    LIMIT 1
);

SET @health_family_share_menu_id := IFNULL(
    @health_family_share_menu_id,
    (SELECT IFNULL(MAX(menu_id), 0) + 1 FROM sys_menu)
);

INSERT INTO sys_menu (
    menu_id, menu_name, menu_type, router_name, parent_id, path, is_button,
    permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted
)
SELECT
    @health_family_share_menu_id,
    '家庭共享管理',
    1,
    'HealthFamilyShare',
    @health_root_menu_id,
    '/health/familyShare',
    0,
    'health:familyShare:list',
    '{"title":"家庭共享管理","showParent":1}',
    1,
    '健康系统后台家庭共享管理菜单',
    1,
    NOW(),
    1,
    NOW(),
    0
FROM dual
WHERE NOT EXISTS (
    SELECT 1
    FROM sys_menu
    WHERE deleted = 0
      AND parent_id = @health_root_menu_id
      AND path = '/health/familyShare'
);

SET @health_family_share_query_button_id := (
    SELECT menu_id
    FROM sys_menu
    WHERE deleted = 0
      AND parent_id = @health_family_share_menu_id
      AND permission = 'health:familyShare:query'
    LIMIT 1
);

SET @health_family_share_query_button_id := IFNULL(
    @health_family_share_query_button_id,
    (SELECT IFNULL(MAX(menu_id), 0) + 1 FROM sys_menu)
);

INSERT INTO sys_menu (
    menu_id, menu_name, menu_type, router_name, parent_id, path, is_button,
    permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted
)
SELECT
    @health_family_share_query_button_id,
    '共享详情',
    0,
    ' ',
    @health_family_share_menu_id,
    '',
    1,
    'health:familyShare:query',
    '{"title":"共享详情"}',
    1,
    '家庭共享管理-详情按钮权限',
    1,
    NOW(),
    1,
    NOW(),
    0
FROM dual
WHERE NOT EXISTS (
    SELECT 1
    FROM sys_menu
    WHERE deleted = 0
      AND parent_id = @health_family_share_menu_id
      AND permission = 'health:familyShare:query'
);

SET @health_family_share_remove_button_id := (
    SELECT menu_id
    FROM sys_menu
    WHERE deleted = 0
      AND parent_id = @health_family_share_menu_id
      AND permission = 'health:familyShare:remove'
    LIMIT 1
);

SET @health_family_share_remove_button_id := IFNULL(
    @health_family_share_remove_button_id,
    (SELECT IFNULL(MAX(menu_id), 0) + 1 FROM sys_menu)
);

INSERT INTO sys_menu (
    menu_id, menu_name, menu_type, router_name, parent_id, path, is_button,
    permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted
)
SELECT
    @health_family_share_remove_button_id,
    '共享移除',
    0,
    ' ',
    @health_family_share_menu_id,
    '',
    1,
    'health:familyShare:remove',
    '{"title":"共享移除"}',
    1,
    '家庭共享管理-移除协同与取消邀请按钮权限',
    1,
    NOW(),
    1,
    NOW(),
    0
FROM dual
WHERE NOT EXISTS (
    SELECT 1
    FROM sys_menu
    WHERE deleted = 0
      AND parent_id = @health_family_share_menu_id
      AND permission = 'health:familyShare:remove'
);

-- ----------------------------
-- 6. 用药历史与依从率管理菜单与按钮
-- ----------------------------
SET @health_medication_history_menu_id := (
    SELECT menu_id
    FROM sys_menu
    WHERE deleted = 0
      AND parent_id = @health_root_menu_id
      AND path = '/health/medicationHistory'
    LIMIT 1
);

SET @health_medication_history_menu_id := IFNULL(
    @health_medication_history_menu_id,
    (SELECT IFNULL(MAX(menu_id), 0) + 1 FROM sys_menu)
);

INSERT INTO sys_menu (
    menu_id, menu_name, menu_type, router_name, parent_id, path, is_button,
    permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted
)
SELECT
    @health_medication_history_menu_id,
    '用药历史与依从率',
    1,
    'HealthMedicationHistory',
    @health_root_menu_id,
    '/health/medicationHistory',
    0,
    'health:medicationHistory:list',
    '{"title":"用药历史与依从率","showParent":1}',
    1,
    '健康系统后台用药历史与依从率管理菜单',
    1,
    NOW(),
    1,
    NOW(),
    0
FROM dual
WHERE NOT EXISTS (
    SELECT 1
    FROM sys_menu
    WHERE deleted = 0
      AND parent_id = @health_root_menu_id
      AND path = '/health/medicationHistory'
);

SET @health_medication_history_query_button_id := (
    SELECT menu_id
    FROM sys_menu
    WHERE deleted = 0
      AND parent_id = @health_medication_history_menu_id
      AND permission = 'health:medicationHistory:query'
    LIMIT 1
);

SET @health_medication_history_query_button_id := IFNULL(
    @health_medication_history_query_button_id,
    (SELECT IFNULL(MAX(menu_id), 0) + 1 FROM sys_menu)
);

INSERT INTO sys_menu (
    menu_id, menu_name, menu_type, router_name, parent_id, path, is_button,
    permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted
)
SELECT
    @health_medication_history_query_button_id,
    '依从率查询',
    0,
    ' ',
    @health_medication_history_menu_id,
    '',
    1,
    'health:medicationHistory:query',
    '{"title":"依从率查询"}',
    1,
    '用药历史与依从率管理-统计与趋势查询按钮权限',
    1,
    NOW(),
    1,
    NOW(),
    0
FROM dual
WHERE NOT EXISTS (
    SELECT 1
    FROM sys_menu
    WHERE deleted = 0
      AND parent_id = @health_medication_history_menu_id
      AND permission = 'health:medicationHistory:query'
);

-- ----------------------------
-- 7. 首页运营任务管理菜单与按钮
-- ----------------------------
SET @health_operation_task_menu_id := (
    SELECT menu_id
    FROM sys_menu
    WHERE deleted = 0
      AND parent_id = @health_root_menu_id
      AND path = '/health/operationTask'
    LIMIT 1
);

SET @health_operation_task_menu_id := IFNULL(
    @health_operation_task_menu_id,
    (SELECT IFNULL(MAX(menu_id), 0) + 1 FROM sys_menu)
);

INSERT INTO sys_menu (
    menu_id, menu_name, menu_type, router_name, parent_id, path, is_button,
    permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted
)
SELECT
    @health_operation_task_menu_id,
    '首页运营任务管理',
    1,
    'HealthOperationTask',
    @health_root_menu_id,
    '/health/operationTask',
    0,
    'health:operationTask:list',
    '{"title":"首页运营任务管理","showParent":1}',
    1,
    '健康系统后台首页运营任务管理菜单',
    1,
    NOW(),
    1,
    NOW(),
    0
FROM dual
WHERE NOT EXISTS (
    SELECT 1
    FROM sys_menu
    WHERE deleted = 0
      AND parent_id = @health_root_menu_id
      AND path = '/health/operationTask'
);

SET @health_operation_task_query_button_id := (
    SELECT menu_id
    FROM sys_menu
    WHERE deleted = 0
      AND parent_id = @health_operation_task_menu_id
      AND permission = 'health:operationTask:query'
    LIMIT 1
);

SET @health_operation_task_query_button_id := IFNULL(
    @health_operation_task_query_button_id,
    (SELECT IFNULL(MAX(menu_id), 0) + 1 FROM sys_menu)
);

INSERT INTO sys_menu (
    menu_id, menu_name, menu_type, router_name, parent_id, path, is_button,
    permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted
)
SELECT
    @health_operation_task_query_button_id,
    '任务详情',
    0,
    ' ',
    @health_operation_task_menu_id,
    '',
    1,
    'health:operationTask:query',
    '{"title":"任务详情"}',
    1,
    '首页运营任务管理-详情按钮权限',
    1,
    NOW(),
    1,
    NOW(),
    0
FROM dual
WHERE NOT EXISTS (
    SELECT 1
    FROM sys_menu
    WHERE deleted = 0
      AND parent_id = @health_operation_task_menu_id
      AND permission = 'health:operationTask:query'
);

SET @health_operation_task_add_button_id := (
    SELECT menu_id
    FROM sys_menu
    WHERE deleted = 0
      AND parent_id = @health_operation_task_menu_id
      AND permission = 'health:operationTask:add'
    LIMIT 1
);

SET @health_operation_task_add_button_id := IFNULL(
    @health_operation_task_add_button_id,
    (SELECT IFNULL(MAX(menu_id), 0) + 1 FROM sys_menu)
);

INSERT INTO sys_menu (
    menu_id, menu_name, menu_type, router_name, parent_id, path, is_button,
    permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted
)
SELECT
    @health_operation_task_add_button_id,
    '任务新增',
    0,
    ' ',
    @health_operation_task_menu_id,
    '',
    1,
    'health:operationTask:add',
    '{"title":"任务新增"}',
    1,
    '首页运营任务管理-新增按钮权限',
    1,
    NOW(),
    1,
    NOW(),
    0
FROM dual
WHERE NOT EXISTS (
    SELECT 1
    FROM sys_menu
    WHERE deleted = 0
      AND parent_id = @health_operation_task_menu_id
      AND permission = 'health:operationTask:add'
);

SET @health_operation_task_edit_button_id := (
    SELECT menu_id
    FROM sys_menu
    WHERE deleted = 0
      AND parent_id = @health_operation_task_menu_id
      AND permission = 'health:operationTask:edit'
    LIMIT 1
);

SET @health_operation_task_edit_button_id := IFNULL(
    @health_operation_task_edit_button_id,
    (SELECT IFNULL(MAX(menu_id), 0) + 1 FROM sys_menu)
);

INSERT INTO sys_menu (
    menu_id, menu_name, menu_type, router_name, parent_id, path, is_button,
    permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted
)
SELECT
    @health_operation_task_edit_button_id,
    '任务修改',
    0,
    ' ',
    @health_operation_task_menu_id,
    '',
    1,
    'health:operationTask:edit',
    '{"title":"任务修改"}',
    1,
    '首页运营任务管理-修改按钮权限',
    1,
    NOW(),
    1,
    NOW(),
    0
FROM dual
WHERE NOT EXISTS (
    SELECT 1
    FROM sys_menu
    WHERE deleted = 0
      AND parent_id = @health_operation_task_menu_id
      AND permission = 'health:operationTask:edit'
);

SET @health_operation_task_remove_button_id := (
    SELECT menu_id
    FROM sys_menu
    WHERE deleted = 0
      AND parent_id = @health_operation_task_menu_id
      AND permission = 'health:operationTask:remove'
    LIMIT 1
);

SET @health_operation_task_remove_button_id := IFNULL(
    @health_operation_task_remove_button_id,
    (SELECT IFNULL(MAX(menu_id), 0) + 1 FROM sys_menu)
);

INSERT INTO sys_menu (
    menu_id, menu_name, menu_type, router_name, parent_id, path, is_button,
    permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted
)
SELECT
    @health_operation_task_remove_button_id,
    '任务删除',
    0,
    ' ',
    @health_operation_task_menu_id,
    '',
    1,
    'health:operationTask:remove',
    '{"title":"任务删除"}',
    1,
    '首页运营任务管理-删除按钮权限',
    1,
    NOW(),
    1,
    NOW(),
    0
FROM dual
WHERE NOT EXISTS (
    SELECT 1
    FROM sys_menu
    WHERE deleted = 0
      AND parent_id = @health_operation_task_menu_id
      AND permission = 'health:operationTask:remove'
);

-- ----------------------------
-- 8. 体检报告管理菜单与按钮
-- ----------------------------
SET @health_report_menu_id := (
    SELECT menu_id
    FROM sys_menu
    WHERE deleted = 0
      AND parent_id = @health_root_menu_id
      AND path = '/health/report'
    LIMIT 1
);

SET @health_report_menu_id := IFNULL(
    @health_report_menu_id,
    (SELECT IFNULL(MAX(menu_id), 0) + 1 FROM sys_menu)
);

INSERT INTO sys_menu (
    menu_id, menu_name, menu_type, router_name, parent_id, path, is_button,
    permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted
)
SELECT
    @health_report_menu_id,
    '体检报告管理',
    1,
    'HealthReport',
    @health_root_menu_id,
    '/health/report',
    0,
    'health:report:list',
    '{"title":"体检报告管理","showParent":1}',
    1,
    '健康系统后台体检报告管理菜单',
    1,
    NOW(),
    1,
    NOW(),
    0
FROM dual
WHERE NOT EXISTS (
    SELECT 1
    FROM sys_menu
    WHERE deleted = 0
      AND parent_id = @health_root_menu_id
      AND path = '/health/report'
);

SET @health_report_query_button_id := (
    SELECT menu_id
    FROM sys_menu
    WHERE deleted = 0
      AND parent_id = @health_report_menu_id
      AND permission = 'health:report:query'
    LIMIT 1
);

SET @health_report_query_button_id := IFNULL(
    @health_report_query_button_id,
    (SELECT IFNULL(MAX(menu_id), 0) + 1 FROM sys_menu)
);

INSERT INTO sys_menu (
    menu_id, menu_name, menu_type, router_name, parent_id, path, is_button,
    permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted
)
SELECT
    @health_report_query_button_id,
    '报告详情',
    0,
    ' ',
    @health_report_menu_id,
    '',
    1,
    'health:report:query',
    '{"title":"报告详情"}',
    1,
    '体检报告管理-详情按钮权限',
    1,
    NOW(),
    1,
    NOW(),
    0
FROM dual
WHERE NOT EXISTS (
    SELECT 1
    FROM sys_menu
    WHERE deleted = 0
      AND parent_id = @health_report_menu_id
      AND permission = 'health:report:query'
);

SET @health_report_ai_button_id := (
    SELECT menu_id
    FROM sys_menu
    WHERE deleted = 0
      AND parent_id = @health_report_menu_id
      AND permission = 'health:report:ai'
    LIMIT 1
);

SET @health_report_ai_button_id := IFNULL(
    @health_report_ai_button_id,
    (SELECT IFNULL(MAX(menu_id), 0) + 1 FROM sys_menu)
);

INSERT INTO sys_menu (
    menu_id, menu_name, menu_type, router_name, parent_id, path, is_button,
    permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted
)
SELECT
    @health_report_ai_button_id,
    'AI总结',
    0,
    ' ',
    @health_report_menu_id,
    '',
    1,
    'health:report:ai',
    '{"title":"AI总结"}',
    1,
    '体检报告管理-AI总结按钮权限',
    1,
    NOW(),
    1,
    NOW(),
    0
FROM dual
WHERE NOT EXISTS (
    SELECT 1
    FROM sys_menu
    WHERE deleted = 0
      AND parent_id = @health_report_menu_id
      AND permission = 'health:report:ai'
);

SET @health_report_remove_button_id := (
    SELECT menu_id
    FROM sys_menu
    WHERE deleted = 0
      AND parent_id = @health_report_menu_id
      AND permission = 'health:report:remove'
    LIMIT 1
);

SET @health_report_remove_button_id := IFNULL(
    @health_report_remove_button_id,
    (SELECT IFNULL(MAX(menu_id), 0) + 1 FROM sys_menu)
);

INSERT INTO sys_menu (
    menu_id, menu_name, menu_type, router_name, parent_id, path, is_button,
    permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted
)
SELECT
    @health_report_remove_button_id,
    '报告删除',
    0,
    ' ',
    @health_report_menu_id,
    '',
    1,
    'health:report:remove',
    '{"title":"报告删除"}',
    1,
    '体检报告管理-删除按钮权限',
    1,
    NOW(),
    1,
    NOW(),
    0
FROM dual
WHERE NOT EXISTS (
    SELECT 1
    FROM sys_menu
    WHERE deleted = 0
      AND parent_id = @health_report_menu_id
      AND permission = 'health:report:remove'
);

-- ----------------------------
-- 9. 给普通角色补齐演示权限
-- ----------------------------
--
-- 当前基础数据中：
-- 1. role_id = 1 通常是超级管理员，可天然查看全部菜单
-- 2. role_id = 2 是普通演示角色，适合本地联调时直接复用
--
-- 因此这里把健康系统相关菜单默认授权给 role_id = 2，
-- 方便测试环境一导脚本就能直接看到页面。

INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 2, @health_root_menu_id
FROM dual
WHERE NOT EXISTS (
    SELECT 1
    FROM sys_role_menu
    WHERE role_id = 2
      AND menu_id = @health_root_menu_id
);

INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 2, @health_drug_menu_id
FROM dual
WHERE NOT EXISTS (
    SELECT 1
    FROM sys_role_menu
    WHERE role_id = 2
      AND menu_id = @health_drug_menu_id
);

INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 2, @health_drug_query_button_id
FROM dual
WHERE NOT EXISTS (
    SELECT 1
    FROM sys_role_menu
    WHERE role_id = 2
      AND menu_id = @health_drug_query_button_id
);

INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 2, @health_drug_add_button_id
FROM dual
WHERE NOT EXISTS (
    SELECT 1
    FROM sys_role_menu
    WHERE role_id = 2
      AND menu_id = @health_drug_add_button_id
);

INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 2, @health_drug_edit_button_id
FROM dual
WHERE NOT EXISTS (
    SELECT 1
    FROM sys_role_menu
    WHERE role_id = 2
      AND menu_id = @health_drug_edit_button_id
);

INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 2, @health_drug_remove_button_id
FROM dual
WHERE NOT EXISTS (
    SELECT 1
    FROM sys_role_menu
    WHERE role_id = 2
      AND menu_id = @health_drug_remove_button_id
);

INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 2, @health_message_menu_id
FROM dual
WHERE NOT EXISTS (
    SELECT 1
    FROM sys_role_menu
    WHERE role_id = 2
      AND menu_id = @health_message_menu_id
);

INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 2, @health_message_query_button_id
FROM dual
WHERE NOT EXISTS (
    SELECT 1
    FROM sys_role_menu
    WHERE role_id = 2
      AND menu_id = @health_message_query_button_id
);

INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 2, @health_message_resend_button_id
FROM dual
WHERE NOT EXISTS (
    SELECT 1
    FROM sys_role_menu
    WHERE role_id = 2
      AND menu_id = @health_message_resend_button_id
);

INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 2, @health_indicator_template_menu_id
FROM dual
WHERE NOT EXISTS (
    SELECT 1
    FROM sys_role_menu
    WHERE role_id = 2
      AND menu_id = @health_indicator_template_menu_id
);

INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 2, @health_indicator_template_query_button_id
FROM dual
WHERE NOT EXISTS (
    SELECT 1
    FROM sys_role_menu
    WHERE role_id = 2
      AND menu_id = @health_indicator_template_query_button_id
);

INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 2, @health_indicator_template_add_button_id
FROM dual
WHERE NOT EXISTS (
    SELECT 1
    FROM sys_role_menu
    WHERE role_id = 2
      AND menu_id = @health_indicator_template_add_button_id
);

INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 2, @health_indicator_template_edit_button_id
FROM dual
WHERE NOT EXISTS (
    SELECT 1
    FROM sys_role_menu
    WHERE role_id = 2
      AND menu_id = @health_indicator_template_edit_button_id
);

INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 2, @health_indicator_template_remove_button_id
FROM dual
WHERE NOT EXISTS (
    SELECT 1
    FROM sys_role_menu
    WHERE role_id = 2
      AND menu_id = @health_indicator_template_remove_button_id
);

INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 2, @health_family_share_menu_id
FROM dual
WHERE NOT EXISTS (
    SELECT 1
    FROM sys_role_menu
    WHERE role_id = 2
      AND menu_id = @health_family_share_menu_id
);

INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 2, @health_family_share_query_button_id
FROM dual
WHERE NOT EXISTS (
    SELECT 1
    FROM sys_role_menu
    WHERE role_id = 2
      AND menu_id = @health_family_share_query_button_id
);

INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 2, @health_family_share_remove_button_id
FROM dual
WHERE NOT EXISTS (
    SELECT 1
    FROM sys_role_menu
    WHERE role_id = 2
      AND menu_id = @health_family_share_remove_button_id
);

INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 2, @health_medication_history_menu_id
FROM dual
WHERE NOT EXISTS (
    SELECT 1
    FROM sys_role_menu
    WHERE role_id = 2
      AND menu_id = @health_medication_history_menu_id
);

INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 2, @health_medication_history_query_button_id
FROM dual
WHERE NOT EXISTS (
    SELECT 1
    FROM sys_role_menu
    WHERE role_id = 2
      AND menu_id = @health_medication_history_query_button_id
);

INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 2, @health_operation_task_menu_id
FROM dual
WHERE NOT EXISTS (
    SELECT 1
    FROM sys_role_menu
    WHERE role_id = 2
      AND menu_id = @health_operation_task_menu_id
);

INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 2, @health_operation_task_query_button_id
FROM dual
WHERE NOT EXISTS (
    SELECT 1
    FROM sys_role_menu
    WHERE role_id = 2
      AND menu_id = @health_operation_task_query_button_id
);

INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 2, @health_operation_task_add_button_id
FROM dual
WHERE NOT EXISTS (
    SELECT 1
    FROM sys_role_menu
    WHERE role_id = 2
      AND menu_id = @health_operation_task_add_button_id
);

INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 2, @health_operation_task_edit_button_id
FROM dual
WHERE NOT EXISTS (
    SELECT 1
    FROM sys_role_menu
    WHERE role_id = 2
      AND menu_id = @health_operation_task_edit_button_id
);

INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 2, @health_operation_task_remove_button_id
FROM dual
WHERE NOT EXISTS (
    SELECT 1
    FROM sys_role_menu
    WHERE role_id = 2
      AND menu_id = @health_operation_task_remove_button_id
);

INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 2, @health_report_menu_id
FROM dual
WHERE NOT EXISTS (
    SELECT 1
    FROM sys_role_menu
    WHERE role_id = 2
      AND menu_id = @health_report_menu_id
);

INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 2, @health_report_query_button_id
FROM dual
WHERE NOT EXISTS (
    SELECT 1
    FROM sys_role_menu
    WHERE role_id = 2
      AND menu_id = @health_report_query_button_id
);

INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 2, @health_report_ai_button_id
FROM dual
WHERE NOT EXISTS (
    SELECT 1
    FROM sys_role_menu
    WHERE role_id = 2
      AND menu_id = @health_report_ai_button_id
);

INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 2, @health_report_remove_button_id
FROM dual
WHERE NOT EXISTS (
    SELECT 1
    FROM sys_role_menu
    WHERE role_id = 2
      AND menu_id = @health_report_remove_button_id
);

-- 执行完成后建议验证：
-- 1. sys_menu 中是否已出现 /health、/health/drug、/health/message、/health/indicatorTemplate、
--    /health/familyShare、/health/medicationHistory、/health/operationTask、/health/report
-- 2. sys_role_menu 中 role_id = 2 是否已关联上述菜单与按钮
-- 3. 后台重新登录后，左侧菜单是否出现“健康系统”
