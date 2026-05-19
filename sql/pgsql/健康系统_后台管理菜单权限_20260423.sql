
-- 健康系统后台菜单与按钮权限 PostgreSQL 初始化脚本
--
-- 说明：
-- 1. 本脚本按 PostgreSQL 语法编写，依赖基础库 sys_menu / sys_role_menu 已存在
-- 2. 菜单插入采用自然键幂等控制，不再依赖 MySQL 变量
-- 3. 默认给 role_id = 2 的演示角色授权，便于新环境联调

INSERT INTO sys_menu (
    menu_name, menu_type, router_name, parent_id, path, is_button, permission,
    meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted
)
SELECT
    '健康系统', 2, '', 0, '/health', FALSE, '',
    '{"title":"健康系统","icon":"ep:first-aid-kit","showParent":1,"rank":10}',
    1, '健康系统后台目录菜单', 1, NOW(), 1, NOW(), 0
WHERE NOT EXISTS (
    SELECT 1
    FROM sys_menu
    WHERE deleted = 0
      AND parent_id = 0
      AND path = '/health'
);

INSERT INTO sys_menu (
    menu_name, menu_type, router_name, parent_id, path, is_button, permission,
    meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted
)
SELECT
    '系统药品管理', 1, 'HealthDrug', root.menu_id, '/health/drug', FALSE, 'health:drug:list',
    '{"title":"系统药品管理","showParent":1}', 1, '健康系统后台系统药品管理菜单', 1, NOW(), 1, NOW(), 0
FROM sys_menu root
WHERE root.deleted = 0
  AND root.parent_id = 0
  AND root.path = '/health'
  AND NOT EXISTS (
      SELECT 1
      FROM sys_menu menu
      WHERE menu.deleted = 0
        AND menu.parent_id = root.menu_id
        AND menu.path = '/health/drug'
  );

INSERT INTO sys_menu (
    menu_name, menu_type, router_name, parent_id, path, is_button, permission,
    meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted
)
SELECT
    '药品详情', 0, ' ', menu.menu_id, '', TRUE, 'health:drug:query',
    '{"title":"药品详情"}', 1, '系统药品管理-详情按钮权限', 1, NOW(), 1, NOW(), 0
FROM sys_menu menu
WHERE menu.deleted = 0
  AND menu.path = '/health/drug'
  AND NOT EXISTS (
      SELECT 1
      FROM sys_menu btn
      WHERE btn.deleted = 0
        AND btn.parent_id = menu.menu_id
        AND btn.permission = 'health:drug:query'
  );

INSERT INTO sys_menu (
    menu_name, menu_type, router_name, parent_id, path, is_button, permission,
    meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted
)
SELECT
    '药品新增', 0, ' ', menu.menu_id, '', TRUE, 'health:drug:add',
    '{"title":"药品新增"}', 1, '系统药品管理-新增按钮权限', 1, NOW(), 1, NOW(), 0
FROM sys_menu menu
WHERE menu.deleted = 0
  AND menu.path = '/health/drug'
  AND NOT EXISTS (
      SELECT 1
      FROM sys_menu btn
      WHERE btn.deleted = 0
        AND btn.parent_id = menu.menu_id
        AND btn.permission = 'health:drug:add'
  );

INSERT INTO sys_menu (
    menu_name, menu_type, router_name, parent_id, path, is_button, permission,
    meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted
)
SELECT
    '药品修改', 0, ' ', menu.menu_id, '', TRUE, 'health:drug:edit',
    '{"title":"药品修改"}', 1, '系统药品管理-修改按钮权限', 1, NOW(), 1, NOW(), 0
FROM sys_menu menu
WHERE menu.deleted = 0
  AND menu.path = '/health/drug'
  AND NOT EXISTS (
      SELECT 1
      FROM sys_menu btn
      WHERE btn.deleted = 0
        AND btn.parent_id = menu.menu_id
        AND btn.permission = 'health:drug:edit'
  );

INSERT INTO sys_menu (
    menu_name, menu_type, router_name, parent_id, path, is_button, permission,
    meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted
)
SELECT
    '药品删除', 0, ' ', menu.menu_id, '', TRUE, 'health:drug:remove',
    '{"title":"药品删除"}', 1, '系统药品管理-删除按钮权限', 1, NOW(), 1, NOW(), 0
