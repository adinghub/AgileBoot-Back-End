-- 慢病病种配置后台菜单权限
--
-- 本增量只补后台维护入口，不改变 App 慢病专项数据结构。
-- 新增病种后，App 通过既有慢病病种清单读取启用配置。

INSERT INTO sys_menu (
    menu_name, menu_type, router_name, parent_id, path, is_button, permission,
    meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted
)
SELECT
    '慢病病种配置', 1, 'HealthChronicDiseaseType', root.menu_id, '/health/chronicDiseaseType', FALSE,
    'health:chronicDiseaseType:list', '{"title":"慢病病种配置","showParent":1}',
    1, '健康系统后台慢病病种配置菜单', 1, NOW(), 1, NOW(), 0
FROM sys_menu root
WHERE root.deleted = 0
  AND root.path = '/health'
  AND NOT EXISTS (
      SELECT 1
      FROM sys_menu menu
      WHERE menu.deleted = 0
        AND menu.parent_id = root.menu_id
        AND menu.path = '/health/chronicDiseaseType'
  );

INSERT INTO sys_menu (
    menu_name, menu_type, router_name, parent_id, path, is_button, permission,
    meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted
)
SELECT
    '病种详情', 0, ' ', menu.menu_id, '', TRUE, 'health:chronicDiseaseType:query',
    '{"title":"病种详情"}', 1, '慢病病种配置-详情按钮权限', 1, NOW(), 1, NOW(), 0
FROM sys_menu menu
WHERE menu.deleted = 0
  AND menu.path = '/health/chronicDiseaseType'
  AND NOT EXISTS (
      SELECT 1
      FROM sys_menu btn
      WHERE btn.deleted = 0
        AND btn.parent_id = menu.menu_id
        AND btn.permission = 'health:chronicDiseaseType:query'
  );

INSERT INTO sys_menu (
    menu_name, menu_type, router_name, parent_id, path, is_button, permission,
    meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted
)
SELECT
    '新增病种', 0, ' ', menu.menu_id, '', TRUE, 'health:chronicDiseaseType:add',
    '{"title":"新增病种"}', 1, '慢病病种配置-新增按钮权限', 1, NOW(), 1, NOW(), 0
FROM sys_menu menu
WHERE menu.deleted = 0
  AND menu.path = '/health/chronicDiseaseType'
  AND NOT EXISTS (
      SELECT 1
      FROM sys_menu btn
      WHERE btn.deleted = 0
        AND btn.parent_id = menu.menu_id
        AND btn.permission = 'health:chronicDiseaseType:add'
  );

INSERT INTO sys_menu (
    menu_name, menu_type, router_name, parent_id, path, is_button, permission,
    meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted
)
SELECT
    '修改病种', 0, ' ', menu.menu_id, '', TRUE, 'health:chronicDiseaseType:edit',
    '{"title":"修改病种"}', 1, '慢病病种配置-修改按钮权限', 1, NOW(), 1, NOW(), 0
FROM sys_menu menu
WHERE menu.deleted = 0
  AND menu.path = '/health/chronicDiseaseType'
  AND NOT EXISTS (
      SELECT 1
      FROM sys_menu btn
      WHERE btn.deleted = 0
        AND btn.parent_id = menu.menu_id
        AND btn.permission = 'health:chronicDiseaseType:edit'
  );

INSERT INTO sys_menu (
    menu_name, menu_type, router_name, parent_id, path, is_button, permission,
    meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted
)
SELECT
    '删除病种', 0, ' ', menu.menu_id, '', TRUE, 'health:chronicDiseaseType:remove',
    '{"title":"删除病种"}', 1, '慢病病种配置-删除按钮权限', 1, NOW(), 1, NOW(), 0
FROM sys_menu menu
WHERE menu.deleted = 0
  AND menu.path = '/health/chronicDiseaseType'
  AND NOT EXISTS (
      SELECT 1
      FROM sys_menu btn
      WHERE btn.deleted = 0
        AND btn.parent_id = menu.menu_id
        AND btn.permission = 'health:chronicDiseaseType:remove'
  );

INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 2, menu.menu_id
FROM sys_menu menu
WHERE menu.deleted = 0
  AND (
      menu.path = '/health/chronicDiseaseType'
      OR menu.permission IN (
          'health:chronicDiseaseType:query',
          'health:chronicDiseaseType:add',
          'health:chronicDiseaseType:edit',
          'health:chronicDiseaseType:remove'
      )
  )
  AND NOT EXISTS (
      SELECT 1
      FROM sys_role_menu role_menu
      WHERE role_menu.role_id = 2
        AND role_menu.menu_id = menu.menu_id
  );