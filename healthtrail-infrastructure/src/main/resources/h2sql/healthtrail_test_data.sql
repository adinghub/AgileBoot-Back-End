
-- H2 测试环境基础数据初始化脚本
--
-- 说明：
-- 1. 数据内容尽量和 PostgreSQL 基线保持一致，避免测试环境与正式环境在菜单、角色、默认账号上出现行为偏差。
-- 2. 由于 H2 不支持 PostgreSQL 的 `setval` 写法，文件尾部统一使用 `ALTER TABLE ... RESTART WITH ...` 回填自增起点。

INSERT INTO sys_config (config_id, config_name, config_key, config_options, config_value, is_allow_change, creator_id, updater_id, update_time, create_time, remark, deleted) VALUES (1, '主框架页-默认皮肤样式名称', 'sys.index.skinName', '["skin-blue","skin-green","skin-purple","skin-red","skin-yellow"]', 'skin-blue', TRUE, NULL, NULL, '2022-08-28 22:12:19', '2022-05-21 08:30:55', '蓝色 skin-blue、绿色 skin-green、紫色 skin-purple、红色 skin-red、黄色 skin-yellow', 0);
INSERT INTO sys_config (config_id, config_name, config_key, config_options, config_value, is_allow_change, creator_id, updater_id, update_time, create_time, remark, deleted) VALUES (2, '用户管理-账号初始密码', 'sys.user.initPassword', '', '123456', TRUE, NULL, 1, '2023-07-20 14:42:08', '2022-05-21 08:30:55', '初始化密码 123456', 0);
INSERT INTO sys_config (config_id, config_name, config_key, config_options, config_value, is_allow_change, creator_id, updater_id, update_time, create_time, remark, deleted) VALUES (3, '主框架页-侧边栏主题', 'sys.index.sideTheme', '["theme-dark","theme-light"]', 'theme-dark', TRUE, NULL, NULL, '2022-08-28 22:12:15', '2022-08-20 08:30:55', '深色主题theme-dark，浅色主题theme-light', 0);
INSERT INTO sys_config (config_id, config_name, config_key, config_options, config_value, is_allow_change, creator_id, updater_id, update_time, create_time, remark, deleted) VALUES (4, '账号自助-验证码开关', 'sys.account.captchaOnOff', '["true","false"]', 'false', FALSE, NULL, 1, '2023-07-20 14:39:36', '2022-05-21 08:30:55', '是否开启验证码功能（true开启，false关闭）', 0);
INSERT INTO sys_config (config_id, config_name, config_key, config_options, config_value, is_allow_change, creator_id, updater_id, update_time, create_time, remark, deleted) VALUES (5, '账号自助-是否开启用户注册功能', 'sys.account.registerUser', '["true","false"]', 'true', FALSE, NULL, 1, '2022-10-05 22:18:57', '2022-05-21 08:30:55', '是否开启注册用户功能（true开启，false关闭）', 0);

INSERT INTO sys_dept (dept_id, parent_id, ancestors, dept_name, order_num, leader_id, leader_name, phone, email, status, creator_id, create_time, updater_id, update_time, deleted) VALUES (1, 0, '0', 'HealthTrail科技', 0, NULL, 'valarchie', '15888888888', 'valarchie@163.com', 1, NULL, '2022-05-21 08:30:54', NULL, NULL, 0);
INSERT INTO sys_dept (dept_id, parent_id, ancestors, dept_name, order_num, leader_id, leader_name, phone, email, status, creator_id, create_time, updater_id, update_time, deleted) VALUES (2, 1, '0,1', '深圳总公司', 1, NULL, 'valarchie', '15888888888', 'valarchie@163.com', 1, NULL, '2022-05-21 08:30:54', NULL, NULL, 0);
INSERT INTO sys_dept (dept_id, parent_id, ancestors, dept_name, order_num, leader_id, leader_name, phone, email, status, creator_id, create_time, updater_id, update_time, deleted) VALUES (3, 1, '0,1', '长沙分公司', 2, NULL, 'valarchie', '15888888888', 'valarchie@163.com', 1, NULL, '2022-05-21 08:30:54', NULL, NULL, 0);
INSERT INTO sys_dept (dept_id, parent_id, ancestors, dept_name, order_num, leader_id, leader_name, phone, email, status, creator_id, create_time, updater_id, update_time, deleted) VALUES (4, 2, '0,1,2', '研发部门', 1, NULL, 'valarchie', '15888888888', 'valarchie@163.com', 1, NULL, '2022-05-21 08:30:54', NULL, NULL, 0);
INSERT INTO sys_dept (dept_id, parent_id, ancestors, dept_name, order_num, leader_id, leader_name, phone, email, status, creator_id, create_time, updater_id, update_time, deleted) VALUES (5, 2, '0,1,2', '市场部门', 2, NULL, 'valarchie', '15888888888', 'valarchie@163.com', 0, NULL, '2022-05-21 08:30:54', 1, '2023-07-20 22:46:41', 0);
INSERT INTO sys_dept (dept_id, parent_id, ancestors, dept_name, order_num, leader_id, leader_name, phone, email, status, creator_id, create_time, updater_id, update_time, deleted) VALUES (6, 2, '0,1,2', '测试部门', 3, NULL, 'valarchie', '15888888888', 'valarchie@163.com', 1, NULL, '2022-05-21 08:30:54', NULL, NULL, 0);
INSERT INTO sys_dept (dept_id, parent_id, ancestors, dept_name, order_num, leader_id, leader_name, phone, email, status, creator_id, create_time, updater_id, update_time, deleted) VALUES (7, 2, '0,1,2', '财务部门', 4, NULL, 'valarchie', '15888888888', 'valarchie@163.com', 1, NULL, '2022-05-21 08:30:54', NULL, NULL, 0);
INSERT INTO sys_dept (dept_id, parent_id, ancestors, dept_name, order_num, leader_id, leader_name, phone, email, status, creator_id, create_time, updater_id, update_time, deleted) VALUES (8, 2, '0,1,2', '运维部门', 5, NULL, 'valarchie', '15888888888', 'valarchie@163.com', 1, NULL, '2022-05-21 08:30:54', NULL, NULL, 0);
INSERT INTO sys_dept (dept_id, parent_id, ancestors, dept_name, order_num, leader_id, leader_name, phone, email, status, creator_id, create_time, updater_id, update_time, deleted) VALUES (9, 3, '0,1,3', '市场部!', 1, NULL, 'valarchie!!', '15888188888', 'valarc1hie@163.com', 0, NULL, '2022-05-21 08:30:54', 1, '2023-07-20 22:33:31', 0);
INSERT INTO sys_dept (dept_id, parent_id, ancestors, dept_name, order_num, leader_id, leader_name, phone, email, status, creator_id, create_time, updater_id, update_time, deleted) VALUES (10, 3, '0,1,3', '财务部门', 2, NULL, 'valarchie', '15888888888', 'valarchie@163.com', 0, NULL, '2022-05-21 08:30:54', NULL, NULL, 0);

