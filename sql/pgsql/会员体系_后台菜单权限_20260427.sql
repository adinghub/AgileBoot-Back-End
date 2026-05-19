-- 健康系统会员体系后台菜单权限脚本（PostgreSQL）
--
-- 说明：
-- 1. 本脚本只面向 PostgreSQL
-- 2. 会员管理挂在顶级“系统管理”(menu_id = 1) 下
-- 3. 页面路由与本次前端落地文件保持一致
-- 4. 脚本尽量按幂等方式执行，避免重复导入

DO $$
DECLARE
    v_root_menu_id BIGINT;
    v_member_feature_menu_id BIGINT;
    v_member_level_menu_id BIGINT;
    v_member_level_feature_menu_id BIGINT;
    v_user_member_menu_id BIGINT;
    v_member_redeem_code_menu_id BIGINT;
BEGIN
    SELECT menu_id INTO v_root_menu_id
    FROM sys_menu
    WHERE deleted = 0
      AND parent_id = 1
      AND path = '/system/member'
    LIMIT 1;

    IF v_root_menu_id IS NULL THEN
        INSERT INTO sys_menu (
            menu_name, menu_type, router_name, parent_id, path, is_button,
            permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted
        ) VALUES (
            '会员管理', 2, 'SystemMemberManagement', 1, '/system/member', false,
            '', '{"title":"会员管理","icon":"ri:vip-crown-2-line","showParent":true,"rank":11}',
            1, '会员管理目录', 1, NOW(), 1, NOW(), 0
        )
        RETURNING menu_id INTO v_root_menu_id;
    END IF;

    SELECT menu_id INTO v_member_level_menu_id
    FROM sys_menu
    WHERE deleted = 0 AND parent_id = v_root_menu_id AND path = '/system/member-levels/index'
    LIMIT 1;
    IF v_member_level_menu_id IS NULL THEN
        INSERT INTO sys_menu (
            menu_name, menu_type, router_name, parent_id, path, is_button, permission,
            meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted
        ) VALUES (
            '会员等级管理', 1, 'SystemMemberLevels', v_root_menu_id, '/system/member-levels/index', false, 'system:memberLevel:list',
            '{"title":"会员等级管理","showParent":true}', 1, '会员等级管理页面', 1, NOW(), 1, NOW(), 0
        )
        RETURNING menu_id INTO v_member_level_menu_id;
    END IF;

    SELECT menu_id INTO v_member_feature_menu_id
    FROM sys_menu
    WHERE deleted = 0 AND parent_id = v_root_menu_id AND path = '/system/member-features/index'
    LIMIT 1;
    IF v_member_feature_menu_id IS NULL THEN
        INSERT INTO sys_menu (
            menu_name, menu_type, router_name, parent_id, path, is_button, permission,
            meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted
        ) VALUES (
            '会员权益定义', 1, 'SystemMemberFeatures', v_root_menu_id, '/system/member-features/index', false, 'system:memberFeature:list',
            '{"title":"会员权益定义","showParent":true}', 1, '会员权益定义页面', 1, NOW(), 1, NOW(), 0
        )
        RETURNING menu_id INTO v_member_feature_menu_id;
    END IF;

    SELECT menu_id INTO v_member_level_feature_menu_id
    FROM sys_menu
    WHERE deleted = 0 AND parent_id = v_root_menu_id AND path = '/system/member-level-features/index'
    LIMIT 1;
    IF v_member_level_feature_menu_id IS NULL THEN
        INSERT INTO sys_menu (
            menu_name, menu_type, router_name, parent_id, path, is_button, permission,
            meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted
        ) VALUES (
            '等级权益配置', 1, 'SystemMemberLevelFeatures', v_root_menu_id, '/system/member-level-features/index', false, 'system:memberLevelFeature:query',
            '{"title":"等级权益配置","showParent":true}', 1, '会员等级权益配置页面', 1, NOW(), 1, NOW(), 0
        )
        RETURNING menu_id INTO v_member_level_feature_menu_id;
    END IF;

    SELECT menu_id INTO v_user_member_menu_id
    FROM sys_menu
    WHERE deleted = 0 AND parent_id = v_root_menu_id AND path = '/system/user-members/index'
    LIMIT 1;
    IF v_user_member_menu_id IS NULL THEN
        INSERT INTO sys_menu (
            menu_name, menu_type, router_name, parent_id, path, is_button, permission,
            meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted
        ) VALUES (
            '用户会员管理', 1, 'SystemUserMembers', v_root_menu_id, '/system/user-members/index', false, 'system:userMember:list',
            '{"title":"用户会员管理","showParent":true}', 1, '用户会员管理页面', 1, NOW(), 1, NOW(), 0
        )
        RETURNING menu_id INTO v_user_member_menu_id;
    END IF;

    SELECT menu_id INTO v_member_redeem_code_menu_id
    FROM sys_menu
    WHERE deleted = 0 AND parent_id = v_root_menu_id AND path = '/system/member-redeem-codes/index'
    LIMIT 1;
    IF v_member_redeem_code_menu_id IS NULL THEN
        INSERT INTO sys_menu (
            menu_name, menu_type, router_name, parent_id, path, is_button, permission,
            meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted
        ) VALUES (
            '会员兑换码管理', 1, 'SystemMemberRedeemCodes', v_root_menu_id, '/system/member-redeem-codes/index', false, 'system:memberRedeemCode:list',
            '{"title":"会员兑换码管理","showParent":true}', 1, '会员兑换码管理页面', 1, NOW(), 1, NOW(), 0
        )
        RETURNING menu_id INTO v_member_redeem_code_menu_id;
    END IF;

    INSERT INTO sys_menu (menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted)
    SELECT '会员等级详情', 0, ' ', v_member_level_menu_id, '', true, 'system:memberLevel:query', '{}', 1, '会员等级详情按钮', 1, NOW(), 1, NOW(), 0
    WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE deleted = 0 AND parent_id = v_member_level_menu_id AND permission = 'system:memberLevel:query');

    INSERT INTO sys_menu (menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted)
    SELECT '会员等级新增', 0, ' ', v_member_level_menu_id, '', true, 'system:memberLevel:add', '{}', 1, '会员等级新增按钮', 1, NOW(), 1, NOW(), 0
    WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE deleted = 0 AND parent_id = v_member_level_menu_id AND permission = 'system:memberLevel:add');

    INSERT INTO sys_menu (menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted)
    SELECT '会员等级修改', 0, ' ', v_member_level_menu_id, '', true, 'system:memberLevel:edit', '{}', 1, '会员等级修改按钮', 1, NOW(), 1, NOW(), 0
    WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE deleted = 0 AND parent_id = v_member_level_menu_id AND permission = 'system:memberLevel:edit');

    INSERT INTO sys_menu (menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted)
    SELECT '会员等级删除', 0, ' ', v_member_level_menu_id, '', true, 'system:memberLevel:remove', '{}', 1, '会员等级删除按钮', 1, NOW(), 1, NOW(), 0
    WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE deleted = 0 AND parent_id = v_member_level_menu_id AND permission = 'system:memberLevel:remove');

    INSERT INTO sys_menu (menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted)
    SELECT '会员权益详情', 0, ' ', v_member_feature_menu_id, '', true, 'system:memberFeature:query', '{}', 1, '会员权益详情按钮', 1, NOW(), 1, NOW(), 0
    WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE deleted = 0 AND parent_id = v_member_feature_menu_id AND permission = 'system:memberFeature:query');

    INSERT INTO sys_menu (menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted)
    SELECT '会员权益新增', 0, ' ', v_member_feature_menu_id, '', true, 'system:memberFeature:add', '{}', 1, '会员权益新增按钮', 1, NOW(), 1, NOW(), 0
    WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE deleted = 0 AND parent_id = v_member_feature_menu_id AND permission = 'system:memberFeature:add');

    INSERT INTO sys_menu (menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted)
    SELECT '会员权益修改', 0, ' ', v_member_feature_menu_id, '', true, 'system:memberFeature:edit', '{}', 1, '会员权益修改按钮', 1, NOW(), 1, NOW(), 0
    WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE deleted = 0 AND parent_id = v_member_feature_menu_id AND permission = 'system:memberFeature:edit');

    INSERT INTO sys_menu (menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted)
    SELECT '会员权益删除', 0, ' ', v_member_feature_menu_id, '', true, 'system:memberFeature:remove', '{}', 1, '会员权益删除按钮', 1, NOW(), 1, NOW(), 0
    WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE deleted = 0 AND parent_id = v_member_feature_menu_id AND permission = 'system:memberFeature:remove');

    INSERT INTO sys_menu (menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted)
    SELECT '等级权益保存', 0, ' ', v_member_level_feature_menu_id, '', true, 'system:memberLevelFeature:edit', '{}', 1, '等级权益保存按钮', 1, NOW(), 1, NOW(), 0
    WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE deleted = 0 AND parent_id = v_member_level_feature_menu_id AND permission = 'system:memberLevelFeature:edit');

    INSERT INTO sys_menu (menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted)
    SELECT '用户会员详情', 0, ' ', v_user_member_menu_id, '', true, 'system:userMember:query', '{}', 1, '用户会员详情按钮', 1, NOW(), 1, NOW(), 0
    WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE deleted = 0 AND parent_id = v_user_member_menu_id AND permission = 'system:userMember:query');

    INSERT INTO sys_menu (menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted)
    SELECT '用户会员赠送', 0, ' ', v_user_member_menu_id, '', true, 'system:userMember:grant', '{}', 1, '用户会员赠送按钮', 1, NOW(), 1, NOW(), 0
    WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE deleted = 0 AND parent_id = v_user_member_menu_id AND permission = 'system:userMember:grant');

    INSERT INTO sys_menu (menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted)
    SELECT '用户会员清空', 0, ' ', v_user_member_menu_id, '', true, 'system:userMember:clear', '{}', 1, '用户会员清空按钮', 1, NOW(), 1, NOW(), 0
    WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE deleted = 0 AND parent_id = v_user_member_menu_id AND permission = 'system:userMember:clear');

    INSERT INTO sys_menu (menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted)
    SELECT '兑换码生成', 0, ' ', v_member_redeem_code_menu_id, '', true, 'system:memberRedeemCode:generate', '{}', 1, '会员兑换码生成按钮', 1, NOW(), 1, NOW(), 0
    WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE deleted = 0 AND parent_id = v_member_redeem_code_menu_id AND permission = 'system:memberRedeemCode:generate');

    INSERT INTO sys_menu (menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted)
    SELECT '兑换码删除', 0, ' ', v_member_redeem_code_menu_id, '', true, 'system:memberRedeemCode:remove', '{}', 1, '会员兑换码删除按钮', 1, NOW(), 1, NOW(), 0
    WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE deleted = 0 AND parent_id = v_member_redeem_code_menu_id AND permission = 'system:memberRedeemCode:remove');

    INSERT INTO sys_role_menu (role_id, menu_id)
    SELECT role_id, menu_id
    FROM (
        SELECT 1 AS role_id, v_root_menu_id AS menu_id
        UNION ALL SELECT 1, v_member_level_menu_id
        UNION ALL SELECT 1, v_member_feature_menu_id
        UNION ALL SELECT 1, v_member_level_feature_menu_id
        UNION ALL SELECT 1, v_user_member_menu_id
        UNION ALL SELECT 1, v_member_redeem_code_menu_id
        UNION ALL SELECT 2, v_root_menu_id
        UNION ALL SELECT 2, v_member_level_menu_id
        UNION ALL SELECT 2, v_member_feature_menu_id
        UNION ALL SELECT 2, v_member_level_feature_menu_id
        UNION ALL SELECT 2, v_user_member_menu_id
        UNION ALL SELECT 2, v_member_redeem_code_menu_id
        UNION ALL
        SELECT 1, menu_id FROM sys_menu
        WHERE deleted = 0
          AND permission IN (
            'system:memberLevel:query',
            'system:memberLevel:add',
            'system:memberLevel:edit',
            'system:memberLevel:remove',
            'system:memberFeature:query',
            'system:memberFeature:add',
            'system:memberFeature:edit',
            'system:memberFeature:remove',
            'system:memberLevelFeature:edit',
            'system:userMember:query',
            'system:userMember:grant',
            'system:userMember:clear',
            'system:memberRedeemCode:generate',
            'system:memberRedeemCode:remove'
          )
        UNION ALL
        SELECT 2, menu_id FROM sys_menu
        WHERE deleted = 0
          AND permission IN (
            'system:memberLevel:query',
            'system:memberLevel:add',
            'system:memberLevel:edit',
            'system:memberLevel:remove',
            'system:memberFeature:query',
            'system:memberFeature:add',
            'system:memberFeature:edit',
            'system:memberFeature:remove',
            'system:memberLevelFeature:edit',
            'system:userMember:query',
            'system:userMember:grant',
            'system:userMember:clear',
            'system:memberRedeemCode:generate',
            'system:memberRedeemCode:remove'
          )
    ) t
    WHERE NOT EXISTS (
        SELECT 1 FROM sys_role_menu srm WHERE srm.role_id = t.role_id AND srm.menu_id = t.menu_id
    );
END $$;