FROM sys_menu menu
WHERE menu.deleted = 0
  AND menu.path = '/health/drug'
  AND NOT EXISTS (
      SELECT 1
      FROM sys_menu btn
      WHERE btn.deleted = 0
        AND btn.parent_id = menu.menu_id
        AND btn.permission = 'health:drug:remove'
  );

INSERT INTO sys_menu (
    menu_name, menu_type, router_name, parent_id, path, is_button, permission,
    meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted
)
SELECT
    '药品单位管理', 1, 'DrugUnit', root.menu_id, '/health/drugUnit', FALSE, 'health:drugUnit:list',
    '{"title":"药品单位管理","showParent":1}', 1, '健康系统后台药品单位管理菜单', 1, NOW(), 1, NOW(), 0
FROM sys_menu root
WHERE root.deleted = 0
  AND root.parent_id = 0
  AND root.path = '/health'
  AND NOT EXISTS (
      SELECT 1
      FROM sys_menu menu
      WHERE menu.deleted = 0
        AND menu.parent_id = root.menu_id
        AND menu.path = '/health/drugUnit'
  );

INSERT INTO sys_menu (
    menu_name, menu_type, router_name, parent_id, path, is_button, permission,
    meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted
)
SELECT
    '单位详情', 0, ' ', menu.menu_id, '', TRUE, 'health:drugUnit:query',
    '{"title":"单位详情"}', 1, '药品单位管理-详情按钮权限', 1, NOW(), 1, NOW(), 0
FROM sys_menu menu
WHERE menu.deleted = 0
  AND menu.path = '/health/drugUnit'
  AND NOT EXISTS (
      SELECT 1
      FROM sys_menu btn
      WHERE btn.deleted = 0
        AND btn.parent_id = menu.menu_id
        AND btn.permission = 'health:drugUnit:query'
  );

INSERT INTO sys_menu (
    menu_name, menu_type, router_name, parent_id, path, is_button, permission,
    meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted
)
SELECT
    '单位新增', 0, ' ', menu.menu_id, '', TRUE, 'health:drugUnit:add',
    '{"title":"单位新增"}', 1, '药品单位管理-新增按钮权限', 1, NOW(), 1, NOW(), 0
FROM sys_menu menu
WHERE menu.deleted = 0
  AND menu.path = '/health/drugUnit'
  AND NOT EXISTS (
      SELECT 1
      FROM sys_menu btn
      WHERE btn.deleted = 0
        AND btn.parent_id = menu.menu_id
        AND btn.permission = 'health:drugUnit:add'
  );

INSERT INTO sys_menu (
    menu_name, menu_type, router_name, parent_id, path, is_button, permission,
    meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted
)
SELECT
    '单位修改', 0, ' ', menu.menu_id, '', TRUE, 'health:drugUnit:edit',
    '{"title":"单位修改"}', 1, '药品单位管理-修改按钮权限', 1, NOW(), 1, NOW(), 0
FROM sys_menu menu
WHERE menu.deleted = 0
  AND menu.path = '/health/drugUnit'
  AND NOT EXISTS (
      SELECT 1
      FROM sys_menu btn
      WHERE btn.deleted = 0
        AND btn.parent_id = menu.menu_id
        AND btn.permission = 'health:drugUnit:edit'
  );

INSERT INTO sys_menu (
    menu_name, menu_type, router_name, parent_id, path, is_button, permission,
    meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted
)
SELECT
    '单位删除', 0, ' ', menu.menu_id, '', TRUE, 'health:drugUnit:remove',
    '{"title":"单位删除"}', 1, '药品单位管理-删除按钮权限', 1, NOW(), 1, NOW(), 0
FROM sys_menu menu
WHERE menu.deleted = 0
  AND menu.path = '/health/drugUnit'
  AND NOT EXISTS (
      SELECT 1
      FROM sys_menu btn
      WHERE btn.deleted = 0
        AND btn.parent_id = menu.menu_id
        AND btn.permission = 'health:drugUnit:remove'
  );

INSERT INTO sys_menu (
    menu_name, menu_type, router_name, parent_id, path, is_button, permission,
    meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted
)
SELECT
    '消息审计', 1, 'HealthMessage', root.menu_id, '/health/message', FALSE, 'health:message:list',
    '{"title":"消息审计","showParent":1}', 1, '健康系统后台消息审计菜单', 1, NOW(), 1, NOW(), 0