INSERT INTO sys_login_info (info_id, username, ip_address, login_location, browser, operation_system, status, msg, login_time, deleted) VALUES (415, 'admin', '127.0.0.1', '内网IP', 'Chrome 11', 'Mac OS X', 1, '登录成功', '2023-06-29 22:49:37', 0);
INSERT INTO sys_login_info (info_id, username, ip_address, login_location, browser, operation_system, status, msg, login_time, deleted) VALUES (416, 'admin', '127.0.0.1', '内网IP', 'Chrome 11', 'Mac OS X', 1, '登录成功', '2023-07-02 22:12:30', 0);
INSERT INTO sys_login_info (info_id, username, ip_address, login_location, browser, operation_system, status, msg, login_time, deleted) VALUES (417, 'admin', '127.0.0.1', '内网IP', 'Chrome 11', 'Mac OS X', 0, '验证码过期', '2023-07-02 22:16:06', 0);

INSERT INTO sys_menu (menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted) VALUES (1, '系统管理', 2, '', 0, '/system', FALSE, '', '{"title":"系统管理","icon":"ep:management","showParent":true,"rank":1}', 1, '系统管理目录', 0, '2022-05-21 08:30:54', 1, '2023-08-14 23:08:50', 0);
INSERT INTO sys_menu (menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted) VALUES (2, '系统监控', 2, '', 0, '/monitor', FALSE, '', '{"title":"系统监控","icon":"ep:monitor","showParent":true,"rank":3}', 1, '系统监控目录', 0, '2022-05-21 08:30:54', 1, '2023-08-14 23:09:15', 0);
INSERT INTO sys_menu (menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted) VALUES (3, '系统工具', 2, '', 0, '/tool', FALSE, '', '{"title":"系统工具","icon":"ep:tools","showParent":true,"rank":2}', 1, '系统工具目录', 0, '2022-05-21 08:30:54', 1, '2023-08-14 23:09:03', 0);
-- 菜单路由名和路径也需要跟随品牌改造统一，否则测试初始化库里仍会残留旧品牌标识。
INSERT INTO sys_menu (menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted) VALUES (4, 'HealthTrail官网', 3, 'HealthTrailguanwangIframeRouter', 0, '/HealthTrailguanwangIframeLink', FALSE, '', '{"title":"HealthTrail官网","icon":"ep:link","showParent":true,"frameSrc":"https://element-plus.org/zh-CN/","rank":8}', 1, 'HealthTrail官网地址', 0, '2022-05-21 08:30:54', 1, '2023-08-14 23:09:40', 0);
INSERT INTO sys_menu (menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted) VALUES (5, '用户管理', 1, 'SystemUser', 1, '/system/user/index', FALSE, 'system:user:list', '{"title":"用户管理","icon":"ep:user-filled","showParent":true}', 1, '用户管理菜单', 0, '2022-05-21 08:30:54', 1, '2023-08-14 23:16:13', 0);
INSERT INTO sys_menu (menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted) VALUES (6, '角色管理', 1, 'SystemRole', 1, '/system/role/index', FALSE, 'system:role:list', '{"title":"角色管理","icon":"ep:user","showParent":true}', 1, '角色管理菜单', 0, '2022-05-21 08:30:54', 1, '2023-08-14 23:16:23', 0);
INSERT INTO sys_menu (menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted) VALUES (7, '菜单管理', 1, 'MenuManagement', 1, '/system/menu/index', FALSE, 'system:menu:list', '{"title":"菜单管理","icon":"ep:menu","showParent":true}', 1, '菜单管理菜单', 0, '2022-05-21 08:30:54', 1, '2023-08-14 23:15:41', 0);
INSERT INTO sys_menu (menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted) VALUES (8, '部门管理', 1, 'Department', 1, '/system/dept/index', FALSE, 'system:dept:list', '{"title":"部门管理","icon":"fa-solid:code-branch","showParent":true}', 1, '部门管理菜单', 0, '2022-05-21 08:30:54', 1, '2023-08-14 23:15:35', 0);
INSERT INTO sys_menu (menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted) VALUES (9, '岗位管理', 1, 'Post', 1, '/system/post/index', FALSE, 'system:post:list', '{"title":"岗位管理","icon":"ep:postcard","showParent":true}', 1, '岗位管理菜单', 0, '2022-05-21 08:30:54', 1, '2023-08-14 23:15:11', 0);
INSERT INTO sys_menu (menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted) VALUES (10, '参数设置', 1, 'Config', 1, '/system/config/index', FALSE, 'system:config:list', '{"title":"参数设置","icon":"ep:setting","showParent":true}', 1, '参数设置菜单', 0, '2022-05-21 08:30:54', 1, '2023-08-14 23:15:03', 0);
INSERT INTO sys_menu (menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted) VALUES (11, '通知公告', 1, 'SystemNotice', 1, '/system/notice/index', FALSE, 'system:notice:list', '{"title":"通知公告","icon":"ep:notification","showParent":true}', 1, '通知公告菜单', 0, '2022-05-21 08:30:54', 1, '2023-08-14 23:14:56', 0);
INSERT INTO sys_menu (menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted) VALUES (12, '日志管理', 1, 'LogManagement', 1, '/system/logd', FALSE, '', '{"title":"日志管理","icon":"ep:document","showParent":true}', 1, '日志管理菜单', 0, '2022-05-21 08:30:54', 1, '2023-08-14 23:14:47', 0);
INSERT INTO sys_menu (menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted) VALUES (13, '在线用户', 1, 'OnlineUser', 2, '/system/monitor/onlineUser/index', FALSE, 'monitor:online:list', '{"title":"在线用户","icon":"fa-solid:users","showParent":true}', 1, '在线用户菜单', 0, '2022-05-21 08:30:54', 1, '2023-08-14 23:13:13', 0);
INSERT INTO sys_menu (menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted) VALUES (14, '数据监控', 1, 'DataMonitor', 2, '/system/monitor/druid/index', FALSE, 'monitor:druid:list', '{"title":"数据监控","icon":"fa:database","showParent":true,"frameSrc":"/druid/login.html","isFrameSrcInternal":true}', 1, '数据监控菜单', 0, '2022-05-21 08:30:54', 1, '2023-08-14 23:13:25', 0);
INSERT INTO sys_menu (menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted) VALUES (15, '服务监控', 1, 'ServerInfo', 2, '/system/monitor/server/index', FALSE, 'monitor:server:list', '{"title":"服务监控","icon":"fa:server","showParent":true}', 1, '服务监控菜单', 0, '2022-05-21 08:30:54', 1, '2023-08-14 23:13:34', 0);
INSERT INTO sys_menu (menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted) VALUES (16, '缓存监控', 1, 'CacheInfo', 2, '/system/monitor/cache/index', FALSE, 'monitor:cache:list', '{"title":"缓存监控","icon":"ep:reading","showParent":true}', 1, '缓存监控菜单', 0, '2022-05-21 08:30:54', 1, '2023-08-14 23:12:59', 0);
INSERT INTO sys_menu (menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted) VALUES (17, '系统接口', 1, 'SystemAPI', 3, '/tool/swagger/index', FALSE, 'tool:swagger:list', '{"title":"系统接口","icon":"ep:document-remove","showParent":true,"frameSrc":"/swagger-ui/index.html","isFrameSrcInternal":true}', 1, '系统接口菜单', 0, '2022-05-21 08:30:54', 1, '2023-08-14 23:14:01', 0);
INSERT INTO sys_menu (menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted) VALUES (18, '操作日志', 1, 'OperationLog', 12, '/system/log/operationLog/index', FALSE, 'monitor:operlog:list', '{"title":"操作日志"}', 1, '操作日志菜单', 0, '2022-05-21 08:30:54', NULL, NULL, 0);
INSERT INTO sys_menu (menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted) VALUES (19, '登录日志', 1, 'LoginLog', 12, '/system/log/loginLog/index', FALSE, 'monitor:logininfor:list', '{"title":"登录日志"}', 1, '登录日志菜单', 0, '2022-05-21 08:30:54', NULL, NULL, 0);
INSERT INTO sys_menu (menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted) VALUES (20, '用户查询', 0, ' ', 5, '', TRUE, 'system:user:query', '{"title":"用户查询"}', 1, '', 0, '2022-05-21 08:30:54', NULL, NULL, 0);
INSERT INTO sys_menu (menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted) VALUES (21, '用户新增', 0, ' ', 5, '', TRUE, 'system:user:add', '{"title":"用户新增"}', 1, '', 0, '2022-05-21 08:30:54', NULL, NULL, 0);
INSERT INTO sys_menu (menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted) VALUES (22, '用户修改', 0, ' ', 5, '', TRUE, 'system:user:edit', '{"title":"用户修改"}', 1, '', 0, '2022-05-21 08:30:54', NULL, NULL, 0);
INSERT INTO sys_menu (menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted) VALUES (23, '用户删除', 0, ' ', 5, '', TRUE, 'system:user:remove', '{"title":"用户删除"}', 1, '', 0, '2022-05-21 08:30:54', NULL, NULL, 0);
INSERT INTO sys_menu (menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted) VALUES (24, '用户导出', 0, ' ', 5, '', TRUE, 'system:user:export', '{"title":"用户导出"}', 1, '', 0, '2022-05-21 08:30:54', NULL, NULL, 0);
INSERT INTO sys_menu (menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted) VALUES (25, '用户导入', 0, ' ', 5, '', TRUE, 'system:user:import', '{"title":"用户导入"}', 1, '', 0, '2022-05-21 08:30:54', NULL, NULL, 0);
INSERT INTO sys_menu (menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted) VALUES (26, '重置密码', 0, ' ', 5, '', TRUE, 'system:user:resetPwd', '{"title":"重置密码"}', 1, '', 0, '2022-05-21 08:30:54', NULL, NULL, 0);
INSERT INTO sys_menu (menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted) VALUES (27, '角色查询', 0, ' ', 6, '', TRUE, 'system:role:query', '{"title":"角色查询"}', 1, '', 0, '2022-05-21 08:30:54', NULL, NULL, 0);
INSERT INTO sys_menu (menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted) VALUES (28, '角色新增', 0, ' ', 6, '', TRUE, 'system:role:add', '{"title":"角色新增"}', 1, '', 0, '2022-05-21 08:30:54', NULL, NULL, 0);
INSERT INTO sys_menu (menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted) VALUES (29, '角色修改', 0, ' ', 6, '', TRUE, 'system:role:edit', '{"title":"角色修改"}', 1, '', 0, '2022-05-21 08:30:54', NULL, NULL, 0);
INSERT INTO sys_menu (menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted) VALUES (30, '角色删除', 0, ' ', 6, '', TRUE, 'system:role:remove', '{"title":"角色删除"}', 1, '', 0, '2022-05-21 08:30:54', NULL, NULL, 0);
INSERT INTO sys_menu (menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted) VALUES (31, '角色导出', 0, ' ', 6, '', TRUE, 'system:role:export', '{"title":"角色导出"}', 1, '', 0, '2022-05-21 08:30:54', NULL, NULL, 0);
INSERT INTO sys_menu (menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted) VALUES (32, '菜单查询', 0, ' ', 7, '', TRUE, 'system:menu:query', '{"title":"菜单查询"}', 1, '', 0, '2022-05-21 08:30:54', NULL, NULL, 0);
INSERT INTO sys_menu (menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted) VALUES (33, '菜单新增', 0, ' ', 7, '', TRUE, 'system:menu:add', '{"title":"菜单新增"}', 1, '', 0, '2022-05-21 08:30:54', NULL, NULL, 0);
INSERT INTO sys_menu (menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted) VALUES (34, '菜单修改', 0, ' ', 7, '', TRUE, 'system:menu:edit', '{"title":"菜单修改"}', 1, '', 0, '2022-05-21 08:30:54', NULL, NULL, 0);
INSERT INTO sys_menu (menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted) VALUES (35, '菜单删除', 0, ' ', 7, '', TRUE, 'system:menu:remove', '{"title":"菜单删除"}', 1, '', 0, '2022-05-21 08:30:54', NULL, NULL, 0);
INSERT INTO sys_menu (menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted) VALUES (36, '部门查询', 0, ' ', 8, '', TRUE, 'system:dept:query', '{"title":"部门查询"}', 1, '', 0, '2022-05-21 08:30:54', NULL, NULL, 0);
INSERT INTO sys_menu (menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted) VALUES (37, '部门新增', 0, ' ', 8, '', TRUE, 'system:dept:add', '{"title":"部门新增"}', 1, '', 0, '2022-05-21 08:30:54', NULL, NULL, 0);
INSERT INTO sys_menu (menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted) VALUES (38, '部门修改', 0, ' ', 8, '', TRUE, 'system:dept:edit', '{"title":"部门修改"}', 1, '', 0, '2022-05-21 08:30:54', NULL, NULL, 0);
INSERT INTO sys_menu (menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted) VALUES (39, '部门删除', 0, ' ', 8, '', TRUE, 'system:dept:remove', '{"title":"部门删除"}', 1, '', 0, '2022-05-21 08:30:54', NULL, NULL, 0);
INSERT INTO sys_menu (menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted) VALUES (40, '岗位查询', 0, ' ', 9, '', TRUE, 'system:post:query', '{"title":"岗位查询"}', 1, '', 0, '2022-05-21 08:30:54', NULL, NULL, 0);
INSERT INTO sys_menu (menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted) VALUES (41, '岗位新增', 0, ' ', 9, '', TRUE, 'system:post:add', '{"title":"岗位新增"}', 1, '', 0, '2022-05-21 08:30:54', NULL, NULL, 0);
INSERT INTO sys_menu (menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted) VALUES (42, '岗位修改', 0, ' ', 9, '', TRUE, 'system:post:edit', '{"title":"岗位修改"}', 1, '', 0, '2022-05-21 08:30:54', NULL, NULL, 0);
INSERT INTO sys_menu (menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted) VALUES (43, '岗位删除', 0, ' ', 9, '', TRUE, 'system:post:remove', '{"title":"岗位删除"}', 1, '', 0, '2022-05-21 08:30:54', NULL, NULL, 0);
INSERT INTO sys_menu (menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted) VALUES (44, '岗位导出', 0, ' ', 9, '', TRUE, 'system:post:export', '{"title":"岗位导出"}', 1, '', 0, '2022-05-21 08:30:54', NULL, NULL, 0);
INSERT INTO sys_menu (menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted) VALUES (45, '参数查询', 0, ' ', 10, '', TRUE, 'system:config:query', '{"title":"参数查询"}', 1, '', 0, '2022-05-21 08:30:54', NULL, NULL, 0);
INSERT INTO sys_menu (menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted) VALUES (46, '参数新增', 0, ' ', 10, '', TRUE, 'system:config:add', '{"title":"参数新增"}', 1, '', 0, '2022-05-21 08:30:54', NULL, NULL, 0);
INSERT INTO sys_menu (menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted) VALUES (47, '参数修改', 0, ' ', 10, '', TRUE, 'system:config:edit', '{"title":"参数修改"}', 1, '', 0, '2022-05-21 08:30:54', NULL, NULL, 0);
INSERT INTO sys_menu (menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted) VALUES (48, '参数删除', 0, ' ', 10, '', TRUE, 'system:config:remove', '{"title":"参数删除"}', 1, '', 0, '2022-05-21 08:30:54', NULL, NULL, 0);
INSERT INTO sys_menu (menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted) VALUES (49, '参数导出', 0, ' ', 10, '', TRUE, 'system:config:export', '{"title":"参数导出"}', 1, '', 0, '2022-05-21 08:30:54', NULL, NULL, 0);
INSERT INTO sys_menu (menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted) VALUES (50, '公告查询', 0, ' ', 11, '', TRUE, 'system:notice:query', '{"title":"公告查询"}', 1, '', 0, '2022-05-21 08:30:54', NULL, NULL, 0);
INSERT INTO sys_menu (menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted) VALUES (51, '公告新增', 0, ' ', 11, '', TRUE, 'system:notice:add', '{"title":"公告新增"}', 1, '', 0, '2022-05-21 08:30:54', NULL, NULL, 0);
INSERT INTO sys_menu (menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted) VALUES (52, '公告修改', 0, ' ', 11, '', TRUE, 'system:notice:edit', '{"title":"公告修改"}', 1, '', 0, '2022-05-21 08:30:54', NULL, NULL, 0);
INSERT INTO sys_menu (menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted) VALUES (53, '公告删除', 0, ' ', 11, '', TRUE, 'system:notice:remove', '{"title":"公告删除"}', 1, '', 0, '2022-05-21 08:30:54', NULL, NULL, 0);
INSERT INTO sys_menu (menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted) VALUES (54, '操作查询', 0, ' ', 18, '', TRUE, 'monitor:operlog:query', '{"title":"操作查询"}', 1, '', 0, '2022-05-21 08:30:54', NULL, NULL, 0);
INSERT INTO sys_menu (menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted) VALUES (55, '操作删除', 0, ' ', 18, '', TRUE, 'monitor:operlog:remove', '{"title":"操作删除"}', 1, '', 0, '2022-05-21 08:30:54', NULL, NULL, 0);
INSERT INTO sys_menu (menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted) VALUES (56, '日志导出', 0, ' ', 18, '', TRUE, 'monitor:operlog:export', '{"title":"日志导出"}', 1, '', 0, '2022-05-21 08:30:54', NULL, NULL, 0);
INSERT INTO sys_menu (menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted) VALUES (57, '登录查询', 0, ' ', 19, '', TRUE, 'monitor:logininfor:query', '{"title":"登录查询"}', 1, '', 0, '2022-05-21 08:30:54', NULL, NULL, 0);
INSERT INTO sys_menu (menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted) VALUES (58, '登录删除', 0, ' ', 19, '', TRUE, 'monitor:logininfor:remove', '{"title":"登录删除"}', 1, '', 0, '2022-05-21 08:30:54', NULL, NULL, 0);
INSERT INTO sys_menu (menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted) VALUES (59, '日志导出', 0, ' ', 19, '', TRUE, 'monitor:logininfor:export', '{"title":"日志导出","rank":22}', 1, '', 0, '2022-05-21 08:30:54', 1, '2023-07-22 17:02:28', 0);
INSERT INTO sys_menu (menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted) VALUES (60, '在线查询', 0, ' ', 13, '', TRUE, 'monitor:online:query', '{"title":"在线查询"}', 1, '', 0, '2022-05-21 08:30:54', NULL, NULL, 0);
INSERT INTO sys_menu (menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted) VALUES (61, '批量强退', 0, ' ', 13, '', TRUE, 'monitor:online:batchLogout', '{"title":"批量强退"}', 1, '', 0, '2022-05-21 08:30:54', NULL, NULL, 0);
INSERT INTO sys_menu (menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted) VALUES (62, '单条强退', 0, ' ', 13, '', TRUE, 'monitor:online:forceLogout', '{"title":"单条强退"}', 1, '', 0, '2022-05-21 08:30:54', NULL, NULL, 0);
INSERT INTO sys_menu (menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted) VALUES (63, 'HealthTrail 项目主页', 4, 'https://healthtrail.local', 0, '/external', FALSE, '', '{"title":"HealthTrail 项目主页","icon":"fa-solid:external-link-alt","showParent":true,"rank":9}', 1, 'HealthTrail 项目主页', 0, '2022-05-21 08:30:54', 1, '2023-08-14 23:12:13', 0);
INSERT INTO sys_menu (menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted) VALUES (64, '首页', 2, '', 0, '/global', FALSE, '121212', '{"title":"首页","showParent":true,"rank":3}', 1, '', 1, '2023-07-24 22:36:03', 1, '2023-07-24 22:38:37', 1);
INSERT INTO sys_menu (menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted) VALUES (65, '个人中心', 1, 'PersonalCenter', 2053, '/system/user/profile', FALSE, '434sdf', '{"title":"个人中心","showParent":true,"rank":3}', 1, '', 1, '2023-07-24 22:36:55', NULL, NULL, 1);

