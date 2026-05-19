-- 健康系统后台 Push 设备管理与派发审计菜单权限 PostgreSQL 增量脚本
--
-- 说明：
-- 1. 复用健康系统根菜单 /health
-- 2. 新增两个后台菜单：设备管理、Push派发审计
-- 3. 默认继续给 role_id = 2 的演示角色授权，方便联调

INSERT INTO sys_menu (
    menu_name, menu_type, router_name, parent_id, path, is_button, permission,
    meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted
)
SELECT
    'App设备管理', 1, 'HealthPushDevice', root.menu_id, '/health/push/device', FALSE, 'health:push:device:list',
    '{"title":"App设备管理","showParent":1}', 1, '健康系统后台 App 设备管理菜单', 1, NOW(), 1, NOW(), 0
FROM sys_menu root
WHERE root.deleted = 0
  AND root.path = '/health'
  AND NOT EXISTS (
      SELECT 1
      FROM sys_menu menu
      WHERE menu.deleted = 0
        AND menu.parent_id = root.menu_id
        AND menu.path = '/health/push/device'
  );

INSERT INTO sys_menu (
    menu_name, menu_type, router_name, parent_id, path, is_button, permission,
    meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted
)
SELECT
    '设备详情', 0, ' ', menu.menu_id, '', TRUE, 'health:push:device:detail',
    '{"title":"设备详情"}', 1, 'App设备管理-详情按钮权限', 1, NOW(), 1, NOW(), 0
FROM sys_menu menu
WHERE menu.deleted = 0
  AND menu.path = '/health/push/device'
  AND NOT EXISTS (
      SELECT 1
      FROM sys_menu btn
      WHERE btn.deleted = 0
        AND btn.parent_id = menu.menu_id
        AND btn.permission = 'health:push:device:detail'
  );

INSERT INTO sys_menu (
    menu_name, menu_type, router_name, parent_id, path, is_button, permission,
    meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted
)
SELECT
    '停用设备', 0, ' ', menu.menu_id, '', TRUE, 'health:push:device:disable',
    '{"title":"停用设备"}', 1, 'App设备管理-停用按钮权限', 1, NOW(), 1, NOW(), 0
FROM sys_menu menu
WHERE menu.deleted = 0
  AND menu.path = '/health/push/device'
  AND NOT EXISTS (
      SELECT 1
      FROM sys_menu btn
      WHERE btn.deleted = 0
        AND btn.parent_id = menu.menu_id
        AND btn.permission = 'health:push:device:disable'
  );

INSERT INTO sys_menu (
    menu_name, menu_type, router_name, parent_id, path, is_button, permission,
    meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted
)
SELECT
    '设备统计', 0, ' ', menu.menu_id, '', TRUE, 'health:push:device:statistics',
    '{"title":"设备统计"}', 1, 'App设备管理-统计按钮权限', 1, NOW(), 1, NOW(), 0
FROM sys_menu menu
WHERE menu.deleted = 0
  AND menu.path = '/health/push/device'
  AND NOT EXISTS (
      SELECT 1
      FROM sys_menu btn
      WHERE btn.deleted = 0
        AND btn.parent_id = menu.menu_id
        AND btn.permission = 'health:push:device:statistics'
  );

INSERT INTO sys_menu (
    menu_name, menu_type, router_name, parent_id, path, is_button, permission,
    meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted
)
SELECT
    'Push派发审计', 1, 'HealthPushDelivery', root.menu_id, '/health/push/delivery', FALSE, 'health:push:delivery:list',
    '{"title":"Push派发审计","showParent":1}', 1, '健康系统后台 Push 派发审计菜单', 1, NOW(), 1, NOW(), 0
FROM sys_menu root
WHERE root.deleted = 0
  AND root.path = '/health'
  AND NOT EXISTS (
      SELECT 1
      FROM sys_menu menu
      WHERE menu.deleted = 0
        AND menu.parent_id = root.menu_id
        AND menu.path = '/health/push/delivery'
  );

INSERT INTO sys_menu (
    menu_name, menu_type, router_name, parent_id, path, is_button, permission,
    meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted
)
SELECT
    '派发详情', 0, ' ', menu.menu_id, '', TRUE, 'health:push:delivery:detail',
    '{"title":"派发详情"}', 1, 'Push派发审计-详情按钮权限', 1, NOW(), 1, NOW(), 0
FROM sys_menu menu
WHERE menu.deleted = 0
  AND menu.path = '/health/push/delivery'
  AND NOT EXISTS (
      SELECT 1
      FROM sys_menu btn
      WHERE btn.deleted = 0
        AND btn.parent_id = menu.menu_id
        AND btn.permission = 'health:push:delivery:detail'
  );

INSERT INTO sys_menu (
    menu_name, menu_type, router_name, parent_id, path, is_button, permission,
    meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted
)
SELECT
    '派发统计', 0, ' ', menu.menu_id, '', TRUE, 'health:push:delivery:statistics',
    '{"title":"派发统计"}', 1, 'Push派发审计-统计按钮权限', 1, NOW(), 1, NOW(), 0
FROM sys_menu menu
WHERE menu.deleted = 0
  AND menu.path = '/health/push/delivery'
  AND NOT EXISTS (
      SELECT 1
      FROM sys_menu btn
      WHERE btn.deleted = 0
        AND btn.parent_id = menu.menu_id
        AND btn.permission = 'health:push:delivery:statistics'
  );

INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 2, menu.menu_id
FROM sys_menu menu
WHERE menu.deleted = 0
  AND (
      menu.path IN ('/health/push/device', '/health/push/delivery')
      OR menu.permission IN (
          'health:push:device:list',
          'health:push:device:detail',
          'health:push:device:disable',
          'health:push:device:statistics',
          'health:push:delivery:list',
          'health:push:delivery:detail',
          'health:push:delivery:statistics'
      )
  )
  AND NOT EXISTS (
      SELECT 1
      FROM sys_role_menu role_menu
      WHERE role_menu.role_id = 2
        AND role_menu.menu_id = menu.menu_id
  );