FROM sys_menu root
WHERE root.deleted = 0
  AND root.path = '/health'
  AND NOT EXISTS (
      SELECT 1
      FROM sys_menu menu
      WHERE menu.deleted = 0
        AND menu.parent_id = root.menu_id
        AND menu.path = '/health/message'
  );

INSERT INTO sys_menu (
    menu_name, menu_type, router_name, parent_id, path, is_button, permission,
    meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted
)
SELECT
    '消息详情', 0, ' ', menu.menu_id, '', TRUE, 'health:message:query',
    '{"title":"消息详情"}', 1, '消息审计-详情按钮权限', 1, NOW(), 1, NOW(), 0
FROM sys_menu menu
WHERE menu.deleted = 0
  AND menu.path = '/health/message'
  AND NOT EXISTS (
      SELECT 1
      FROM sys_menu btn
      WHERE btn.deleted = 0
        AND btn.parent_id = menu.menu_id
        AND btn.permission = 'health:message:query'
  );

INSERT INTO sys_menu (
    menu_name, menu_type, router_name, parent_id, path, is_button, permission,
    meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted
)
SELECT
    '消息重发', 0, ' ', menu.menu_id, '', TRUE, 'health:message:resend',
    '{"title":"消息重发"}', 1, '消息审计-人工重发与批量重发按钮权限', 1, NOW(), 1, NOW(), 0
FROM sys_menu menu
WHERE menu.deleted = 0
  AND menu.path = '/health/message'
  AND NOT EXISTS (
      SELECT 1
      FROM sys_menu btn
      WHERE btn.deleted = 0
        AND btn.parent_id = menu.menu_id
        AND btn.permission = 'health:message:resend'
  );

INSERT INTO sys_menu (
    menu_name, menu_type, router_name, parent_id, path, is_button, permission,
    meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted
)
SELECT
    '指标模板管理', 1, 'HealthIndicatorTemplate', root.menu_id, '/health/indicatorTemplate', FALSE,
    'health:indicatorTemplate:list', '{"title":"指标模板管理","showParent":1}',
    1, '健康系统后台指标模板管理菜单', 1, NOW(), 1, NOW(), 0
FROM sys_menu root
WHERE root.deleted = 0
  AND root.path = '/health'
  AND NOT EXISTS (
      SELECT 1
      FROM sys_menu menu
      WHERE menu.deleted = 0
        AND menu.parent_id = root.menu_id
        AND menu.path = '/health/indicatorTemplate'
  );

INSERT INTO sys_menu (
    menu_name, menu_type, router_name, parent_id, path, is_button, permission,
    meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted
)
SELECT
    '模板详情', 0, ' ', menu.menu_id, '', TRUE, 'health:indicatorTemplate:query',
    '{"title":"模板详情"}', 1, '指标模板管理-详情按钮权限', 1, NOW(), 1, NOW(), 0
FROM sys_menu menu
WHERE menu.deleted = 0
  AND menu.path = '/health/indicatorTemplate'
  AND NOT EXISTS (
      SELECT 1
      FROM sys_menu btn
      WHERE btn.deleted = 0
        AND btn.parent_id = menu.menu_id
        AND btn.permission = 'health:indicatorTemplate:query'
  );

INSERT INTO sys_menu (
    menu_name, menu_type, router_name, parent_id, path, is_button, permission,
    meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted
)
SELECT
    '模板新增', 0, ' ', menu.menu_id, '', TRUE, 'health:indicatorTemplate:add',
    '{"title":"模板新增"}', 1, '指标模板管理-新增按钮权限', 1, NOW(), 1, NOW(), 0
FROM sys_menu menu
WHERE menu.deleted = 0
  AND menu.path = '/health/indicatorTemplate'
  AND NOT EXISTS (
      SELECT 1
      FROM sys_menu btn
      WHERE btn.deleted = 0
        AND btn.parent_id = menu.menu_id
        AND btn.permission = 'health:indicatorTemplate:add'
  );