INSERT INTO sys_notice (notice_id, notice_title, notice_type, notice_content, status, creator_id, create_time, updater_id, update_time, remark, deleted) VALUES (1, '温馨提醒：2018-07-01 HealthTrail新版本发布啦', 2, '新版本内容~~~~~~~~~~', 1, 1, '2022-05-21 08:30:55', 1, '2022-08-29 20:12:37', '管理员', 0);
INSERT INTO sys_notice (notice_id, notice_title, notice_type, notice_content, status, creator_id, create_time, updater_id, update_time, remark, deleted) VALUES (2, '维护通知：2018-07-01 HealthTrail系统凌晨维护', 1, '维护内容', 1, 1, '2022-05-21 08:30:55', NULL, NULL, '管理员', 0);

INSERT INTO sys_operation_log (operation_id, business_type, request_method, request_module, request_url, called_method, operator_type, user_id, username, operator_ip, operator_location, dept_id, dept_name, operation_param, operation_result, status, error_stack, operation_time, deleted) VALUES (561, 1, 2, '菜单管理', '/system/menus', 'it.upos.builder.admin.controller.system.SysMenuController.add()', 1, 0, 'admin', '127.0.0.1', '内网IP', 0, NULL, '{"menuName":"","permission":"","parentId":2035,"path":"","isButton":false,"routerName":"","meta":{"showParent":true,"rank":0},"status":1},', '', 1, '', '2023-07-22 17:06:57', 0);

