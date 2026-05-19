-- 健康系统会员管理菜单 PostgreSQL 初始化脚本
--
-- 说明：
-- 1. 这份脚本参考了 E:\code\AssetFlow 的会员菜单落地方式；
-- 2. 本次除了已有会员等级 / 权益 / 用户会员 / 兑换码页面，也把新补的门禁点 / 门禁规则页面一起挂到菜单里；
-- 3. 为了尽量避开历史主键冲突，菜单 ID 统一使用 300 段位；
-- 4. 如果你们库里这些 menu_id 已被占用，请整体替换成未使用的 ID 后再执行。

-- 会员管理目录
INSERT INTO sys_menu (
    menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission,
    meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted
) VALUES (
    300, '会员管理', 2, 'SystemMemberManagement', 1, '/system/member', false, NULL,
    '{"title":"会员管理","icon":"ri:vip-crown-2-line","showLink":true,"rank":80}',
    1, '会员管理目录', NULL, NOW(), NULL, NOW(), 0
)
ON CONFLICT (menu_id) DO NOTHING;

-- 会员管理页面菜单
INSERT INTO sys_menu (
    menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission,
    meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted
) VALUES
    (301, '会员等级管理', 1, 'SystemMemberLevels', 300, '/system/member-levels/index', false, 'system:memberLevel:list', '{"title":"会员等级管理","showLink":true,"rank":81}', 1, '会员等级管理页面', NULL, NOW(), NULL, NOW(), 0),
    (302, '用户会员管理', 1, 'SystemUserMembers', 300, '/system/user-members/index', false, 'system:userMember:list', '{"title":"用户会员管理","showLink":true,"rank":82}', 1, '用户会员管理页面', NULL, NOW(), NULL, NOW(), 0),
    (303, '会员兑换码管理', 1, 'SystemMemberRedeemCodes', 300, '/system/member-redeem-codes/index', false, 'system:memberRedeemCode:list', '{"title":"会员兑换码管理","showLink":true,"rank":83}', 1, '会员兑换码管理页面', NULL, NOW(), NULL, NOW(), 0),
    (304, '会员权益定义', 1, 'SystemMemberFeatures', 300, '/system/member-features/index', false, 'system:memberFeature:list', '{"title":"会员权益定义","showLink":true,"rank":84}', 1, '会员权益定义页面', NULL, NOW(), NULL, NOW(), 0),
    (305, '等级权益配置', 1, 'SystemMemberLevelFeatures', 300, '/system/member-level-features/index', false, 'system:memberLevelFeature:query', '{"title":"等级权益配置","showLink":true,"rank":85}', 1, '会员等级权益配置页面', NULL, NOW(), NULL, NOW(), 0),
    (306, '会员门禁点管理', 1, 'SystemMemberGates', 300, '/system/member-gates/index', false, 'system:memberGate:list', '{"title":"会员门禁点管理","showLink":true,"rank":86}', 1, '会员门禁点管理页面', NULL, NOW(), NULL, NOW(), 0),
    (307, '会员门禁规则管理', 1, 'SystemMemberGateRules', 300, '/system/member-gate-rules/index', false, 'system:memberGateRule:list', '{"title":"会员门禁规则管理","showLink":true,"rank":87}', 1, '会员门禁规则管理页面', NULL, NOW(), NULL, NOW(), 0)
ON CONFLICT (menu_id) DO NOTHING;

-- 会员等级管理按钮
INSERT INTO sys_menu (
    menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission,
    meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted
) VALUES
    (308, '会员等级查询', 0, ' ', 301, '', true, 'system:memberLevel:list', '{}', 1, '会员等级查询按钮', NULL, NOW(), NULL, NOW(), 0),
    (309, '会员等级详情', 0, ' ', 301, '', true, 'system:memberLevel:query', '{}', 1, '会员等级详情按钮', NULL, NOW(), NULL, NOW(), 0),
    (310, '会员等级新增', 0, ' ', 301, '', true, 'system:memberLevel:add', '{}', 1, '会员等级新增按钮', NULL, NOW(), NULL, NOW(), 0),
    (311, '会员等级修改', 0, ' ', 301, '', true, 'system:memberLevel:edit', '{}', 1, '会员等级修改按钮', NULL, NOW(), NULL, NOW(), 0),
    (312, '会员等级删除', 0, ' ', 301, '', true, 'system:memberLevel:remove', '{}', 1, '会员等级删除按钮', NULL, NOW(), NULL, NOW(), 0)
ON CONFLICT (menu_id) DO NOTHING;

-- 用户会员管理按钮
INSERT INTO sys_menu (
    menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission,
    meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted
) VALUES
    (313, '用户会员查询', 0, ' ', 302, '', true, 'system:userMember:list', '{}', 1, '用户会员查询按钮', NULL, NOW(), NULL, NOW(), 0),
    (314, '用户会员详情', 0, ' ', 302, '', true, 'system:userMember:query', '{}', 1, '用户会员详情按钮', NULL, NOW(), NULL, NOW(), 0),
    (315, '后台赠送会员', 0, ' ', 302, '', true, 'system:userMember:grant', '{}', 1, '后台赠送会员按钮', NULL, NOW(), NULL, NOW(), 0),
    (316, '清空用户会员', 0, ' ', 302, '', true, 'system:userMember:clear', '{}', 1, '清空用户会员按钮', NULL, NOW(), NULL, NOW(), 0)