INSERT INTO sys_menu (
    menu_name, menu_type, router_name, parent_id, path, is_button, permission,
    meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted
)
SELECT
    '模板修改', 0, ' ', menu.menu_id, '', TRUE, 'health:indicatorTemplate:edit',
    '{"title":"模板修改"}', 1, '指标模板管理-修改按钮权限', 1, NOW(), 1, NOW(), 0
FROM sys_menu menu
WHERE menu.deleted = 0
  AND menu.path = '/health/indicatorTemplate'
  AND NOT EXISTS (
      SELECT 1
      FROM sys_menu btn
      WHERE btn.deleted = 0
        AND btn.parent_id = menu.menu_id
        AND btn.permission = 'health:indicatorTemplate:edit'
  );

INSERT INTO sys_menu (
    menu_name, menu_type, router_name, parent_id, path, is_button, permission,
    meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted
)
SELECT
    '模板删除', 0, ' ', menu.menu_id, '', TRUE, 'health:indicatorTemplate:remove',
    '{"title":"模板删除"}', 1, '指标模板管理-删除按钮权限', 1, NOW(), 1, NOW(), 0
FROM sys_menu menu
WHERE menu.deleted = 0
  AND menu.path = '/health/indicatorTemplate'
  AND NOT EXISTS (
      SELECT 1
      FROM sys_menu btn
      WHERE btn.deleted = 0
        AND btn.parent_id = menu.menu_id
        AND btn.permission = 'health:indicatorTemplate:remove'
  );

INSERT INTO sys_menu (
    menu_name, menu_type, router_name, parent_id, path, is_button, permission,
    meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted
)
SELECT
    '家庭共享管理', 1, 'HealthFamilyShare', root.menu_id, '/health/familyShare', FALSE,
    'health:familyShare:list', '{"title":"家庭共享管理","showParent":1}',
    1, '健康系统后台家庭共享管理菜单', 1, NOW(), 1, NOW(), 0
FROM sys_menu root
WHERE root.deleted = 0
  AND root.path = '/health'
  AND NOT EXISTS (
      SELECT 1
      FROM sys_menu menu
      WHERE menu.deleted = 0
        AND menu.parent_id = root.menu_id
        AND menu.path = '/health/familyShare'
  );

INSERT INTO sys_menu (
    menu_name, menu_type, router_name, parent_id, path, is_button, permission,
    meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted
)
SELECT
    '共享详情', 0, ' ', menu.menu_id, '', TRUE, 'health:familyShare:query',
    '{"title":"共享详情"}', 1, '家庭共享管理-详情按钮权限', 1, NOW(), 1, NOW(), 0
FROM sys_menu menu
WHERE menu.deleted = 0
  AND menu.path = '/health/familyShare'
  AND NOT EXISTS (
      SELECT 1
      FROM sys_menu btn
      WHERE btn.deleted = 0
        AND btn.parent_id = menu.menu_id
        AND btn.permission = 'health:familyShare:query'
  );

INSERT INTO sys_menu (
    menu_name, menu_type, router_name, parent_id, path, is_button, permission,
    meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted
)
SELECT
    '共享移除', 0, ' ', menu.menu_id, '', TRUE, 'health:familyShare:remove',
    '{"title":"共享移除"}', 1, '家庭共享管理-移除协同与取消邀请按钮权限', 1, NOW(), 1, NOW(), 0
FROM sys_menu menu
WHERE menu.deleted = 0
  AND menu.path = '/health/familyShare'
  AND NOT EXISTS (
      SELECT 1
      FROM sys_menu btn
      WHERE btn.deleted = 0
        AND btn.parent_id = menu.menu_id
        AND btn.permission = 'health:familyShare:remove'
  );

INSERT INTO sys_menu (
    menu_name, menu_type, router_name, parent_id, path, is_button, permission,
    meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted
)
SELECT
    '用药历史与依从率', 1, 'HealthMedicationHistory', root.menu_id, '/health/medicationHistory', FALSE,
    'health:medicationHistory:list', '{"title":"用药历史与依从率","showParent":1}',
    1, '健康系统后台用药历史与依从率管理菜单', 1, NOW(), 1, NOW(), 0
FROM sys_menu root
WHERE root.deleted = 0
  AND root.path = '/health'
  AND NOT EXISTS (
      SELECT 1
      FROM sys_menu menu
      WHERE menu.deleted = 0
        AND menu.parent_id = root.menu_id
        AND menu.path = '/health/medicationHistory'
  );