INSERT INTO sys_post (post_id, post_code, post_name, post_sort, status, remark, creator_id, create_time, updater_id, update_time, deleted) VALUES (1, 'ceo', '董事长', 1, 1, '', NULL, '2022-05-21 08:30:54', NULL, NULL, 0);
INSERT INTO sys_post (post_id, post_code, post_name, post_sort, status, remark, creator_id, create_time, updater_id, update_time, deleted) VALUES (2, 'se', '项目经理', 2, 1, '', NULL, '2022-05-21 08:30:54', NULL, NULL, 0);
INSERT INTO sys_post (post_id, post_code, post_name, post_sort, status, remark, creator_id, create_time, updater_id, update_time, deleted) VALUES (3, 'hr', '人力资源', 3, 1, '', NULL, '2022-05-21 08:30:54', NULL, NULL, 0);
INSERT INTO sys_post (post_id, post_code, post_name, post_sort, status, remark, creator_id, create_time, updater_id, update_time, deleted) VALUES (4, 'user', '普通员工', 5, 0, '', NULL, '2022-05-21 08:30:54', NULL, NULL, 0);

INSERT INTO sys_role (role_id, role_name, role_key, role_sort, data_scope, dept_id_set, status, creator_id, create_time, updater_id, update_time, remark, deleted) VALUES (1, '超级管理员', 'admin', 1, 1, '', 1, NULL, '2022-05-21 08:30:54', NULL, NULL, '超级管理员', 0);
INSERT INTO sys_role (role_id, role_name, role_key, role_sort, data_scope, dept_id_set, status, creator_id, create_time, updater_id, update_time, remark, deleted) VALUES (2, '普通角色', 'common', 3, 2, '', 1, NULL, '2022-05-21 08:30:54', NULL, NULL, '普通角色', 0);
INSERT INTO sys_role (role_id, role_name, role_key, role_sort, data_scope, dept_id_set, status, creator_id, create_time, updater_id, update_time, remark, deleted) VALUES (3, '闲置角色', 'unused', 4, 2, '', 0, NULL, '2022-05-21 08:30:54', NULL, NULL, '未使用的角色', 0);