ON CONFLICT (menu_id) DO NOTHING;

-- 会员兑换码管理按钮
INSERT INTO sys_menu (
    menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission,
    meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted
) VALUES
    (317, '兑换码查询', 0, ' ', 303, '', true, 'system:memberRedeemCode:list', '{}', 1, '会员兑换码查询按钮', NULL, NOW(), NULL, NOW(), 0),
    (318, '批量生成兑换码', 0, ' ', 303, '', true, 'system:memberRedeemCode:generate', '{}', 1, '会员兑换码生成按钮', NULL, NOW(), NULL, NOW(), 0),
    (319, '删除兑换码', 0, ' ', 303, '', true, 'system:memberRedeemCode:remove', '{}', 1, '会员兑换码删除按钮', NULL, NOW(), NULL, NOW(), 0)
ON CONFLICT (menu_id) DO NOTHING;

-- 会员权益定义按钮
INSERT INTO sys_menu (
    menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission,
    meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted
) VALUES
    (320, '会员权益查询', 0, ' ', 304, '', true, 'system:memberFeature:list', '{}', 1, '会员权益查询按钮', NULL, NOW(), NULL, NOW(), 0),
    (321, '会员权益详情', 0, ' ', 304, '', true, 'system:memberFeature:query', '{}', 1, '会员权益详情按钮', NULL, NOW(), NULL, NOW(), 0),
    (322, '会员权益新增', 0, ' ', 304, '', true, 'system:memberFeature:add', '{}', 1, '会员权益新增按钮', NULL, NOW(), NULL, NOW(), 0),
    (323, '会员权益修改', 0, ' ', 304, '', true, 'system:memberFeature:edit', '{}', 1, '会员权益修改按钮', NULL, NOW(), NULL, NOW(), 0),
    (324, '会员权益删除', 0, ' ', 304, '', true, 'system:memberFeature:remove', '{}', 1, '会员权益删除按钮', NULL, NOW(), NULL, NOW(), 0)
ON CONFLICT (menu_id) DO NOTHING;

-- 等级权益配置按钮
INSERT INTO sys_menu (
    menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission,
    meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted
) VALUES
    (325, '等级权益矩阵查询', 0, ' ', 305, '', true, 'system:memberLevelFeature:query', '{}', 1, '等级权益矩阵查询按钮', NULL, NOW(), NULL, NOW(), 0),
    (326, '等级权益矩阵保存', 0, ' ', 305, '', true, 'system:memberLevelFeature:edit', '{}', 1, '等级权益矩阵保存按钮', NULL, NOW(), NULL, NOW(), 0)
ON CONFLICT (menu_id) DO NOTHING;

-- 会员门禁点管理按钮
INSERT INTO sys_menu (
    menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission,
    meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted
) VALUES
    (327, '会员门禁点查询', 0, ' ', 306, '', true, 'system:memberGate:list', '{}', 1, '会员门禁点查询按钮', NULL, NOW(), NULL, NOW(), 0),
    (328, '会员门禁点详情', 0, ' ', 306, '', true, 'system:memberGate:query', '{}', 1, '会员门禁点详情按钮', NULL, NOW(), NULL, NOW(), 0),
    (329, '会员门禁点新增', 0, ' ', 306, '', true, 'system:memberGate:add', '{}', 1, '会员门禁点新增按钮', NULL, NOW(), NULL, NOW(), 0),
    (330, '会员门禁点修改', 0, ' ', 306, '', true, 'system:memberGate:edit', '{}', 1, '会员门禁点修改按钮', NULL, NOW(), NULL, NOW(), 0),
    (331, '会员门禁点删除', 0, ' ', 306, '', true, 'system:memberGate:remove', '{}', 1, '会员门禁点删除按钮', NULL, NOW(), NULL, NOW(), 0)
ON CONFLICT (menu_id) DO NOTHING;

-- 会员门禁规则管理按钮
INSERT INTO sys_menu (
    menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission,
    meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted
) VALUES
    (332, '会员门禁规则查询', 0, ' ', 307, '', true, 'system:memberGateRule:list', '{}', 1, '会员门禁规则查询按钮', NULL, NOW(), NULL, NOW(), 0),
    (333, '会员门禁规则详情', 0, ' ', 307, '', true, 'system:memberGateRule:query', '{}', 1, '会员门禁规则详情按钮', NULL, NOW(), NULL, NOW(), 0),
    (334, '会员门禁规则保存', 0, ' ', 307, '', true, 'system:memberGateRule:edit', '{}', 1, '会员门禁规则保存按钮', NULL, NOW(), NULL, NOW(), 0)
ON CONFLICT (menu_id) DO NOTHING;