INSERT INTO sys_menu (
    menu_name, menu_type, router_name, parent_id, path, is_button, permission,
    meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted
)
SELECT
    '依从率查询', 0, ' ', menu.menu_id, '', TRUE, 'health:medicationHistory:query',
    '{"title":"依从率查询"}', 1, '用药历史与依从率管理-统计与趋势查询按钮权限', 1, NOW(), 1, NOW(), 0
FROM sys_menu menu
WHERE menu.deleted = 0
  AND menu.path = '/health/medicationHistory'
  AND NOT EXISTS (
      SELECT 1
      FROM sys_menu btn
      WHERE btn.deleted = 0
        AND btn.parent_id = menu.menu_id
        AND btn.permission = 'health:medicationHistory:query'
  );

INSERT INTO sys_menu (
    menu_name, menu_type, router_name, parent_id, path, is_button, permission,
    meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted
)
SELECT
    '首页运营任务管理', 1, 'HealthOperationTask', root.menu_id, '/health/operationTask', FALSE,
    'health:operationTask:list', '{"title":"首页运营任务管理","showParent":1}',
    1, '健康系统后台首页运营任务管理菜单', 1, NOW(), 1, NOW(), 0
FROM sys_menu root
WHERE root.deleted = 0
  AND root.path = '/health'
  AND NOT EXISTS (
      SELECT 1
      FROM sys_menu menu
      WHERE menu.deleted = 0
        AND menu.parent_id = root.menu_id
        AND menu.path = '/health/operationTask'
  );

INSERT INTO sys_menu (
    menu_name, menu_type, router_name, parent_id, path, is_button, permission,
    meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted
)
SELECT
    '任务详情', 0, ' ', menu.menu_id, '', TRUE, 'health:operationTask:query',
    '{"title":"任务详情"}', 1, '首页运营任务管理-详情按钮权限', 1, NOW(), 1, NOW(), 0
FROM sys_menu menu
WHERE menu.deleted = 0
  AND menu.path = '/health/operationTask'
  AND NOT EXISTS (
      SELECT 1
      FROM sys_menu btn
      WHERE btn.deleted = 0
        AND btn.parent_id = menu.menu_id
        AND btn.permission = 'health:operationTask:query'
  );

INSERT INTO sys_menu (
    menu_name, menu_type, router_name, parent_id, path, is_button, permission,
    meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted
)
SELECT
    '任务新增', 0, ' ', menu.menu_id, '', TRUE, 'health:operationTask:add',
    '{"title":"任务新增"}', 1, '首页运营任务管理-新增按钮权限', 1, NOW(), 1, NOW(), 0
FROM sys_menu menu
WHERE menu.deleted = 0
  AND menu.path = '/health/operationTask'
  AND NOT EXISTS (
      SELECT 1
      FROM sys_menu btn
      WHERE btn.deleted = 0
        AND btn.parent_id = menu.menu_id
        AND btn.permission = 'health:operationTask:add'
  );

INSERT INTO sys_menu (
    menu_name, menu_type, router_name, parent_id, path, is_button, permission,
    meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted
)
SELECT
    '任务修改', 0, ' ', menu.menu_id, '', TRUE, 'health:operationTask:edit',
    '{"title":"任务修改"}', 1, '首页运营任务管理-修改按钮权限', 1, NOW(), 1, NOW(), 0
FROM sys_menu menu
WHERE menu.deleted = 0
  AND menu.path = '/health/operationTask'
  AND NOT EXISTS (
      SELECT 1
      FROM sys_menu btn
      WHERE btn.deleted = 0
        AND btn.parent_id = menu.menu_id
        AND btn.permission = 'health:operationTask:edit'
  );

INSERT INTO sys_menu (
    menu_name, menu_type, router_name, parent_id, path, is_button, permission,
    meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted
)
SELECT
    '任务删除', 0, ' ', menu.menu_id, '', TRUE, 'health:operationTask:remove',
    '{"title":"任务删除"}', 1, '首页运营任务管理-删除按钮权限', 1, NOW(), 1, NOW(), 0
FROM sys_menu menu
WHERE menu.deleted = 0
  AND menu.path = '/health/operationTask'
  AND NOT EXISTS (
      SELECT 1
      FROM sys_menu btn
      WHERE btn.deleted = 0
        AND btn.parent_id = menu.menu_id
        AND btn.permission = 'health:operationTask:remove'
  );