INSERT INTO sys_role_menu (role_id, menu_id) VALUES (2, 1);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (2, 2);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (2, 3);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (2, 4);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (2, 5);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (2, 6);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (2, 7);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (2, 8);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (2, 9);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (2, 10);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (2, 11);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (2, 12);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (2, 13);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (2, 14);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (2, 15);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (2, 16);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (2, 17);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (2, 18);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (2, 19);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (2, 20);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (2, 21);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (2, 22);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (2, 23);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (2, 24);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (2, 25);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (2, 26);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (2, 27);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (2, 28);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (2, 29);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (2, 30);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (2, 31);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (2, 32);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (2, 33);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (2, 34);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (2, 35);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (2, 36);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (2, 37);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (2, 38);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (2, 39);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (2, 40);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (2, 41);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (2, 42);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (2, 43);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (2, 44);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (2, 45);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (2, 46);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (2, 47);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (2, 48);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (2, 49);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (2, 50);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (2, 51);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (2, 52);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (2, 53);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (2, 54);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (2, 55);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (2, 56);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (2, 57);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (2, 58);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (2, 59);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (2, 60);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (2, 61);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (3, 1);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (111, 1);

INSERT INTO sys_user (user_id, post_id, role_id, dept_id, username, nickname, user_type, email, phone_number, sex, avatar, password, status, login_ip, login_date, is_admin, creator_id, create_time, updater_id, update_time, remark, deleted) VALUES (1, 1, 1, 4, 'admin', 'valarchie1', 0, 'healthtrail@163.com', '15888888883', 0, '/profile/avatar/20230725164110_blob_6b7a989b1cdd4dd396665d2cfd2addc5.png', '$2a$10$o55UFZAtyWnDpRV6dvQe8.c/MjlFacC49ASj2usNXm9BY74SYI/uG', 1, '127.0.0.1', '2023-08-14 23:07:03', TRUE, NULL, '2022-05-21 08:30:54', 1, '2023-08-14 23:07:03', '管理员', 0);
INSERT INTO sys_user (user_id, post_id, role_id, dept_id, username, nickname, user_type, email, phone_number, sex, avatar, password, status, login_ip, login_date, is_admin, creator_id, create_time, updater_id, update_time, remark, deleted) VALUES (2, 2, 2, 5, 'ag1', 'valarchie2', 0, 'healthtrail1@qq.com', '15666666666', 1, '/profile/avatar/20230725114818_avatar_b5bf400732bb43369b4df58802049b22.png', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', 1, '127.0.0.1', '2022-05-21 08:30:54', FALSE, NULL, '2022-05-21 08:30:54', NULL, NULL, '测试员1', 0);
INSERT INTO sys_user (user_id, post_id, role_id, dept_id, username, nickname, user_type, email, phone_number, sex, avatar, password, status, login_ip, login_date, is_admin, creator_id, create_time, updater_id, update_time, remark, deleted) VALUES (3, 2, 0, 5, 'ag2', 'valarchie3', 0, 'healthtrail2@qq.com', '15666666667', 1, '/profile/avatar/20230725114818_avatar_b5bf400732bb43369b4df58802049b22.png', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', 1, '127.0.0.1', '2022-05-21 08:30:54', FALSE, NULL, '2022-05-21 08:30:54', NULL, NULL, '测试员2', 0);

-- 健康业务后台菜单。只放当前前端已有页面，避免动态路由找不到组件。
INSERT INTO sys_menu (menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted) VALUES
(100, '健康系统', 2, '', 0, '/health', FALSE, '', '{"title":"健康系统","icon":"ep:first-aid-kit","showParent":true,"rank":10}', 1, '健康系统后台目录菜单', 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, 0),
(101, '系统药品管理', 1, 'HealthDrug', 100, '/health/drug', FALSE, 'health:drug:list', '{"title":"系统药品管理","showParent":true}', 1, '健康系统后台系统药品管理菜单', 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, 0),
(102, '药品单位管理', 1, 'DrugUnit', 100, '/health/drugUnit', FALSE, 'health:drugUnit:list', '{"title":"药品单位管理","showParent":true}', 1, '健康系统后台药品单位管理菜单', 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, 0),
(103, '消息审计', 1, 'HealthMessage', 100, '/health/message', FALSE, 'health:message:list', '{"title":"消息审计","showParent":true}', 1, '健康系统后台消息审计菜单', 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, 0),
(104, '指标模板管理', 1, 'HealthIndicatorTemplate', 100, '/health/indicatorTemplate', FALSE, 'health:indicatorTemplate:list', '{"title":"指标模板管理","showParent":true}', 1, '健康系统后台指标模板管理菜单', 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, 0),
(105, '家庭共享管理', 1, 'HealthFamilyShare', 100, '/health/familyShare', FALSE, 'health:familyShare:list', '{"title":"家庭共享管理","showParent":true}', 1, '健康系统后台家庭共享管理菜单', 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, 0),
(106, '用药历史与依从率', 1, 'HealthMedicationHistory', 100, '/health/medicationHistory', FALSE, 'health:medicationHistory:list', '{"title":"用药历史与依从率","showParent":true}', 1, '健康系统后台用药历史与依从率管理菜单', 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, 0),
(107, '首页运营任务管理', 1, 'HealthOperationTask', 100, '/health/operationTask', FALSE, 'health:operationTask:list', '{"title":"首页运营任务管理","showParent":true}', 1, '健康系统后台首页运营任务管理菜单', 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, 0),
(108, '体检报告管理', 1, 'HealthReport', 100, '/health/report', FALSE, 'health:report:list', '{"title":"体检报告管理","showParent":true}', 1, '健康系统后台体检报告管理菜单', 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, 0),
(109, '慢病病种配置', 1, 'HealthChronicDiseaseType', 100, '/health/chronicDiseaseType', FALSE, 'health:chronicDiseaseType:list', '{"title":"慢病病种配置","showParent":true}', 1, '健康系统后台慢病病种配置菜单', 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, 0);

INSERT INTO sys_menu (menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted) VALUES
(110, '药品详情', 0, ' ', 101, '', TRUE, 'health:drug:query', '{}', 1, '系统药品管理-详情按钮权限', 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, 0),
(111, '药品新增', 0, ' ', 101, '', TRUE, 'health:drug:add', '{}', 1, '系统药品管理-新增按钮权限', 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, 0),
(112, '药品修改', 0, ' ', 101, '', TRUE, 'health:drug:edit', '{}', 1, '系统药品管理-修改按钮权限', 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, 0),
(113, '药品删除', 0, ' ', 101, '', TRUE, 'health:drug:remove', '{}', 1, '系统药品管理-删除按钮权限', 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, 0),
(114, '单位详情', 0, ' ', 102, '', TRUE, 'health:drugUnit:query', '{}', 1, '药品单位管理-详情按钮权限', 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, 0),
(115, '单位新增', 0, ' ', 102, '', TRUE, 'health:drugUnit:add', '{}', 1, '药品单位管理-新增按钮权限', 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, 0),
(116, '单位修改', 0, ' ', 102, '', TRUE, 'health:drugUnit:edit', '{}', 1, '药品单位管理-修改按钮权限', 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, 0),
(117, '单位删除', 0, ' ', 102, '', TRUE, 'health:drugUnit:remove', '{}', 1, '药品单位管理-删除按钮权限', 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, 0),
(118, '消息详情', 0, ' ', 103, '', TRUE, 'health:message:query', '{}', 1, '消息审计-详情按钮权限', 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, 0),
(119, '消息重发', 0, ' ', 103, '', TRUE, 'health:message:resend', '{}', 1, '消息审计-人工重发与批量重发按钮权限', 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, 0),
(120, '模板详情', 0, ' ', 104, '', TRUE, 'health:indicatorTemplate:query', '{}', 1, '指标模板管理-详情按钮权限', 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, 0),
(121, '模板新增', 0, ' ', 104, '', TRUE, 'health:indicatorTemplate:add', '{}', 1, '指标模板管理-新增按钮权限', 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, 0),
(122, '模板修改', 0, ' ', 104, '', TRUE, 'health:indicatorTemplate:edit', '{}', 1, '指标模板管理-修改按钮权限', 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, 0),
(123, '模板删除', 0, ' ', 104, '', TRUE, 'health:indicatorTemplate:remove', '{}', 1, '指标模板管理-删除按钮权限', 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, 0),
(124, '共享详情', 0, ' ', 105, '', TRUE, 'health:familyShare:query', '{}', 1, '家庭共享管理-详情按钮权限', 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, 0),
(125, '共享移除', 0, ' ', 105, '', TRUE, 'health:familyShare:remove', '{}', 1, '家庭共享管理-移除协同与取消邀请按钮权限', 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, 0),
(126, '依从率查询', 0, ' ', 106, '', TRUE, 'health:medicationHistory:query', '{}', 1, '用药历史与依从率管理-统计与趋势查询按钮权限', 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, 0),
(127, '任务详情', 0, ' ', 107, '', TRUE, 'health:operationTask:query', '{}', 1, '首页运营任务管理-详情按钮权限', 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, 0),
(128, '任务新增', 0, ' ', 107, '', TRUE, 'health:operationTask:add', '{}', 1, '首页运营任务管理-新增按钮权限', 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, 0),
(129, '任务修改', 0, ' ', 107, '', TRUE, 'health:operationTask:edit', '{}', 1, '首页运营任务管理-修改按钮权限', 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, 0),
(130, '任务删除', 0, ' ', 107, '', TRUE, 'health:operationTask:remove', '{}', 1, '首页运营任务管理-删除按钮权限', 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, 0),
(131, '报告详情', 0, ' ', 108, '', TRUE, 'health:report:query', '{}', 1, '体检报告管理-详情按钮权限', 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, 0),
(132, 'AI总结', 0, ' ', 108, '', TRUE, 'health:report:ai', '{}', 1, '体检报告管理-AI总结按钮权限', 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, 0),
(133, '报告删除', 0, ' ', 108, '', TRUE, 'health:report:remove', '{}', 1, '体检报告管理-删除按钮权限', 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, 0),
(134, '病种详情', 0, ' ', 109, '', TRUE, 'health:chronicDiseaseType:query', '{}', 1, '慢病病种配置-详情按钮权限', 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, 0),
(135, '新增病种', 0, ' ', 109, '', TRUE, 'health:chronicDiseaseType:add', '{}', 1, '慢病病种配置-新增按钮权限', 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, 0),
(136, '修改病种', 0, ' ', 109, '', TRUE, 'health:chronicDiseaseType:edit', '{}', 1, '慢病病种配置-修改按钮权限', 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, 0),
(137, '删除病种', 0, ' ', 109, '', TRUE, 'health:chronicDiseaseType:remove', '{}', 1, '慢病病种配置-删除按钮权限', 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, 0);

-- 会员管理后台菜单。
INSERT INTO sys_menu (menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted) VALUES
(300, '会员管理', 2, 'SystemMemberManagement', 1, '/system/member', FALSE, '', '{"title":"会员管理","icon":"ri:vip-crown-2-line","showParent":true,"rank":80}', 1, '会员管理目录', 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, 0),
(301, '会员等级管理', 1, 'SystemMemberLevels', 300, '/system/member-levels/index', FALSE, 'system:memberLevel:list', '{"title":"会员等级管理","showParent":true}', 1, '会员等级管理页面', 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, 0),
(302, '用户会员管理', 1, 'SystemUserMembers', 300, '/system/user-members/index', FALSE, 'system:userMember:list', '{"title":"用户会员管理","showParent":true}', 1, '用户会员管理页面', 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, 0),
(303, '会员兑换码管理', 1, 'SystemMemberRedeemCodes', 300, '/system/member-redeem-codes/index', FALSE, 'system:memberRedeemCode:list', '{"title":"会员兑换码管理","showParent":true}', 1, '会员兑换码管理页面', 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, 0),
(304, '会员权益定义', 1, 'SystemMemberFeatures', 300, '/system/member-features/index', FALSE, 'system:memberFeature:list', '{"title":"会员权益定义","showParent":true}', 1, '会员权益定义页面', 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, 0),
(305, '等级权益配置', 1, 'SystemMemberLevelFeatures', 300, '/system/member-level-features/index', FALSE, 'system:memberLevelFeature:query', '{"title":"等级权益配置","showParent":true}', 1, '会员等级权益配置页面', 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, 0),
(306, '会员门禁点管理', 1, 'SystemMemberGates', 300, '/system/member-gates/index', FALSE, 'system:memberGate:list', '{"title":"会员门禁点管理","showParent":true}', 1, '会员门禁点管理页面', 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, 0),
(307, '会员门禁规则管理', 1, 'SystemMemberGateRules', 300, '/system/member-gate-rules/index', FALSE, 'system:memberGateRule:list', '{"title":"会员门禁规则管理","showParent":true}', 1, '会员门禁规则管理页面', 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, 0);

INSERT INTO sys_menu (menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted) VALUES
(308, '会员等级查询', 0, ' ', 301, '', TRUE, 'system:memberLevel:list', '{}', 1, '会员等级查询按钮', 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, 0),
(309, '会员等级详情', 0, ' ', 301, '', TRUE, 'system:memberLevel:query', '{}', 1, '会员等级详情按钮', 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, 0),
(310, '会员等级新增', 0, ' ', 301, '', TRUE, 'system:memberLevel:add', '{}', 1, '会员等级新增按钮', 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, 0),
(311, '会员等级修改', 0, ' ', 301, '', TRUE, 'system:memberLevel:edit', '{}', 1, '会员等级修改按钮', 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, 0),
(312, '会员等级删除', 0, ' ', 301, '', TRUE, 'system:memberLevel:remove', '{}', 1, '会员等级删除按钮', 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, 0),
(313, '用户会员查询', 0, ' ', 302, '', TRUE, 'system:userMember:list', '{}', 1, '用户会员查询按钮', 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, 0),
(314, '用户会员详情', 0, ' ', 302, '', TRUE, 'system:userMember:query', '{}', 1, '用户会员详情按钮', 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, 0),
(315, '后台赠送会员', 0, ' ', 302, '', TRUE, 'system:userMember:grant', '{}', 1, '后台赠送会员按钮', 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, 0),
(316, '清空用户会员', 0, ' ', 302, '', TRUE, 'system:userMember:clear', '{}', 1, '清空用户会员按钮', 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, 0),
(317, '兑换码查询', 0, ' ', 303, '', TRUE, 'system:memberRedeemCode:list', '{}', 1, '会员兑换码查询按钮', 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, 0),
(318, '批量生成兑换码', 0, ' ', 303, '', TRUE, 'system:memberRedeemCode:generate', '{}', 1, '会员兑换码生成按钮', 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, 0),
(319, '删除兑换码', 0, ' ', 303, '', TRUE, 'system:memberRedeemCode:remove', '{}', 1, '会员兑换码删除按钮', 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, 0),
(320, '会员权益查询', 0, ' ', 304, '', TRUE, 'system:memberFeature:list', '{}', 1, '会员权益查询按钮', 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, 0),
(321, '会员权益详情', 0, ' ', 304, '', TRUE, 'system:memberFeature:query', '{}', 1, '会员权益详情按钮', 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, 0),
(322, '会员权益新增', 0, ' ', 304, '', TRUE, 'system:memberFeature:add', '{}', 1, '会员权益新增按钮', 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, 0),
(323, '会员权益修改', 0, ' ', 304, '', TRUE, 'system:memberFeature:edit', '{}', 1, '会员权益修改按钮', 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, 0),
(324, '会员权益删除', 0, ' ', 304, '', TRUE, 'system:memberFeature:remove', '{}', 1, '会员权益删除按钮', 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, 0),
(325, '等级权益矩阵查询', 0, ' ', 305, '', TRUE, 'system:memberLevelFeature:query', '{}', 1, '等级权益矩阵查询按钮', 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, 0),
(326, '等级权益矩阵保存', 0, ' ', 305, '', TRUE, 'system:memberLevelFeature:edit', '{}', 1, '等级权益矩阵保存按钮', 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, 0),
(327, '会员门禁点查询', 0, ' ', 306, '', TRUE, 'system:memberGate:list', '{}', 1, '会员门禁点查询按钮', 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, 0),
(328, '会员门禁点详情', 0, ' ', 306, '', TRUE, 'system:memberGate:query', '{}', 1, '会员门禁点详情按钮', 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, 0),
(329, '会员门禁点新增', 0, ' ', 306, '', TRUE, 'system:memberGate:add', '{}', 1, '会员门禁点新增按钮', 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, 0),
(330, '会员门禁点修改', 0, ' ', 306, '', TRUE, 'system:memberGate:edit', '{}', 1, '会员门禁点修改按钮', 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, 0),
(331, '会员门禁点删除', 0, ' ', 306, '', TRUE, 'system:memberGate:remove', '{}', 1, '会员门禁点删除按钮', 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, 0),
(332, '会员门禁规则查询', 0, ' ', 307, '', TRUE, 'system:memberGateRule:list', '{}', 1, '会员门禁规则查询按钮', 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, 0),
(333, '会员门禁规则详情', 0, ' ', 307, '', TRUE, 'system:memberGateRule:query', '{}', 1, '会员门禁规则详情按钮', 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, 0),
(334, '会员门禁规则保存', 0, ' ', 307, '', TRUE, 'system:memberGateRule:edit', '{}', 1, '会员门禁规则保存按钮', 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, 0);

INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(2, 100), (2, 101), (2, 102), (2, 103), (2, 104), (2, 105), (2, 106), (2, 107), (2, 108), (2, 109),
(2, 110), (2, 111), (2, 112), (2, 113), (2, 114), (2, 115), (2, 116), (2, 117), (2, 118), (2, 119),
(2, 120), (2, 121), (2, 122), (2, 123), (2, 124), (2, 125), (2, 126), (2, 127), (2, 128), (2, 129),
(2, 130), (2, 131), (2, 132), (2, 133), (2, 134), (2, 135), (2, 136), (2, 137),
(2, 300), (2, 301), (2, 302), (2, 303), (2, 304), (2, 305), (2, 306), (2, 307), (2, 308), (2, 309),
(2, 310), (2, 311), (2, 312), (2, 313), (2, 314), (2, 315), (2, 316), (2, 317), (2, 318), (2, 319),
(2, 320), (2, 321), (2, 322), (2, 323), (2, 324), (2, 325), (2, 326), (2, 327), (2, 328), (2, 329),
(2, 330), (2, 331), (2, 332), (2, 333), (2, 334);

-- 回填自增列的起始值，确保测试中继续插入数据时不会和种子数据主键冲突。
ALTER TABLE sys_config ALTER COLUMN config_id RESTART WITH 6;
ALTER TABLE sys_dept ALTER COLUMN dept_id RESTART WITH 11;
ALTER TABLE sys_login_info ALTER COLUMN info_id RESTART WITH 418;
ALTER TABLE sys_menu ALTER COLUMN menu_id RESTART WITH 400;
ALTER TABLE sys_notice ALTER COLUMN notice_id RESTART WITH 3;
ALTER TABLE sys_operation_log ALTER COLUMN operation_id RESTART WITH 562;
ALTER TABLE sys_post ALTER COLUMN post_id RESTART WITH 5;
ALTER TABLE sys_role ALTER COLUMN role_id RESTART WITH 4;
ALTER TABLE sys_user ALTER COLUMN user_id RESTART WITH 4;