INSERT INTO sys_menu (
    menu_name, menu_type, router_name, parent_id, path, is_button, permission,
    meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted
)
SELECT
    '体检报告管理', 1, 'HealthReport', root.menu_id, '/health/report', FALSE, 'health:report:list',
    '{"title":"体检报告管理","showParent":1}', 1, '健康系统后台体检报告管理菜单', 1, NOW(), 1, NOW(), 0
FROM sys_menu root
WHERE root.deleted = 0
  AND root.path = '/health'
  AND NOT EXISTS (
      SELECT 1
      FROM sys_menu menu
      WHERE menu.deleted = 0
        AND menu.parent_id = root.menu_id
        AND menu.path = '/health/report'
  );

INSERT INTO sys_menu (
    menu_name, menu_type, router_name, parent_id, path, is_button, permission,
    meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted
)
SELECT
    '报告详情', 0, ' ', menu.menu_id, '', TRUE, 'health:report:query',
    '{"title":"报告详情"}', 1, '体检报告管理-详情按钮权限', 1, NOW(), 1, NOW(), 0
FROM sys_menu menu
WHERE menu.deleted = 0
  AND menu.path = '/health/report'
  AND NOT EXISTS (
      SELECT 1
      FROM sys_menu btn
      WHERE btn.deleted = 0
        AND btn.parent_id = menu.menu_id
        AND btn.permission = 'health:report:query'
  );

INSERT INTO sys_menu (
    menu_name, menu_type, router_name, parent_id, path, is_button, permission,
    meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted
)
SELECT
    'AI总结', 0, ' ', menu.menu_id, '', TRUE, 'health:report:ai',
    '{"title":"AI总结"}', 1, '体检报告管理-AI总结按钮权限', 1, NOW(), 1, NOW(), 0
FROM sys_menu menu
WHERE menu.deleted = 0
  AND menu.path = '/health/report'
  AND NOT EXISTS (
      SELECT 1
      FROM sys_menu btn
      WHERE btn.deleted = 0
        AND btn.parent_id = menu.menu_id
        AND btn.permission = 'health:report:ai'
  );

INSERT INTO sys_menu (
    menu_name, menu_type, router_name, parent_id, path, is_button, permission,
    meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted
)
SELECT
    '报告删除', 0, ' ', menu.menu_id, '', TRUE, 'health:report:remove',
    '{"title":"报告删除"}', 1, '体检报告管理-删除按钮权限', 1, NOW(), 1, NOW(), 0
FROM sys_menu menu
WHERE menu.deleted = 0
  AND menu.path = '/health/report'
  AND NOT EXISTS (
      SELECT 1
      FROM sys_menu btn
      WHERE btn.deleted = 0
        AND btn.parent_id = menu.menu_id
        AND btn.permission = 'health:report:remove'
  );

INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 2, menu.menu_id
FROM sys_menu menu
WHERE menu.deleted = 0
  AND (
      menu.path IN (
          '/health',
          '/health/drug',
          '/health/drugUnit',
          '/health/message',
          '/health/indicatorTemplate',
          '/health/familyShare',
          '/health/medicationHistory',
          '/health/operationTask',
          '/health/report'
      )
      OR menu.permission IN (
          'health:drug:query',
          'health:drug:add',
          'health:drug:edit',
          'health:drug:remove',
          'health:drugUnit:query',
          'health:drugUnit:add',
          'health:drugUnit:edit',
          'health:drugUnit:remove',
          'health:message:query',
          'health:message:resend',
          'health:indicatorTemplate:query',
          'health:indicatorTemplate:add',
          'health:indicatorTemplate:edit',
          'health:indicatorTemplate:remove',
          'health:familyShare:query',
          'health:familyShare:remove',
          'health:medicationHistory:query',
          'health:operationTask:query',
          'health:operationTask:add',
          'health:operationTask:edit',
          'health:operationTask:remove',
          'health:report:query',
          'health:report:ai',
          'health:report:remove'
      )
  )
  AND NOT EXISTS (
      SELECT 1
      FROM sys_role_menu role_menu
      WHERE role_menu.role_id = 2
        AND role_menu.menu_id = menu.menu_id
  );
