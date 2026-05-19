
-- HealthTrail PostgreSQL 一体化初始化脚本
--
-- 说明：
-- 1. 本脚本已经合并基础系统表、基础种子数据、健康业务表、健康业务默认数据
-- 2. 新环境初始化时不再需要额外先执行 healthtrail_schema.sql / healthtrail_data.sql
-- 3. 后台健康菜单权限仍然单独保留在 健康系统_后台管理菜单权限_20260423.sql
-- 4. 当前直接使用 PostgreSQL 默认 schema，不再额外创建 app schema

-- ==============================
-- 一、基础系统表结构
-- ==============================

-- 创建表 sys_config 参数配置表
CREATE TABLE sys_config (
            config_id SERIAL PRIMARY KEY NOT NULL, -- 参数主键；代码 SysConfigEntity.configId 为 Integer，使用 32 位自增
            config_name VARCHAR(128) NOT NULL DEFAULT '', -- 配置名称
            config_key VARCHAR(128) NOT NULL, -- 配置键名
            config_options VARCHAR(1024) NOT NULL DEFAULT '', -- 可选的选项
            config_value VARCHAR(256) NOT NULL DEFAULT '', -- 配置值
            is_allow_change bool NOT NULL, -- 是否允许修改
            creator_id int8, -- 创建者ID
            updater_id int8, -- 更新者ID
            update_time TIMESTAMPTZ, -- 更新时间
            create_time TIMESTAMPTZ, -- 创建时间
            remark VARCHAR(128), -- 备注
            deleted int2 DEFAULT 0 NOT NULL -- 逻辑删除
);

-- 创建表 sys_dept 部门表
CREATE TABLE  sys_dept (
           dept_id serial8 PRIMARY KEY NOT NULL, -- 部门id
           parent_id int8 NOT NULL DEFAULT 0, -- 父部门id
           ancestors TEXT NOT NULL, -- 祖级列表
           dept_name VARCHAR(64) NOT NULL DEFAULT '', -- 部门名称
           order_num INT NOT NULL DEFAULT 0, -- 显示顺序
           leader_id int8, -- 负责人
           leader_name VARCHAR(64), -- 负责人姓名
           phone VARCHAR(16), -- 联系电话
           email VARCHAR(128), -- 邮箱
           status SMALLINT NOT NULL DEFAULT 0, -- 部门状态（0停用 1启用）
           creator_id int8, -- 创建者ID
           create_time TIMESTAMPTZ, -- 创建时间
           updater_id int8, -- 更新者ID
           update_time TIMESTAMPTZ, -- 更新时间
           deleted int2 DEFAULT 0 NOT NULL -- 逻辑删除
);

-- 创建表 sys_login_info 系统访问记录
CREATE TABLE sys_login_info (
            info_id serial8  PRIMARY KEY NOT NULL, -- 访问ID
            username VARCHAR(50) NOT NULL DEFAULT '', -- 用户账号
            ip_address VARCHAR(128) NOT NULL DEFAULT '', -- 登录IP地址
            login_location VARCHAR(255) NOT NULL DEFAULT '', -- 登录地点
            browser VARCHAR(50) NOT NULL DEFAULT '', -- 浏览器类型
            operation_system VARCHAR(50) NOT NULL DEFAULT '', -- 操作系统
            status int2 NOT NULL DEFAULT 0, -- 登录状态（1成功 0失败）
            msg VARCHAR(255) NOT NULL DEFAULT '', -- 提示消息
            login_time TIMESTAMPTZ, -- 访问时间
            deleted int2 DEFAULT 0 NOT NULL -- 逻辑删除
);


-- 创建表 sys_notice 通知公告表
CREATE TABLE sys_notice (
            notice_id SERIAL PRIMARY KEY NOT NULL, -- 公告ID；代码 SysNoticeEntity.noticeId 为 Integer，使用 32 位自增
            notice_title VARCHAR(64) NOT NULL, -- 公告标题
            notice_type int2 NOT NULL, -- 公告类型（1通知 2公告）
            notice_content TEXT, -- 公告内容
            status int2 NOT NULL DEFAULT 0, -- 公告状态（1正常 0关闭）
            creator_id int8 NOT NULL, -- 创建者ID
            create_time TIMESTAMPTZ, -- 创建时间
            updater_id int8, -- 更新者ID
            update_time TIMESTAMPTZ, -- 更新时间
            remark VARCHAR(255) NOT NULL DEFAULT '', -- 备注
            deleted int2 DEFAULT 0 NOT NULL -- 逻辑删除
);

-- 创建表 sys_operation_log 操作日志记录
CREATE TABLE  sys_operation_log (
            operation_id serial8 PRIMARY KEY NOT NULL, -- 日志主键
            business_type int2 NOT NULL DEFAULT 0, -- 业务类型（0其它 1新增 2修改 3删除）
            request_method int2 NOT NULL DEFAULT 0, -- 请求方式
            request_module VARCHAR(64) NOT NULL DEFAULT '', -- 请求模块
            request_url VARCHAR(256) NOT NULL DEFAULT '', -- 请求URL
            called_method VARCHAR(128) NOT NULL DEFAULT '', -- 调用方法
            operator_type int2 NOT NULL DEFAULT 0, -- 操作类别（0其它 1后台用户 2手机端用户）
            user_id int8, -- 用户ID
            username VARCHAR(32), -- 操作人员
            operator_ip VARCHAR(128), -- 操作人员ip
            operator_location VARCHAR(256), -- 操作地点
            dept_id int8, -- 部门ID
            dept_name VARCHAR(64), -- 部门名称
            operation_param VARCHAR(2048), -- 请求参数
            operation_result VARCHAR(2048), -- 返回参数
            status int2 NOT NULL DEFAULT 1, -- 操作状态（1正常 0异常）
            error_stack VARCHAR(2048), -- 错误消息
            operation_time TIMESTAMPTZ NOT NULL, -- 操作时间
            deleted int2 DEFAULT 0 NOT NULL -- 逻辑删除
);

-- 创建表 sys_post 岗位信息表
CREATE TABLE  sys_post (
           post_id serial8  PRIMARY KEY NOT NULL, -- 岗位ID
           post_code VARCHAR(64) NOT NULL, -- 岗位编码
           post_name VARCHAR(64) NOT NULL, -- 岗位名称
           post_sort INT NOT NULL, -- 显示顺序
           status int2 NOT NULL, -- 状态（1正常 0停用）
           remark VARCHAR(512), -- 备注
           creator_id int8, -- 创建者ID
           create_time TIMESTAMPTZ, -- 创建时间
           updater_id int8, -- 更新者ID
           update_time TIMESTAMPTZ, -- 更新时间
           deleted int2 DEFAULT 0 NOT NULL -- 逻辑删除
);


-- 创建表 sys_menu 菜单权限表
CREATE TABLE  sys_menu (
           menu_id serial8 PRIMARY KEY NOT NULL, -- 菜单ID
           menu_name VARCHAR(64) NOT NULL, -- 菜单名称
           menu_type int2 NOT NULL DEFAULT 0, -- 菜单的类型(1为普通菜单2为目录3为内嵌iFrame4为外链跳转)
           router_name VARCHAR(255) NOT NULL DEFAULT '', -- 路由名称
           parent_id int8 NOT NULL DEFAULT 0, -- 父菜单ID
           path VARCHAR(255), -- 组件路径
           is_button bool NOT NULL DEFAULT false, -- 是否按钮
           permission VARCHAR(128), -- 权限标识
           meta_info VARCHAR(1024) NOT NULL DEFAULT '{}', -- 路由元信息
           status int2 NOT NULL DEFAULT 0, -- 菜单状态（1启用 0停用）
           remark VARCHAR(256), -- 备注
           creator_id int8, -- 创建者ID
           create_time TIMESTAMPTZ, -- 创建时间
           updater_id int8, -- 更新者ID
           update_time TIMESTAMPTZ, -- 更新时间
           deleted int2 DEFAULT 0 NOT NULL -- 逻辑删除
);

-- 创建表 sys_role 角色信息表
CREATE TABLE  sys_role (
           role_id serial8 PRIMARY KEY NOT NULL, -- 角色ID
           role_name VARCHAR(32) NOT NULL, -- 角色名称
           role_key VARCHAR(128) NOT NULL, -- 角色权限字符串
           role_sort INT NOT NULL, -- 显示顺序
           data_scope int2, -- 数据范围
           dept_id_set VARCHAR(1024) DEFAULT '', -- 角色所拥有的部门数据权限
           status int2 NOT NULL, -- 角色状态（1正常 0停用）
           creator_id int8, -- 创建者ID
           create_time TIMESTAMPTZ, -- 创建时间
           updater_id int8, -- 更新者ID
           update_time TIMESTAMPTZ, -- 更新时间
           remark VARCHAR(512), -- 备注
           deleted int2 DEFAULT 0 NOT NULL -- 删除标志（0代表存在 1代表删除）
);

-- 创建表 sys_role_menu 角色和菜单关联表
CREATE TABLE  sys_role_menu (
        role_id int8 NOT NULL, -- 角色ID
        menu_id int8 NOT NULL, -- 菜单ID
        PRIMARY KEY (role_id, menu_id) -- 设置复合主键
);


-- 创建表 sys_user 用户信息表
CREATE TABLE  sys_user (
       user_id serial8 PRIMARY KEY NOT NULL, -- 用户ID
       post_id int8, -- 职位id
       role_id int8, -- 角色id
       dept_id int8, -- 部门ID
       username VARCHAR(64) NOT NULL, -- 用户账号
       nickname VARCHAR(32) NOT NULL, -- 用户昵称
       user_type int2 DEFAULT 0, -- 用户类型（00系统用户）
       email VARCHAR(128), -- 用户邮箱
       phone_number VARCHAR(18), -- 手机号码
       sex int2, -- 用户性别（0男 1女 2未知）
       avatar VARCHAR(512), -- 头像地址
       password VARCHAR(128) NOT NULL, -- 密码
       status int2 NOT NULL, -- 帐号状态（1正常 2停用 3冻结）
       login_ip VARCHAR(128), -- 最后登录IP
       login_date TIMESTAMPTZ, -- 最后登录时间
       is_admin bool DEFAULT false NOT NULL,  -- 超级管理员标志（1是，0否）
       creator_id int8, -- 更新者ID
       create_time TIMESTAMPTZ, -- 创建时间
       updater_id int8, -- 更新者ID
       update_time TIMESTAMPTZ, -- 更新时间
       remark VARCHAR(512), -- 备注
       deleted int2 DEFAULT 0 NOT NULL -- 删除标志（0代表存在 1代表删除）
);


-- ==============================
-- 二、基础系统种子数据
-- ==============================



INSERT INTO  sys_config (config_id, config_name, config_key, config_options, config_value, is_allow_change, creator_id, updater_id, update_time, create_time, remark, deleted) VALUES (1, '主框架页-默认皮肤样式名称', 'sys.index.skinName', '["skin-blue","skin-green","skin-purple","skin-red","skin-yellow"]', 'skin-blue', true, null, null, '2022-08-28 22:12:19', '2022-05-21 08:30:55', '蓝色 skin-blue、绿色 skin-green、紫色 skin-purple、红色 skin-red、黄色 skin-yellow', 0);
INSERT INTO  sys_config (config_id, config_name, config_key, config_options, config_value, is_allow_change, creator_id, updater_id, update_time, create_time, remark, deleted) VALUES (2, '用户管理-账号初始密码', 'sys.user.initPassword', '', '123456', true, null, 1, '2023-07-20 14:42:08', '2022-05-21 08:30:55', '初始化密码 123456', 0);
INSERT INTO  sys_config (config_id, config_name, config_key, config_options, config_value, is_allow_change, creator_id, updater_id, update_time, create_time, remark, deleted) VALUES (3, '主框架页-侧边栏主题', 'sys.index.sideTheme', '["theme-dark","theme-light"]', 'theme-dark', true, null, null, '2022-08-28 22:12:15', '2022-08-20 08:30:55', '深色主题theme-dark，浅色主题theme-light', 0);
INSERT INTO  sys_config (config_id, config_name, config_key, config_options, config_value, is_allow_change, creator_id, updater_id, update_time, create_time, remark, deleted) VALUES (4, '账号自助-验证码开关', 'sys.account.captchaOnOff', '["true","false"]', 'false', false, null, 1, '2023-07-20 14:39:36', '2022-05-21 08:30:55', '是否开启验证码功能（true开启，false关闭）', 0);
INSERT INTO  sys_config (config_id, config_name, config_key, config_options, config_value, is_allow_change, creator_id, updater_id, update_time, create_time, remark, deleted) VALUES (5, '账号自助-是否开启用户注册功能', 'sys.account.registerUser', '["true","false"]', 'true', false, null, 1, '2022-10-05 22:18:57', '2022-05-21 08:30:55', '是否开启注册用户功能（true开启，false关闭）', 0);


INSERT INTO  sys_dept (dept_id, parent_id, ancestors, dept_name, order_num, leader_id, leader_name, phone, email, status, creator_id, create_time, updater_id, update_time, deleted) VALUES (1, 0, '0', 'HealthTrail科技', 0, null, 'valarchie', '15888888888', 'valarchie@163.com', 1, null, '2022-05-21 08:30:54', null, null, 0);
INSERT INTO  sys_dept (dept_id, parent_id, ancestors, dept_name, order_num, leader_id, leader_name, phone, email, status, creator_id, create_time, updater_id, update_time, deleted) VALUES (2, 1, '0,1', '深圳总公司', 1, null, 'valarchie', '15888888888', 'valarchie@163.com', 1, null, '2022-05-21 08:30:54', null, null, 0);
INSERT INTO  sys_dept (dept_id, parent_id, ancestors, dept_name, order_num, leader_id, leader_name, phone, email, status, creator_id, create_time, updater_id, update_time, deleted) VALUES (3, 1, '0,1', '长沙分公司', 2, null, 'valarchie', '15888888888', 'valarchie@163.com', 1, null, '2022-05-21 08:30:54', null, null, 0);
INSERT INTO  sys_dept (dept_id, parent_id, ancestors, dept_name, order_num, leader_id, leader_name, phone, email, status, creator_id, create_time, updater_id, update_time, deleted) VALUES (4, 2, '0,1,2', '研发部门', 1, null, 'valarchie', '15888888888', 'valarchie@163.com', 1, null, '2022-05-21 08:30:54', null, null, 0);
INSERT INTO  sys_dept (dept_id, parent_id, ancestors, dept_name, order_num, leader_id, leader_name, phone, email, status, creator_id, create_time, updater_id, update_time, deleted) VALUES (5, 2, '0,1,2', '市场部门', 2, null, 'valarchie', '15888888888', 'valarchie@163.com', 0, null, '2022-05-21 08:30:54', 1, '2023-07-20 22:46:41', 0);
INSERT INTO  sys_dept (dept_id, parent_id, ancestors, dept_name, order_num, leader_id, leader_name, phone, email, status, creator_id, create_time, updater_id, update_time, deleted) VALUES (6, 2, '0,1,2', '测试部门', 3, null, 'valarchie', '15888888888', 'valarchie@163.com', 1, null, '2022-05-21 08:30:54', null, null, 0);
INSERT INTO  sys_dept (dept_id, parent_id, ancestors, dept_name, order_num, leader_id, leader_name, phone, email, status, creator_id, create_time, updater_id, update_time, deleted) VALUES (7, 2, '0,1,2', '财务部门', 4, null, 'valarchie', '15888888888', 'valarchie@163.com', 1, null, '2022-05-21 08:30:54', null, null, 0);
INSERT INTO  sys_dept (dept_id, parent_id, ancestors, dept_name, order_num, leader_id, leader_name, phone, email, status, creator_id, create_time, updater_id, update_time, deleted) VALUES (8, 2, '0,1,2', '运维部门', 5, null, 'valarchie', '15888888888', 'valarchie@163.com', 1, null, '2022-05-21 08:30:54', null, null, 0);
INSERT INTO  sys_dept (dept_id, parent_id, ancestors, dept_name, order_num, leader_id, leader_name, phone, email, status, creator_id, create_time, updater_id, update_time, deleted) VALUES (9, 3, '0,1,3', '市场部!', 1, null, 'valarchie!!', '15888188888', 'valarc1hie@163.com', 0, null, '2022-05-21 08:30:54', 1, '2023-07-20 22:33:31', 0);
INSERT INTO  sys_dept (dept_id, parent_id, ancestors, dept_name, order_num, leader_id, leader_name, phone, email, status, creator_id, create_time, updater_id, update_time, deleted) VALUES (10, 3, '0,1,3', '财务部门', 2, null, 'valarchie', '15888888888', 'valarchie@163.com', 0, null, '2022-05-21 08:30:54', null, null, 0);


INSERT INTO  sys_login_info (info_id, username, ip_address, login_location, browser, operation_system, status, msg, login_time, deleted) VALUES (415, 'admin', '127.0.0.1', '内网IP', 'Chrome 11', 'Mac OS X', 1, '登录成功', '2023-06-29 22:49:37', 0);
INSERT INTO  sys_login_info (info_id, username, ip_address, login_location, browser, operation_system, status, msg, login_time, deleted) VALUES (416, 'admin', '127.0.0.1', '内网IP', 'Chrome 11', 'Mac OS X', 1, '登录成功', '2023-07-02 22:12:30', 0);
INSERT INTO  sys_login_info (info_id, username, ip_address, login_location, browser, operation_system, status, msg, login_time, deleted) VALUES (417, 'admin', '127.0.0.1', '内网IP', 'Chrome 11', 'Mac OS X', 0, '验证码过期', '2023-07-02 22:16:06', 0);


INSERT INTO  sys_menu (menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted) VALUES (1, '系统管理', 2, '', 0, '/system', false, '', '{"title":"系统管理","icon":"ep:management","showParent":true,"rank":1}', 1, '系统管理目录', 0, '2022-05-21 08:30:54', 1, '2023-08-14 23:08:50', 0);
INSERT INTO  sys_menu (menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted) VALUES (2, '系统监控', 2, '', 0, '/monitor', false, '', '{"title":"系统监控","icon":"ep:monitor","showParent":true,"rank":3}', 1, '系统监控目录', 0, '2022-05-21 08:30:54', 1, '2023-08-14 23:09:15', 0);
INSERT INTO  sys_menu (menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted) VALUES (3, '系统工具', 2, '', 0, '/tool', false, '', '{"title":"系统工具","icon":"ep:tools","showParent":true,"rank":2}', 1, '系统工具目录', 0, '2022-05-21 08:30:54', 1, '2023-08-14 23:09:03', 0);
-- 菜单路由名和路径也需要跟随品牌改造统一，否则健康系统 PostgreSQL 新库初始化后仍会残留旧品牌标识。
INSERT INTO  sys_menu (menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted) VALUES (4, 'HealthTrail官网', 3, 'HealthTrailguanwangIframeRouter', 0, '/HealthTrailguanwangIframeLink', false, '', '{"title":"HealthTrail官网","icon":"ep:link","showParent":true,"frameSrc":"https://element-plus.org/zh-CN/","rank":8}', 1, 'HealthTrail官网地址', 0, '2022-05-21 08:30:54', 1, '2023-08-14 23:09:40', 0);
INSERT INTO  sys_menu (menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted) VALUES (5, '用户管理', 1, 'SystemUser', 1, '/system/user/index', false, 'system:user:list', '{"title":"用户管理","icon":"ep:user-filled","showParent":true}', 1, '用户管理菜单', 0, '2022-05-21 08:30:54', 1, '2023-08-14 23:16:13', 0);
INSERT INTO  sys_menu (menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted) VALUES (6, '角色管理', 1, 'SystemRole', 1, '/system/role/index', false, 'system:role:list', '{"title":"角色管理","icon":"ep:user","showParent":true}', 1, '角色管理菜单', 0, '2022-05-21 08:30:54', 1, '2023-08-14 23:16:23', 0);
INSERT INTO  sys_menu (menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted) VALUES (7, '菜单管理', 1, 'MenuManagement', 1, '/system/menu/index', false, 'system:menu:list', '{"title":"菜单管理","icon":"ep:menu","showParent":true}', 1, '菜单管理菜单', 0, '2022-05-21 08:30:54', 1, '2023-08-14 23:15:41', 0);
INSERT INTO  sys_menu (menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted) VALUES (8, '部门管理', 1, 'Department', 1, '/system/dept/index', false, 'system:dept:list', '{"title":"部门管理","icon":"fa-solid:code-branch","showParent":true}', 1, '部门管理菜单', 0, '2022-05-21 08:30:54', 1, '2023-08-14 23:15:35', 0);
INSERT INTO  sys_menu (menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted) VALUES (9, '岗位管理', 1, 'Post', 1, '/system/post/index', false, 'system:post:list', '{"title":"岗位管理","icon":"ep:postcard","showParent":true}', 1, '岗位管理菜单', 0, '2022-05-21 08:30:54', 1, '2023-08-14 23:15:11', 0);
INSERT INTO  sys_menu (menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted) VALUES (10, '参数设置', 1, 'Config', 1, '/system/config/index', false, 'system:config:list', '{"title":"参数设置","icon":"ep:setting","showParent":true}', 1, '参数设置菜单', 0, '2022-05-21 08:30:54', 1, '2023-08-14 23:15:03', 0);
INSERT INTO  sys_menu (menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted) VALUES (11, '通知公告', 1, 'SystemNotice', 1, '/system/notice/index', false, 'system:notice:list', '{"title":"通知公告","icon":"ep:notification","showParent":true}', 1, '通知公告菜单', 0, '2022-05-21 08:30:54', 1, '2023-08-14 23:14:56', 0);
INSERT INTO  sys_menu (menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted) VALUES (12, '日志管理', 1, 'LogManagement', 1, '/system/logd', false, '', '{"title":"日志管理","icon":"ep:document","showParent":true}', 1, '日志管理菜单', 0, '2022-05-21 08:30:54', 1, '2023-08-14 23:14:47', 0);
INSERT INTO  sys_menu (menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted) VALUES (13, '在线用户', 1, 'OnlineUser', 2, '/system/monitor/onlineUser/index', false, 'monitor:online:list', '{"title":"在线用户","icon":"fa-solid:users","showParent":true}', 1, '在线用户菜单', 0, '2022-05-21 08:30:54', 1, '2023-08-14 23:13:13', 0);
INSERT INTO  sys_menu (menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted) VALUES (14, '数据监控', 1, 'DataMonitor', 2, '/system/monitor/druid/index', false, 'monitor:druid:list', '{"title":"数据监控","icon":"fa:database","showParent":true,"frameSrc":"/druid/login.html","isFrameSrcInternal":true}', 1, '数据监控菜单', 0, '2022-05-21 08:30:54', 1, '2023-08-14 23:13:25', 0);
INSERT INTO  sys_menu (menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted) VALUES (15, '服务监控', 1, 'ServerInfo', 2, '/system/monitor/server/index', false, 'monitor:server:list', '{"title":"服务监控","icon":"fa:server","showParent":true}', 1, '服务监控菜单', 0, '2022-05-21 08:30:54', 1, '2023-08-14 23:13:34', 0);
INSERT INTO  sys_menu (menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted) VALUES (16, '缓存监控', 1, 'CacheInfo', 2, '/system/monitor/cache/index', false, 'monitor:cache:list', '{"title":"缓存监控","icon":"ep:reading","showParent":true}', 1, '缓存监控菜单', 0, '2022-05-21 08:30:54', 1, '2023-08-14 23:12:59', 0);
INSERT INTO  sys_menu (menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted) VALUES (17, '系统接口', 1, 'SystemAPI', 3, '/tool/swagger/index', false, 'tool:swagger:list', '{"title":"系统接口","icon":"ep:document-remove","showParent":true,"frameSrc":"/swagger-ui/index.html","isFrameSrcInternal":true}', 1, '系统接口菜单', 0, '2022-05-21 08:30:54', 1, '2023-08-14 23:14:01', 0);
INSERT INTO  sys_menu (menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted) VALUES (18, '操作日志', 1, 'OperationLog', 12, '/system/log/operationLog/index', false, 'monitor:operlog:list', '{"title":"操作日志"}', 1, '操作日志菜单', 0, '2022-05-21 08:30:54', null, null, 0);
INSERT INTO  sys_menu (menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted) VALUES (19, '登录日志', 1, 'LoginLog', 12, '/system/log/loginLog/index', false, 'monitor:logininfor:list', '{"title":"登录日志"}', 1, '登录日志菜单', 0, '2022-05-21 08:30:54', null, null, 0);
INSERT INTO  sys_menu (menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted) VALUES (20, '用户查询', 0, ' ', 5, '', true, 'system:user:query', '{"title":"用户查询"}', 1, '', 0, '2022-05-21 08:30:54', null, null, 0);
INSERT INTO  sys_menu (menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted) VALUES (21, '用户新增', 0, ' ', 5, '', true, 'system:user:add', '{"title":"用户新增"}', 1, '', 0, '2022-05-21 08:30:54', null, null, 0);
INSERT INTO  sys_menu (menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted) VALUES (22, '用户修改', 0, ' ', 5, '', true, 'system:user:edit', '{"title":"用户修改"}', 1, '', 0, '2022-05-21 08:30:54', null, null, 0);
INSERT INTO  sys_menu (menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted) VALUES (23, '用户删除', 0, ' ', 5, '', true, 'system:user:remove', '{"title":"用户删除"}', 1, '', 0, '2022-05-21 08:30:54', null, null, 0);
INSERT INTO  sys_menu (menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted) VALUES (24, '用户导出', 0, ' ', 5, '', true, 'system:user:export', '{"title":"用户导出"}', 1, '', 0, '2022-05-21 08:30:54', null, null, 0);
INSERT INTO  sys_menu (menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted) VALUES (25, '用户导入', 0, ' ', 5, '', true, 'system:user:import', '{"title":"用户导入"}', 1, '', 0, '2022-05-21 08:30:54', null, null, 0);
INSERT INTO  sys_menu (menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted) VALUES (26, '重置密码', 0, ' ', 5, '', true, 'system:user:resetPwd', '{"title":"重置密码"}', 1, '', 0, '2022-05-21 08:30:54', null, null, 0);
INSERT INTO  sys_menu (menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted) VALUES (27, '角色查询', 0, ' ', 6, '', true, 'system:role:query', '{"title":"角色查询"}', 1, '', 0, '2022-05-21 08:30:54', null, null, 0);
INSERT INTO  sys_menu (menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted) VALUES (28, '角色新增', 0, ' ', 6, '', true, 'system:role:add', '{"title":"角色新增"}', 1, '', 0, '2022-05-21 08:30:54', null, null, 0);
INSERT INTO  sys_menu (menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted) VALUES (29, '角色修改', 0, ' ', 6, '', true, 'system:role:edit', '{"title":"角色修改"}', 1, '', 0, '2022-05-21 08:30:54', null, null, 0);
INSERT INTO  sys_menu (menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted) VALUES (30, '角色删除', 0, ' ', 6, '', true, 'system:role:remove', '{"title":"角色删除"}', 1, '', 0, '2022-05-21 08:30:54', null, null, 0);
INSERT INTO  sys_menu (menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted) VALUES (31, '角色导出', 0, ' ', 6, '', true, 'system:role:export', '{"title":"角色导出"}', 1, '', 0, '2022-05-21 08:30:54', null, null, 0);
INSERT INTO  sys_menu (menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted) VALUES (32, '菜单查询', 0, ' ', 7, '', true, 'system:menu:query', '{"title":"菜单查询"}', 1, '', 0, '2022-05-21 08:30:54', null, null, 0);
INSERT INTO  sys_menu (menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted) VALUES (33, '菜单新增', 0, ' ', 7, '', true, 'system:menu:add', '{"title":"菜单新增"}', 1, '', 0, '2022-05-21 08:30:54', null, null, 0);
INSERT INTO  sys_menu (menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted) VALUES (34, '菜单修改', 0, ' ', 7, '', true, 'system:menu:edit', '{"title":"菜单修改"}', 1, '', 0, '2022-05-21 08:30:54', null, null, 0);
INSERT INTO  sys_menu (menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted) VALUES (35, '菜单删除', 0, ' ', 7, '', true, 'system:menu:remove', '{"title":"菜单删除"}', 1, '', 0, '2022-05-21 08:30:54', null, null, 0);
INSERT INTO  sys_menu (menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted) VALUES (36, '部门查询', 0, ' ', 8, '', true, 'system:dept:query', '{"title":"部门查询"}', 1, '', 0, '2022-05-21 08:30:54', null, null, 0);
INSERT INTO  sys_menu (menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted) VALUES (37, '部门新增', 0, ' ', 8, '', true, 'system:dept:add', '{"title":"部门新增"}', 1, '', 0, '2022-05-21 08:30:54', null, null, 0);
INSERT INTO  sys_menu (menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted) VALUES (38, '部门修改', 0, ' ', 8, '', true, 'system:dept:edit', '{"title":"部门修改"}', 1, '', 0, '2022-05-21 08:30:54', null, null, 0);
INSERT INTO  sys_menu (menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted) VALUES (39, '部门删除', 0, ' ', 8, '', true, 'system:dept:remove', '{"title":"部门删除"}', 1, '', 0, '2022-05-21 08:30:54', null, null, 0);
INSERT INTO  sys_menu (menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted) VALUES (40, '岗位查询', 0, ' ', 9, '', true, 'system:post:query', '{"title":"岗位查询"}', 1, '', 0, '2022-05-21 08:30:54', null, null, 0);
INSERT INTO  sys_menu (menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted) VALUES (41, '岗位新增', 0, ' ', 9, '', true, 'system:post:add', '{"title":"岗位新增"}', 1, '', 0, '2022-05-21 08:30:54', null, null, 0);
INSERT INTO  sys_menu (menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted) VALUES (42, '岗位修改', 0, ' ', 9, '', true, 'system:post:edit', '{"title":"岗位修改"}', 1, '', 0, '2022-05-21 08:30:54', null, null, 0);
INSERT INTO  sys_menu (menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted) VALUES (43, '岗位删除', 0, ' ', 9, '', true, 'system:post:remove', '{"title":"岗位删除"}', 1, '', 0, '2022-05-21 08:30:54', null, null, 0);
INSERT INTO  sys_menu (menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted) VALUES (44, '岗位导出', 0, ' ', 9, '', true, 'system:post:export', '{"title":"岗位导出"}', 1, '', 0, '2022-05-21 08:30:54', null, null, 0);
INSERT INTO  sys_menu (menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted) VALUES (45, '参数查询', 0, ' ', 10, '', true, 'system:config:query', '{"title":"参数查询"}', 1, '', 0, '2022-05-21 08:30:54', null, null, 0);
INSERT INTO  sys_menu (menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted) VALUES (46, '参数新增', 0, ' ', 10, '', true, 'system:config:add', '{"title":"参数新增"}', 1, '', 0, '2022-05-21 08:30:54', null, null, 0);
INSERT INTO  sys_menu (menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted) VALUES (47, '参数修改', 0, ' ', 10, '', true, 'system:config:edit', '{"title":"参数修改"}', 1, '', 0, '2022-05-21 08:30:54', null, null, 0);
INSERT INTO  sys_menu (menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted) VALUES (48, '参数删除', 0, ' ', 10, '', true, 'system:config:remove', '{"title":"参数删除"}', 1, '', 0, '2022-05-21 08:30:54', null, null, 0);
INSERT INTO  sys_menu (menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted) VALUES (49, '参数导出', 0, ' ', 10, '', true, 'system:config:export', '{"title":"参数导出"}', 1, '', 0, '2022-05-21 08:30:54', null, null, 0);
INSERT INTO  sys_menu (menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted) VALUES (50, '公告查询', 0, ' ', 11, '', true, 'system:notice:query', '{"title":"公告查询"}', 1, '', 0, '2022-05-21 08:30:54', null, null, 0);
INSERT INTO  sys_menu (menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted) VALUES (51, '公告新增', 0, ' ', 11, '', true, 'system:notice:add', '{"title":"公告新增"}', 1, '', 0, '2022-05-21 08:30:54', null, null, 0);
INSERT INTO  sys_menu (menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted) VALUES (52, '公告修改', 0, ' ', 11, '', true, 'system:notice:edit', '{"title":"公告修改"}', 1, '', 0, '2022-05-21 08:30:54', null, null, 0);
INSERT INTO  sys_menu (menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted) VALUES (53, '公告删除', 0, ' ', 11, '', true, 'system:notice:remove', '{"title":"公告删除"}', 1, '', 0, '2022-05-21 08:30:54', null, null, 0);
INSERT INTO  sys_menu (menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted) VALUES (54, '操作查询', 0, ' ', 18, '', true, 'monitor:operlog:query', '{"title":"操作查询"}', 1, '', 0, '2022-05-21 08:30:54', null, null, 0);
INSERT INTO  sys_menu (menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted) VALUES (55, '操作删除', 0, ' ', 18, '', true, 'monitor:operlog:remove', '{"title":"操作删除"}', 1, '', 0, '2022-05-21 08:30:54', null, null, 0);
INSERT INTO  sys_menu (menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted) VALUES (56, '日志导出', 0, ' ', 18, '', true, 'monitor:operlog:export', '{"title":"日志导出"}', 1, '', 0, '2022-05-21 08:30:54', null, null, 0);
INSERT INTO  sys_menu (menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted) VALUES (57, '登录查询', 0, ' ', 19, '', true, 'monitor:logininfor:query', '{"title":"登录查询"}', 1, '', 0, '2022-05-21 08:30:54', null, null, 0);
INSERT INTO  sys_menu (menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted) VALUES (58, '登录删除', 0, ' ', 19, '', true, 'monitor:logininfor:remove', '{"title":"登录删除"}', 1, '', 0, '2022-05-21 08:30:54', null, null, 0);
INSERT INTO  sys_menu (menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted) VALUES (59, '日志导出', 0, ' ', 19, '', true, 'monitor:logininfor:export', '{"title":"日志导出","rank":22}', 1, '', 0, '2022-05-21 08:30:54', 1, '2023-07-22 17:02:28', 0);
INSERT INTO  sys_menu (menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted) VALUES (60, '在线查询', 0, ' ', 13, '', true, 'monitor:online:query', '{"title":"在线查询"}', 1, '', 0, '2022-05-21 08:30:54', null, null, 0);
INSERT INTO  sys_menu (menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted) VALUES (61, '批量强退', 0, ' ', 13, '', true, 'monitor:online:batchLogout', '{"title":"批量强退"}', 1, '', 0, '2022-05-21 08:30:54', null, null, 0);
INSERT INTO  sys_menu (menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted) VALUES (62, '单条强退', 0, ' ', 13, '', true, 'monitor:online:forceLogout', '{"title":"单条强退"}', 1, '', 0, '2022-05-21 08:30:54', null, null, 0);
INSERT INTO  sys_menu (menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted) VALUES (63, 'HealthTrail 项目主页', 4, 'https://healthtrail.local', 0, '/external', false, '', '{"title":"HealthTrail 项目主页","icon":"fa-solid:external-link-alt","showParent":true,"rank":9}', 1, 'HealthTrail 项目主页', 0, '2022-05-21 08:30:54', 1, '2023-08-14 23:12:13', 0);
INSERT INTO  sys_menu (menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted) VALUES (64, '首页', 2, '', 0, '/global', false, '121212', '{"title":"首页","showParent":true,"rank":3}', 1, '', 1, '2023-07-24 22:36:03', 1, '2023-07-24 22:38:37', 1);
INSERT INTO  sys_menu (menu_id, menu_name, menu_type, router_name, parent_id, path, is_button, permission, meta_info, status, remark, creator_id, create_time, updater_id, update_time, deleted) VALUES (65, '个人中心', 1, 'PersonalCenter', 2053, '/system/user/profile', false, '434sdf', '{"title":"个人中心","showParent":true,"rank":3}', 1, '', 1, '2023-07-24 22:36:55', null, null, 1);



INSERT INTO  sys_notice (notice_id, notice_title, notice_type, notice_content, status, creator_id, create_time, updater_id, update_time, remark, deleted) VALUES (1, '温馨提醒：2018-07-01 HealthTrail新版本发布啦', 2, '新版本内容~~~~~~~~~~', 1, 1, '2022-05-21 08:30:55', 1, '2022-08-29 20:12:37', '管理员', 0);
INSERT INTO  sys_notice (notice_id, notice_title, notice_type, notice_content, status, creator_id, create_time, updater_id, update_time, remark, deleted) VALUES (2, '维护通知：2018-07-01 HealthTrail系统凌晨维护', 1, '维护内容', 1, 1, '2022-05-21 08:30:55', null, null, '管理员', 0);



INSERT INTO  sys_operation_log (operation_id, business_type, request_method, request_module, request_url, called_method, operator_type, user_id, username, operator_ip, operator_location, dept_id, dept_name, operation_param, operation_result, status, error_stack, operation_time, deleted) VALUES (561, 1, 2, '菜单管理', '/system/menus', 'it.upos.builder.admin.controller.system.SysMenuController.add()', 1, 0, 'admin', '127.0.0.1', '内网IP', 0, null, '{"menuName":"","permission":"","parentId":2035,"path":"","isButton":false,"routerName":"","meta":{"showParent":true,"rank":0},"status":1},', '', 1, '', '2023-07-22 17:06:57', 0);



INSERT INTO  sys_post (post_id, post_code, post_name, post_sort, status, remark, creator_id, create_time, updater_id, update_time, deleted) VALUES (1, 'ceo', '董事长', 1, 1, '', null, '2022-05-21 08:30:54', null, null, 0);
INSERT INTO  sys_post (post_id, post_code, post_name, post_sort, status, remark, creator_id, create_time, updater_id, update_time, deleted) VALUES (2, 'se', '项目经理', 2, 1, '', null, '2022-05-21 08:30:54', null, null, 0);
INSERT INTO  sys_post (post_id, post_code, post_name, post_sort, status, remark, creator_id, create_time, updater_id, update_time, deleted) VALUES (3, 'hr', '人力资源', 3, 1, '', null, '2022-05-21 08:30:54', null, null, 0);
INSERT INTO  sys_post (post_id, post_code, post_name, post_sort, status, remark, creator_id, create_time, updater_id, update_time, deleted) VALUES (4, 'user', '普通员工', 5, 0, '', null, '2022-05-21 08:30:54', null, null, 0);



INSERT INTO  sys_role (role_id, role_name, role_key, role_sort, data_scope, dept_id_set, status, creator_id, create_time, updater_id, update_time, remark, deleted) VALUES (1, '超级管理员', 'admin', 1, 1, '', 1, null, '2022-05-21 08:30:54', null, null, '超级管理员', 0);
INSERT INTO  sys_role (role_id, role_name, role_key, role_sort, data_scope, dept_id_set, status, creator_id, create_time, updater_id, update_time, remark, deleted) VALUES (2, '普通角色', 'common', 3, 2, '', 1, null, '2022-05-21 08:30:54', null, null, '普通角色', 0);
INSERT INTO  sys_role (role_id, role_name, role_key, role_sort, data_scope, dept_id_set, status, creator_id, create_time, updater_id, update_time, remark, deleted) VALUES (3, '闲置角色', 'unused', 4, 2, '', 0, null, '2022-05-21 08:30:54', null, null, '未使用的角色', 0);



INSERT INTO  sys_role_menu (role_id, menu_id) VALUES (2, 1);
INSERT INTO  sys_role_menu (role_id, menu_id) VALUES (2, 2);
INSERT INTO  sys_role_menu (role_id, menu_id) VALUES (2, 3);
INSERT INTO  sys_role_menu (role_id, menu_id) VALUES (2, 4);
INSERT INTO  sys_role_menu (role_id, menu_id) VALUES (2, 5);
INSERT INTO  sys_role_menu (role_id, menu_id) VALUES (2, 6);
INSERT INTO  sys_role_menu (role_id, menu_id) VALUES (2, 7);
INSERT INTO  sys_role_menu (role_id, menu_id) VALUES (2, 8);
INSERT INTO  sys_role_menu (role_id, menu_id) VALUES (2, 9);
INSERT INTO  sys_role_menu (role_id, menu_id) VALUES (2, 10);
INSERT INTO  sys_role_menu (role_id, menu_id) VALUES (2, 11);
INSERT INTO  sys_role_menu (role_id, menu_id) VALUES (2, 12);
INSERT INTO  sys_role_menu (role_id, menu_id) VALUES (2, 13);
INSERT INTO  sys_role_menu (role_id, menu_id) VALUES (2, 14);
INSERT INTO  sys_role_menu (role_id, menu_id) VALUES (2, 15);
INSERT INTO  sys_role_menu (role_id, menu_id) VALUES (2, 16);
INSERT INTO  sys_role_menu (role_id, menu_id) VALUES (2, 17);
INSERT INTO  sys_role_menu (role_id, menu_id) VALUES (2, 18);
INSERT INTO  sys_role_menu (role_id, menu_id) VALUES (2, 19);
INSERT INTO  sys_role_menu (role_id, menu_id) VALUES (2, 20);
INSERT INTO  sys_role_menu (role_id, menu_id) VALUES (2, 21);
INSERT INTO  sys_role_menu (role_id, menu_id) VALUES (2, 22);
INSERT INTO  sys_role_menu (role_id, menu_id) VALUES (2, 23);
INSERT INTO  sys_role_menu (role_id, menu_id) VALUES (2, 24);
INSERT INTO  sys_role_menu (role_id, menu_id) VALUES (2, 25);
INSERT INTO  sys_role_menu (role_id, menu_id) VALUES (2, 26);
INSERT INTO  sys_role_menu (role_id, menu_id) VALUES (2, 27);
INSERT INTO  sys_role_menu (role_id, menu_id) VALUES (2, 28);
INSERT INTO  sys_role_menu (role_id, menu_id) VALUES (2, 29);
INSERT INTO  sys_role_menu (role_id, menu_id) VALUES (2, 30);
INSERT INTO  sys_role_menu (role_id, menu_id) VALUES (2, 31);
INSERT INTO  sys_role_menu (role_id, menu_id) VALUES (2, 32);
INSERT INTO  sys_role_menu (role_id, menu_id) VALUES (2, 33);
INSERT INTO  sys_role_menu (role_id, menu_id) VALUES (2, 34);
INSERT INTO  sys_role_menu (role_id, menu_id) VALUES (2, 35);
INSERT INTO  sys_role_menu (role_id, menu_id) VALUES (2, 36);
INSERT INTO  sys_role_menu (role_id, menu_id) VALUES (2, 37);
INSERT INTO  sys_role_menu (role_id, menu_id) VALUES (2, 38);
INSERT INTO  sys_role_menu (role_id, menu_id) VALUES (2, 39);
INSERT INTO  sys_role_menu (role_id, menu_id) VALUES (2, 40);
INSERT INTO  sys_role_menu (role_id, menu_id) VALUES (2, 41);
INSERT INTO  sys_role_menu (role_id, menu_id) VALUES (2, 42);
INSERT INTO  sys_role_menu (role_id, menu_id) VALUES (2, 43);
INSERT INTO  sys_role_menu (role_id, menu_id) VALUES (2, 44);
INSERT INTO  sys_role_menu (role_id, menu_id) VALUES (2, 45);
INSERT INTO  sys_role_menu (role_id, menu_id) VALUES (2, 46);
INSERT INTO  sys_role_menu (role_id, menu_id) VALUES (2, 47);
INSERT INTO  sys_role_menu (role_id, menu_id) VALUES (2, 48);
INSERT INTO  sys_role_menu (role_id, menu_id) VALUES (2, 49);
INSERT INTO  sys_role_menu (role_id, menu_id) VALUES (2, 50);
INSERT INTO  sys_role_menu (role_id, menu_id) VALUES (2, 51);
INSERT INTO  sys_role_menu (role_id, menu_id) VALUES (2, 52);
INSERT INTO  sys_role_menu (role_id, menu_id) VALUES (2, 53);
INSERT INTO  sys_role_menu (role_id, menu_id) VALUES (2, 54);
INSERT INTO  sys_role_menu (role_id, menu_id) VALUES (2, 55);
INSERT INTO  sys_role_menu (role_id, menu_id) VALUES (2, 56);
INSERT INTO  sys_role_menu (role_id, menu_id) VALUES (2, 57);
INSERT INTO  sys_role_menu (role_id, menu_id) VALUES (2, 58);
INSERT INTO  sys_role_menu (role_id, menu_id) VALUES (2, 59);
INSERT INTO  sys_role_menu (role_id, menu_id) VALUES (2, 60);
INSERT INTO  sys_role_menu (role_id, menu_id) VALUES (2, 61);
INSERT INTO  sys_role_menu (role_id, menu_id) VALUES (3, 1);
INSERT INTO  sys_role_menu (role_id, menu_id) VALUES (111, 1);


INSERT INTO  sys_user (user_id, post_id, role_id, dept_id, username, nickname, user_type, email, phone_number, sex, avatar, password, status, login_ip, login_date, is_admin, creator_id, create_time, updater_id, update_time, remark, deleted) VALUES (1, 1, 1, 4, 'admin', 'valarchie1', 0, 'healthtrail@163.com', '15888888883', 0, '/profile/avatar/20230725164110_blob_6b7a989b1cdd4dd396665d2cfd2addc5.png', '$2a$10$o55UFZAtyWnDpRV6dvQe8.c/MjlFacC49ASj2usNXm9BY74SYI/uG', 1, '127.0.0.1', '2023-08-14 23:07:03', true, null, '2022-05-21 08:30:54', 1, '2023-08-14 23:07:03', '管理员', 0);
INSERT INTO  sys_user (user_id, post_id, role_id, dept_id, username, nickname, user_type, email, phone_number, sex, avatar, password, status, login_ip, login_date, is_admin, creator_id, create_time, updater_id, update_time, remark, deleted) VALUES (2, 2, 2, 5, 'ag1', 'valarchie2', 0, 'healthtrail1@qq.com', '15666666666', 1, '/profile/avatar/20230725114818_avatar_b5bf400732bb43369b4df58802049b22.png', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', 1, '127.0.0.1', '2022-05-21 08:30:54', false, null, '2022-05-21 08:30:54', null, null, '测试员1', 0);
INSERT INTO  sys_user (user_id, post_id, role_id, dept_id, username, nickname, user_type, email, phone_number, sex, avatar, password, status, login_ip, login_date, is_admin, creator_id, create_time, updater_id, update_time, remark, deleted) VALUES (3, 2, 0, 5, 'ag2', 'valarchie3', 0, 'healthtrail2@qq.com', '15666666667', 1, '/profile/avatar/20230725114818_avatar_b5bf400732bb43369b4df58802049b22.png', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', 1, '127.0.0.1', '2022-05-21 08:30:54', false, null, '2022-05-21 08:30:54', null, null, '测试员2', 0);

-- 序列更新
select setval('sys_config_config_id_seq',COALESCE(max(config_id),1)) from sys_config;
select setval('sys_dept_dept_id_seq',COALESCE(max(dept_id),1)) from sys_dept;
select setval('sys_login_info_info_id_seq',COALESCE(max(info_id),1)) from sys_login_info;
select setval('sys_menu_menu_id_seq',COALESCE(max(menu_id),1)) from sys_menu;
select setval('sys_notice_notice_id_seq',COALESCE(max(notice_id),1)) from sys_notice;
select setval('sys_operation_log_operation_id_seq',COALESCE(max(operation_id),1)) from sys_operation_log;
select setval('sys_post_post_id_seq',COALESCE(max(post_id),1)) from sys_post;
select setval('sys_role_role_id_seq',COALESCE(max(role_id),1)) from sys_role;
select setval('sys_user_user_id_seq',COALESCE(max(user_id),1)) from sys_user;


-- ==============================
-- 三、健康业务表结构与默认数据
-- ==============================

-- 健康系统 PostgreSQL 业务表初始化脚本
--
-- 说明：
-- 1. 本脚本直接按“当前最终表结构”创建，不再拆成 MySQL 时代的基础表 + 增强 alter 表
-- 2. 当前直接使用 PostgreSQL 默认 schema，避免初始化脚本、连接串和测试环境出现额外 schema 分叉
-- 3. 该脚本适用于“新环境直接上 PostgreSQL”的初始化场景

CREATE TABLE IF NOT EXISTS app_user (
    user_id BIGSERIAL PRIMARY KEY,
    mobile VARCHAR(16) NOT NULL,
    nickname VARCHAR(64) NOT NULL,
    avatar VARCHAR(255),
    password VARCHAR(128) NOT NULL,
    status SMALLINT DEFAULT 1 NOT NULL,
    last_login_ip VARCHAR(128),
    last_login_time TIMESTAMPTZ,
    register_source VARCHAR(32) DEFAULT 'APP' NOT NULL,
    creator_id BIGINT,
    create_time TIMESTAMPTZ,
    updater_id BIGINT,
    update_time TIMESTAMPTZ,
    deleted SMALLINT DEFAULT 0 NOT NULL,
    CONSTRAINT uk_app_user_mobile UNIQUE (mobile)
);

CREATE INDEX IF NOT EXISTS idx_app_user_status ON app_user (status);

CREATE TABLE IF NOT EXISTS family_member (
    member_id BIGSERIAL PRIMARY KEY,
    member_code VARCHAR(32),
    owner_user_id BIGINT NOT NULL,
    member_name VARCHAR(64) NOT NULL,
    gender SMALLINT DEFAULT 0 NOT NULL,
    birthday DATE,
    relation_type VARCHAR(32) NOT NULL,
    height NUMERIC(5, 2),
    weight NUMERIC(5, 2),
    blood_type VARCHAR(16),
    allergy_history VARCHAR(500),
    chronic_history VARCHAR(500),
    remark VARCHAR(500),
    status SMALLINT DEFAULT 1 NOT NULL,
    creator_id BIGINT,
    create_time TIMESTAMPTZ,
    updater_id BIGINT,
    update_time TIMESTAMPTZ,
    deleted SMALLINT DEFAULT 0 NOT NULL
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_family_member_code ON family_member (member_code);
CREATE INDEX IF NOT EXISTS idx_family_member_owner_user_id ON family_member (owner_user_id);
CREATE INDEX IF NOT EXISTS idx_family_member_status ON family_member (status);

CREATE TABLE IF NOT EXISTS attachment (
    attachment_id BIGSERIAL PRIMARY KEY,
    owner_user_id BIGINT,
    attachment_type VARCHAR(30) NOT NULL,
    storage_provider VARCHAR(30) DEFAULT 'LOCAL' NOT NULL,
    file_url VARCHAR(500) NOT NULL,
    stored_file_name VARCHAR(255) NOT NULL,
    original_file_name VARCHAR(255),
    file_size BIGINT,
    file_extension VARCHAR(20),
    content_type VARCHAR(100),
    status SMALLINT DEFAULT 1 NOT NULL,
    remark VARCHAR(500),
    creator_id BIGINT,
    create_time TIMESTAMPTZ,
    updater_id BIGINT,
    update_time TIMESTAMPTZ,
    deleted SMALLINT DEFAULT 0 NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_attachment_owner_user_id ON attachment (owner_user_id);
CREATE INDEX IF NOT EXISTS idx_attachment_type ON attachment (attachment_type);
CREATE INDEX IF NOT EXISTS idx_attachment_status ON attachment (status);

CREATE TABLE IF NOT EXISTS drug_unit (
    unit_id BIGSERIAL PRIMARY KEY,
    unit_code VARCHAR(50) NOT NULL,
    unit_name VARCHAR(20) NOT NULL,
    unit_alias VARCHAR(200),
    precision_scale SMALLINT DEFAULT 0 NOT NULL,
    sort INT DEFAULT 0 NOT NULL,
    status SMALLINT DEFAULT 1 NOT NULL,
    icon_attachment_id BIGINT,
    remark VARCHAR(500),
    creator_id BIGINT,
    create_time TIMESTAMPTZ,
    updater_id BIGINT,
    update_time TIMESTAMPTZ,
    deleted SMALLINT DEFAULT 0 NOT NULL,
    CONSTRAINT uk_drug_unit_code UNIQUE (unit_code, deleted),
    CONSTRAINT uk_drug_unit_name UNIQUE (unit_name, deleted)
);

CREATE INDEX IF NOT EXISTS idx_drug_unit_status ON drug_unit (status);
CREATE INDEX IF NOT EXISTS idx_drug_unit_sort ON drug_unit (sort);
CREATE INDEX IF NOT EXISTS idx_drug_unit_icon_attachment_id ON drug_unit (icon_attachment_id);

CREATE TABLE IF NOT EXISTS drug (
    drug_id BIGSERIAL PRIMARY KEY,
    drug_code VARCHAR(32),
    owner_user_id BIGINT NOT NULL,
    drug_name VARCHAR(100) NOT NULL,
    generic_name VARCHAR(100),
    brand_name VARCHAR(100),
    dosage_form VARCHAR(50),
    specification VARCHAR(100),
    indication VARCHAR(1000),
    usage_instruction VARCHAR(1000),
    adverse_reaction VARCHAR(1000),
    contraindication VARCHAR(1000),
    manufacturer VARCHAR(200),
    drug_type VARCHAR(20),
    source_drug_id BIGINT,
    stock_unit_id BIGINT,
    stock_unit VARCHAR(20),
    image_attachment_id BIGINT,
    stock_alert_threshold NUMERIC(10, 2),
    low_stock_notified SMALLINT DEFAULT 0 NOT NULL,
    low_stock_notify_time TIMESTAMPTZ,
    status SMALLINT DEFAULT 1 NOT NULL,
    remark VARCHAR(500),
    creator_id BIGINT,
    create_time TIMESTAMPTZ,
    updater_id BIGINT,
    update_time TIMESTAMPTZ,
    deleted SMALLINT DEFAULT 0 NOT NULL,
    CONSTRAINT uk_drug_owner_name UNIQUE (owner_user_id, drug_name)
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_drug_code ON drug (drug_code);
CREATE INDEX IF NOT EXISTS idx_drug_owner_user_id ON drug (owner_user_id);
CREATE INDEX IF NOT EXISTS idx_drug_type ON drug (drug_type);
CREATE INDEX IF NOT EXISTS idx_drug_status ON drug (status);
CREATE INDEX IF NOT EXISTS idx_drug_stock_unit_id ON drug (stock_unit_id);
CREATE INDEX IF NOT EXISTS idx_drug_image_attachment_id ON drug (image_attachment_id);
CREATE UNIQUE INDEX IF NOT EXISTS uk_drug_owner_source ON drug (owner_user_id, source_drug_id);
CREATE INDEX IF NOT EXISTS idx_drug_stock_alert ON drug (owner_user_id, low_stock_notified, stock_alert_threshold);

CREATE TABLE IF NOT EXISTS drug_stock_batch (
    batch_id BIGSERIAL PRIMARY KEY,
    owner_user_id BIGINT NOT NULL,
    drug_id BIGINT NOT NULL,
    batch_no VARCHAR(50),
    expire_date DATE,
    is_default_batch SMALLINT DEFAULT 0 NOT NULL,
    stock_quantity NUMERIC(10, 2) NOT NULL,
    near_expire_notified SMALLINT DEFAULT 0 NOT NULL,
    near_expire_notify_time TIMESTAMPTZ,
    creator_id BIGINT,
    create_time TIMESTAMPTZ,
    updater_id BIGINT,
    update_time TIMESTAMPTZ,
    deleted SMALLINT DEFAULT 0 NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_drug_stock_batch_owner_user_id ON drug_stock_batch (owner_user_id);
CREATE INDEX IF NOT EXISTS idx_drug_stock_batch_drug_id ON drug_stock_batch (drug_id);
CREATE INDEX IF NOT EXISTS idx_drug_stock_batch_expire_date ON drug_stock_batch (expire_date);
CREATE INDEX IF NOT EXISTS idx_drug_stock_batch_batch_no ON drug_stock_batch (batch_no);
CREATE INDEX IF NOT EXISTS idx_drug_stock_batch_default_batch ON drug_stock_batch (is_default_batch);

INSERT INTO drug_unit (
    unit_id, unit_code, unit_name, unit_alias, precision_scale, sort, status,
    remark, creator_id, create_time, updater_id, update_time, deleted
)
VALUES
    (1, 'PIAN', '片', '片剂,tablet', 0, 10, 1, '系统初始化药品单位', NULL, NOW(), NULL, NOW(), 0),
    (2, 'LI', '粒', '胶囊,颗粒', 0, 20, 1, '系统初始化药品单位', NULL, NOW(), NULL, NOW(), 0),
    (3, 'ZHI', '支', '支装', 0, 30, 1, '系统初始化药品单位', NULL, NOW(), NULL, NOW(), 0),
    (4, 'DAI', '袋', '包,袋装', 0, 40, 1, '系统初始化药品单位', NULL, NOW(), NULL, NOW(), 0),
    (5, 'HE', '盒', '盒装', 0, 50, 1, '系统初始化药品单位', NULL, NOW(), NULL, NOW(), 0),
    (6, 'PING', '瓶', '瓶装', 0, 60, 1, '系统初始化药品单位', NULL, NOW(), NULL, NOW(), 0),
    (7, 'ML', 'ml', '毫升,ML', 2, 70, 1, '系统初始化药品单位', NULL, NOW(), NULL, NOW(), 0),
    (8, 'G', 'g', '克,G', 2, 80, 1, '系统初始化药品单位', NULL, NOW(), NULL, NOW(), 0)
ON CONFLICT (unit_id) DO NOTHING;

INSERT INTO drug (
    drug_code, owner_user_id, drug_name, generic_name, brand_name, dosage_form, specification,
    indication, usage_instruction, adverse_reaction, contraindication, manufacturer, drug_type, stock_unit_id,
    stock_unit, image_attachment_id, status, remark, creator_id, create_time, updater_id, update_time, deleted
)
VALUES (
    'DRG1', 0, '阿司匹林肠溶片', '阿司匹林', '拜阿司匹灵', '肠溶片', '100mg*30片',
    '抗血小板聚集，预防心脑血管事件', '成人通常一次1片，一日1次，遵医嘱服用', '胃部不适、出血风险增加',
    '活动性消化道出血、阿司匹林过敏者禁用', '拜耳医药保健有限公司', 'RX', 1,
    '片', NULL, 1, '系统下发常用药示例数据', NULL, NOW(), NULL, NOW(), 0
)
ON CONFLICT ON CONSTRAINT uk_drug_owner_name DO NOTHING;

INSERT INTO drug (
    drug_code, owner_user_id, drug_name, generic_name, brand_name, dosage_form, specification,
    indication, usage_instruction, adverse_reaction, contraindication, manufacturer, drug_type, stock_unit_id,
    stock_unit, image_attachment_id, status, remark, creator_id, create_time, updater_id, update_time, deleted
)
VALUES (
    'DRG2', 0, '二甲双胍片', '盐酸二甲双胍', '格华止', '片剂', '0.5g*20片',
    '用于2型糖尿病血糖控制', '随餐或餐后服用，起始剂量遵医嘱', '恶心、腹泻、腹胀',
    '严重肾功能不全、代谢性酸中毒患者禁用', '中美上海施贵宝制药有限公司', 'RX', 1,
    '片', NULL, 1, '系统下发常用药示例数据', NULL, NOW(), NULL, NOW(), 0
)
ON CONFLICT ON CONSTRAINT uk_drug_owner_name DO NOTHING;

CREATE TABLE IF NOT EXISTS medication_plan (
    plan_id BIGSERIAL PRIMARY KEY,
    plan_code VARCHAR(32),
    owner_user_id BIGINT NOT NULL,
    member_id BIGINT NOT NULL,
    drug_id BIGINT,
    custom_drug_name VARCHAR(100),
    start_date DATE NOT NULL,
    end_date DATE,
    reminder_times_json VARCHAR(500) NOT NULL,
    meal_timing VARCHAR(30) DEFAULT 'NO_LIMIT' NOT NULL,
    dose_amount NUMERIC(10, 2),
    dose_unit VARCHAR(20),
    frequency_type VARCHAR(30) DEFAULT 'DAILY' NOT NULL,
    weekly_days_json VARCHAR(100),
    interval_hours INT,
    interval_days INT,
    dose_rule VARCHAR(1000),
    remark VARCHAR(500),
    status SMALLINT DEFAULT 1 NOT NULL,
    creator_id BIGINT,
    create_time TIMESTAMPTZ,
    updater_id BIGINT,
    update_time TIMESTAMPTZ,
    deleted SMALLINT DEFAULT 0 NOT NULL
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_medication_plan_code ON medication_plan (plan_code);
CREATE INDEX IF NOT EXISTS idx_medication_plan_owner_user_id ON medication_plan (owner_user_id);
CREATE INDEX IF NOT EXISTS idx_medication_plan_member_id ON medication_plan (member_id);
CREATE INDEX IF NOT EXISTS idx_medication_plan_frequency_type ON medication_plan (frequency_type);
CREATE INDEX IF NOT EXISTS idx_medication_plan_status ON medication_plan (status);

CREATE TABLE IF NOT EXISTS medication_reminder (
    reminder_id BIGSERIAL PRIMARY KEY,
    owner_user_id BIGINT NOT NULL,
    plan_id BIGINT NOT NULL,
    member_id BIGINT NOT NULL,
    reminder_date DATE NOT NULL,
    scheduled_time TIMESTAMPTZ NOT NULL,
    drug_name_snapshot VARCHAR(100) NOT NULL,
    dose_amount NUMERIC(10, 2),
    dose_unit VARCHAR(20),
    meal_timing VARCHAR(30),
    reminder_status SMALLINT DEFAULT 0 NOT NULL,
    notify_status SMALLINT DEFAULT 0 NOT NULL,
    notify_time TIMESTAMPTZ,
    notify_fail_reason VARCHAR(255),
    notify_retry_count INT DEFAULT 0 NOT NULL,
    feedback_time TIMESTAMPTZ,
    skip_reason VARCHAR(255),
    creator_id BIGINT,
    create_time TIMESTAMPTZ,
    updater_id BIGINT,
    update_time TIMESTAMPTZ,
    deleted SMALLINT DEFAULT 0 NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_medication_reminder_owner_user_id ON medication_reminder (owner_user_id);
CREATE INDEX IF NOT EXISTS idx_medication_reminder_plan_id ON medication_reminder (plan_id);
CREATE INDEX IF NOT EXISTS idx_medication_reminder_plan_time ON medication_reminder (plan_id, scheduled_time);
CREATE INDEX IF NOT EXISTS idx_medication_reminder_member_id ON medication_reminder (member_id);
CREATE INDEX IF NOT EXISTS idx_medication_reminder_scheduled_time ON medication_reminder (scheduled_time);
CREATE INDEX IF NOT EXISTS idx_medication_reminder_status ON medication_reminder (reminder_status);
CREATE INDEX IF NOT EXISTS idx_medication_reminder_notify_status ON medication_reminder (notify_status);

CREATE TABLE IF NOT EXISTS drug_stock_log (
    log_id BIGSERIAL PRIMARY KEY,
    owner_user_id BIGINT NOT NULL,
    drug_id BIGINT NOT NULL,
    change_type VARCHAR(30) NOT NULL,
    before_quantity NUMERIC(10, 2),
    change_quantity NUMERIC(10, 2),
    after_quantity NUMERIC(10, 2),
    stock_unit_snapshot VARCHAR(20),
    alert_threshold_snapshot NUMERIC(10, 2),
    related_plan_id BIGINT,
    related_reminder_id BIGINT,
    related_temp_medication_id BIGINT,
    operation_remark VARCHAR(255),
    creator_id BIGINT,
    create_time TIMESTAMPTZ,
    updater_id BIGINT,
    update_time TIMESTAMPTZ,
    deleted SMALLINT DEFAULT 0 NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_drug_stock_log_owner_user_id ON drug_stock_log (owner_user_id);
CREATE INDEX IF NOT EXISTS idx_drug_stock_log_drug_id ON drug_stock_log (drug_id);
CREATE INDEX IF NOT EXISTS idx_drug_stock_log_change_type ON drug_stock_log (change_type);
CREATE INDEX IF NOT EXISTS idx_drug_stock_log_reminder_id ON drug_stock_log (related_reminder_id);
CREATE INDEX IF NOT EXISTS idx_drug_stock_log_temp_medication_id ON drug_stock_log (related_temp_medication_id);

CREATE TABLE IF NOT EXISTS drug_temporary_medication_record (
    record_id BIGSERIAL PRIMARY KEY,
    owner_user_id BIGINT NOT NULL,
    member_id BIGINT,
    drug_id BIGINT NOT NULL,
    used_quantity NUMERIC(10, 2) NOT NULL,
    stock_unit_snapshot VARCHAR(20),
    use_time TIMESTAMPTZ NOT NULL,
    symptom VARCHAR(100),
    remark VARCHAR(255),
    deducted_quantity NUMERIC(10, 2) NOT NULL,
    creator_id BIGINT,
    create_time TIMESTAMPTZ,
    updater_id BIGINT,
    update_time TIMESTAMPTZ,
    deleted SMALLINT DEFAULT 0 NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_drug_temp_medication_owner_user_id ON drug_temporary_medication_record (owner_user_id);
CREATE INDEX IF NOT EXISTS idx_drug_temp_medication_member_id ON drug_temporary_medication_record (member_id);
CREATE INDEX IF NOT EXISTS idx_drug_temp_medication_drug_id ON drug_temporary_medication_record (drug_id);
CREATE INDEX IF NOT EXISTS idx_drug_temp_medication_use_time ON drug_temporary_medication_record (use_time);

CREATE TABLE IF NOT EXISTS report (
    report_id BIGSERIAL PRIMARY KEY,
    owner_user_id BIGINT NOT NULL,
    member_id BIGINT NOT NULL,
    report_name VARCHAR(100) NOT NULL,
    report_type VARCHAR(50),
    recognized_report_type VARCHAR(50),
    hospital_name VARCHAR(100),
    report_date DATE,
    file_url VARCHAR(255) NOT NULL,
    stored_file_name VARCHAR(255) NOT NULL,
    original_file_name VARCHAR(255) NOT NULL,
    file_size BIGINT,
    file_extension VARCHAR(20),
    parse_status SMALLINT DEFAULT 0 NOT NULL,
    ai_parse_cached SMALLINT DEFAULT 0 NOT NULL,
    ocr_status SMALLINT DEFAULT 0 NOT NULL,
    ocr_text_snapshot VARCHAR(2000),
    ocr_time TIMESTAMPTZ,
    ai_summary_status SMALLINT DEFAULT 0 NOT NULL,
    ai_summary_content VARCHAR(2000),
    ai_summary_time TIMESTAMPTZ,
    analysis_summary VARCHAR(1000),
    result_interpretation VARCHAR(2000),
    remark VARCHAR(500),
    creator_id BIGINT,
    create_time TIMESTAMPTZ,
    updater_id BIGINT,
    update_time TIMESTAMPTZ,
    deleted SMALLINT DEFAULT 0 NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_report_owner_user_id ON report (owner_user_id);
CREATE INDEX IF NOT EXISTS idx_report_member_id ON report (member_id);
CREATE INDEX IF NOT EXISTS idx_report_report_date ON report (report_date);
CREATE INDEX IF NOT EXISTS idx_report_parse_status ON report (parse_status);

CREATE TABLE IF NOT EXISTS report_item (
    item_id BIGSERIAL PRIMARY KEY,
    report_id BIGINT NOT NULL,
    owner_user_id BIGINT NOT NULL,
    item_code VARCHAR(64),
    standard_item_code VARCHAR(64),
    item_name VARCHAR(100) NOT NULL,
    result_value VARCHAR(100) NOT NULL,
    result_unit VARCHAR(50),
    reference_min NUMERIC(10, 4),
    reference_max NUMERIC(10, 4),
    reference_text VARCHAR(100),
    item_interpretation VARCHAR(300),
    abnormal_flag SMALLINT DEFAULT 0 NOT NULL,
    sort INT DEFAULT 0 NOT NULL,
    remark VARCHAR(255),
    creator_id BIGINT,
    create_time TIMESTAMPTZ,
    updater_id BIGINT,
    update_time TIMESTAMPTZ,
    deleted SMALLINT DEFAULT 0 NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_report_item_report_id ON report_item (report_id);
CREATE INDEX IF NOT EXISTS idx_report_item_owner_user_id ON report_item (owner_user_id);
CREATE INDEX IF NOT EXISTS idx_report_item_standard_item_code ON report_item (standard_item_code);
CREATE INDEX IF NOT EXISTS idx_report_item_abnormal_flag ON report_item (abnormal_flag);

CREATE TABLE IF NOT EXISTS health_problem (
    problem_id BIGSERIAL PRIMARY KEY,
    owner_user_id BIGINT NOT NULL,
    member_id BIGINT NOT NULL,
    problem_name VARCHAR(100) NOT NULL,
    problem_type VARCHAR(50) NOT NULL,
    problem_status SMALLINT DEFAULT 1 NOT NULL,
    risk_level SMALLINT DEFAULT 1 NOT NULL,
    standard_item_code VARCHAR(64),
    first_found_date DATE,
    last_follow_date DATE,
    summary VARCHAR(1000),
    remark VARCHAR(500),
    creator_id BIGINT,
    create_time TIMESTAMPTZ,
    updater_id BIGINT,
    update_time TIMESTAMPTZ,
    deleted SMALLINT DEFAULT 0 NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_health_problem_member_id ON health_problem (member_id);
CREATE INDEX IF NOT EXISTS idx_health_problem_standard_code ON health_problem (standard_item_code);

CREATE TABLE IF NOT EXISTS health_problem_evidence (
    evidence_id BIGSERIAL PRIMARY KEY,
    problem_id BIGINT NOT NULL,
    owner_user_id BIGINT NOT NULL,
    member_id BIGINT NOT NULL,
    evidence_type VARCHAR(50) NOT NULL,
    report_id BIGINT,
    report_item_id BIGINT,
    evidence_title VARCHAR(100),
    evidence_summary VARCHAR(1000),
    evidence_date DATE,
    confidence_level SMALLINT DEFAULT 0 NOT NULL,
    confirm_status SMALLINT DEFAULT 0 NOT NULL,
    creator_id BIGINT,
    create_time TIMESTAMPTZ,
    updater_id BIGINT,
    update_time TIMESTAMPTZ,
    deleted SMALLINT DEFAULT 0 NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_health_problem_evidence_problem_id ON health_problem_evidence (problem_id);
CREATE INDEX IF NOT EXISTS idx_health_problem_evidence_report_id ON health_problem_evidence (report_id);
CREATE INDEX IF NOT EXISTS idx_health_problem_evidence_report_item_id ON health_problem_evidence (report_item_id);

CREATE TABLE IF NOT EXISTS app_device (
    device_id BIGSERIAL PRIMARY KEY,
    owner_user_id BIGINT NOT NULL,
    device_code VARCHAR(64) NOT NULL,
    push_platform VARCHAR(20) NOT NULL,
    device_token VARCHAR(255) NOT NULL,
    device_model VARCHAR(100),
    manufacturer VARCHAR(100),
    os_version VARCHAR(50),
    app_version VARCHAR(50),
    last_active_time TIMESTAMPTZ,
    status SMALLINT DEFAULT 1 NOT NULL,
    creator_id BIGINT,
    create_time TIMESTAMPTZ,
    updater_id BIGINT,
    update_time TIMESTAMPTZ,
    deleted SMALLINT DEFAULT 0 NOT NULL,
    CONSTRAINT uk_app_device_code UNIQUE (device_code)
);

CREATE INDEX IF NOT EXISTS idx_app_device_owner_user_id ON app_device (owner_user_id);
CREATE INDEX IF NOT EXISTS idx_app_device_status ON app_device (status);
CREATE INDEX IF NOT EXISTS idx_app_device_token ON app_device (device_token);

CREATE TABLE IF NOT EXISTS app_message (
    message_id BIGSERIAL PRIMARY KEY,
    owner_user_id BIGINT NOT NULL,
    member_id BIGINT,
    member_name_snapshot VARCHAR(50),
    business_scene VARCHAR(40) NOT NULL,
    business_id BIGINT NOT NULL,
    message_title VARCHAR(100) NOT NULL,
    message_content VARCHAR(500),
    payload_json TEXT,
    dedup_key VARCHAR(100),
    read_status SMALLINT DEFAULT 0 NOT NULL,
    read_time TIMESTAMPTZ,
    send_status SMALLINT DEFAULT 0 NOT NULL,
    send_time TIMESTAMPTZ,
    send_channel VARCHAR(30),
    send_retry_count INT DEFAULT 0 NOT NULL,
    send_result_message VARCHAR(255),
    creator_id BIGINT,
    create_time TIMESTAMPTZ,
    updater_id BIGINT,
    update_time TIMESTAMPTZ,
    deleted SMALLINT DEFAULT 0 NOT NULL,
    CONSTRAINT uk_app_message_owner_dedup UNIQUE (owner_user_id, dedup_key)
);

CREATE INDEX IF NOT EXISTS idx_app_message_owner_user_id ON app_message (owner_user_id);
CREATE INDEX IF NOT EXISTS idx_app_message_owner_read_status ON app_message (owner_user_id, read_status);
CREATE INDEX IF NOT EXISTS idx_app_message_business_scene ON app_message (business_scene);
CREATE INDEX IF NOT EXISTS idx_app_message_business_id ON app_message (business_id);

-- App Push 设备级派发审计表。
--
-- 这张表原先只存在于 2026-04-28 的增量脚本里，fresh init 直接执行主 schema 时不会自动带上，
-- 导致新环境或 H2 镜像环境在写设备级发送审计时出现缺表异常。
-- 这里把最终结构并回主初始化脚本，确保“主 schema + 后续增量”两条建库路径结果一致。
CREATE TABLE IF NOT EXISTS app_push_delivery_log (
    delivery_id BIGSERIAL PRIMARY KEY,
    message_id BIGINT,
    owner_user_id BIGINT NOT NULL,
    member_id BIGINT,
    business_scene VARCHAR(40) NOT NULL,
    business_id BIGINT,
    device_id BIGINT NOT NULL,
    device_code VARCHAR(64),
    push_platform VARCHAR(20),
    device_status_snapshot SMALLINT,
    device_token_masked VARCHAR(40),
    send_channel VARCHAR(30),
    send_status SMALLINT NOT NULL,
    failure_reason_category VARCHAR(30),
    failure_reason_message VARCHAR(255),
    vendor_code VARCHAR(50),
    vendor_message VARCHAR(255),
    retry_no INT DEFAULT 1 NOT NULL,
    send_time TIMESTAMPTZ,
    creator_id BIGINT,
    create_time TIMESTAMPTZ,
    updater_id BIGINT,
    update_time TIMESTAMPTZ,
    deleted SMALLINT DEFAULT 0 NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_app_push_delivery_log_message_id ON app_push_delivery_log (message_id);
CREATE INDEX IF NOT EXISTS idx_app_push_delivery_log_owner_user_id ON app_push_delivery_log (owner_user_id);
CREATE INDEX IF NOT EXISTS idx_app_push_delivery_log_device_id ON app_push_delivery_log (device_id);
CREATE INDEX IF NOT EXISTS idx_app_push_delivery_log_business_scene ON app_push_delivery_log (business_scene);
CREATE INDEX IF NOT EXISTS idx_app_push_delivery_log_send_time ON app_push_delivery_log (send_time);
CREATE INDEX IF NOT EXISTS idx_app_push_delivery_log_send_status ON app_push_delivery_log (send_status);
CREATE INDEX IF NOT EXISTS idx_app_push_delivery_log_failure_reason_category
    ON app_push_delivery_log (failure_reason_category);
CREATE INDEX IF NOT EXISTS idx_app_push_delivery_log_message_retry ON app_push_delivery_log (message_id, retry_no);

CREATE TABLE IF NOT EXISTS follow_up_task (
    task_id BIGSERIAL PRIMARY KEY,
    owner_user_id BIGINT NOT NULL,
    member_id BIGINT,
    task_type VARCHAR(30) NOT NULL,
    source_id BIGINT NOT NULL,
    task_status SMALLINT DEFAULT 0 NOT NULL,
    read_status SMALLINT DEFAULT 0 NOT NULL,
    read_time TIMESTAMPTZ,
    delayed_until TIMESTAMPTZ,
    complete_time TIMESTAMPTZ,
    action_remark VARCHAR(255),
    creator_id BIGINT,
    create_time TIMESTAMPTZ,
    updater_id BIGINT,
    update_time TIMESTAMPTZ,
    deleted SMALLINT DEFAULT 0 NOT NULL
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_follow_up_task_owner_source
    ON follow_up_task (owner_user_id, task_type, source_id);
CREATE INDEX IF NOT EXISTS idx_follow_up_task_owner_user_id ON follow_up_task (owner_user_id);
CREATE INDEX IF NOT EXISTS idx_follow_up_task_member_id ON follow_up_task (member_id);
CREATE INDEX IF NOT EXISTS idx_follow_up_task_status ON follow_up_task (task_status);
CREATE INDEX IF NOT EXISTS idx_follow_up_task_owner_read_status ON follow_up_task (owner_user_id, read_status);
CREATE INDEX IF NOT EXISTS idx_follow_up_task_delayed_until ON follow_up_task (delayed_until);

CREATE TABLE IF NOT EXISTS follow_up_task_log (
    log_id BIGSERIAL PRIMARY KEY,
    task_id BIGINT NOT NULL,
    owner_user_id BIGINT NOT NULL,
    member_id BIGINT,
    task_type VARCHAR(30) NOT NULL,
    source_id BIGINT NOT NULL,
    action_type VARCHAR(30) NOT NULL,
    before_status SMALLINT,
    after_status SMALLINT,
    action_remark VARCHAR(255),
    delayed_until_snapshot TIMESTAMPTZ,
    creator_id BIGINT,
    create_time TIMESTAMPTZ,
    updater_id BIGINT,
    update_time TIMESTAMPTZ,
    deleted SMALLINT DEFAULT 0 NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_follow_up_task_log_task_id ON follow_up_task_log (task_id);
CREATE INDEX IF NOT EXISTS idx_follow_up_task_log_owner_user_id ON follow_up_task_log (owner_user_id);
CREATE INDEX IF NOT EXISTS idx_follow_up_task_log_source ON follow_up_task_log (task_type, source_id);
CREATE INDEX IF NOT EXISTS idx_follow_up_task_log_create_time ON follow_up_task_log (create_time);

CREATE TABLE IF NOT EXISTS operation_task (
    operation_task_id BIGSERIAL PRIMARY KEY,
    owner_user_id BIGINT NOT NULL,
    member_id BIGINT,
    task_title VARCHAR(100) NOT NULL,
    task_content VARCHAR(500),
    action_text VARCHAR(50) DEFAULT '去查看' NOT NULL,
    risk_level VARCHAR(20) DEFAULT 'MEDIUM' NOT NULL,
    priority_weight INT DEFAULT 0 NOT NULL,
    target_page_code VARCHAR(50),
    target_page_name VARCHAR(50),
    target_biz_id BIGINT,
    target_biz_type VARCHAR(30),
    target_tab_code VARCHAR(30),
    target_anchor_code VARCHAR(50),
    target_anchor_name VARCHAR(50),
    start_time TIMESTAMPTZ,
    end_time TIMESTAMPTZ,
    status SMALLINT DEFAULT 1 NOT NULL,
    remark VARCHAR(500),
    creator_id BIGINT,
    create_time TIMESTAMPTZ,
    updater_id BIGINT,
    update_time TIMESTAMPTZ,
    deleted SMALLINT DEFAULT 0 NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_operation_task_owner_user_id ON operation_task (owner_user_id);
CREATE INDEX IF NOT EXISTS idx_operation_task_member_id ON operation_task (member_id);
CREATE INDEX IF NOT EXISTS idx_operation_task_status ON operation_task (status);
CREATE INDEX IF NOT EXISTS idx_operation_task_risk_level ON operation_task (risk_level);
CREATE INDEX IF NOT EXISTS idx_operation_task_start_time ON operation_task (start_time);
CREATE INDEX IF NOT EXISTS idx_operation_task_end_time ON operation_task (end_time);
CREATE INDEX IF NOT EXISTS idx_operation_task_owner_status_time
    ON operation_task (owner_user_id, status, start_time, end_time);

CREATE TABLE IF NOT EXISTS family_member_share (
    share_id BIGSERIAL PRIMARY KEY,
    member_id BIGINT NOT NULL,
    owner_user_id BIGINT NOT NULL,
    collaborator_user_id BIGINT NOT NULL,
    invite_id BIGINT,
    share_role VARCHAR(20) NOT NULL,
    share_status SMALLINT DEFAULT 1 NOT NULL,
    accepted_time TIMESTAMPTZ,
    remark VARCHAR(255),
    creator_id BIGINT,
    create_time TIMESTAMPTZ,
    updater_id BIGINT,
    update_time TIMESTAMPTZ,
    deleted SMALLINT DEFAULT 0 NOT NULL
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_family_member_share_member_collaborator
    ON family_member_share (member_id, collaborator_user_id, deleted);
CREATE INDEX IF NOT EXISTS idx_family_member_share_owner_user_id ON family_member_share (owner_user_id);
CREATE INDEX IF NOT EXISTS idx_family_member_share_collaborator_user_id ON family_member_share (collaborator_user_id);
CREATE INDEX IF NOT EXISTS idx_family_member_share_status ON family_member_share (share_status);

CREATE TABLE IF NOT EXISTS family_share_invite (
    invite_id BIGSERIAL PRIMARY KEY,
    member_id BIGINT NOT NULL,
    owner_user_id BIGINT NOT NULL,
    invite_code VARCHAR(32) NOT NULL,
    invitee_user_id BIGINT,
    share_role VARCHAR(20) NOT NULL,
    invite_status SMALLINT DEFAULT 0 NOT NULL,
    expire_time TIMESTAMPTZ NOT NULL,
    accepted_time TIMESTAMPTZ,
    remark VARCHAR(255),
    creator_id BIGINT,
    create_time TIMESTAMPTZ,
    updater_id BIGINT,
    update_time TIMESTAMPTZ,
    deleted SMALLINT DEFAULT 0 NOT NULL,
    CONSTRAINT uk_family_share_invite_invite_code UNIQUE (invite_code, deleted)
);

CREATE INDEX IF NOT EXISTS idx_family_share_invite_member_id ON family_share_invite (member_id);
CREATE INDEX IF NOT EXISTS idx_family_share_invite_owner_user_id ON family_share_invite (owner_user_id);
CREATE INDEX IF NOT EXISTS idx_family_share_invite_status ON family_share_invite (invite_status);
CREATE INDEX IF NOT EXISTS idx_family_share_invite_expire_time ON family_share_invite (expire_time);

CREATE TABLE IF NOT EXISTS indicator_template (
    template_id BIGSERIAL PRIMARY KEY,
    report_type VARCHAR(50),
    item_code VARCHAR(64),
    item_name VARCHAR(100) NOT NULL,
    result_unit VARCHAR(50),
    reference_min NUMERIC(10, 4),
    reference_max NUMERIC(10, 4),
    reference_text VARCHAR(100),
    suggestion_template VARCHAR(1000),
    sort INT DEFAULT 0 NOT NULL,
    status SMALLINT DEFAULT 1 NOT NULL,
    creator_id BIGINT,
    create_time TIMESTAMPTZ,
    updater_id BIGINT,
    update_time TIMESTAMPTZ,
    deleted SMALLINT DEFAULT 0 NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_indicator_template_report_type ON indicator_template (report_type);
CREATE INDEX IF NOT EXISTS idx_indicator_template_item_code ON indicator_template (item_code);
CREATE INDEX IF NOT EXISTS idx_indicator_template_item_name ON indicator_template (item_name);
CREATE INDEX IF NOT EXISTS idx_indicator_template_status ON indicator_template (status);

-- ==============================
-- 四、健康业务表注释
-- ==============================
-- 这里统一使用 PostgreSQL 的 COMMENT ON 语法补充表/字段说明。
-- 这样即使建表语句本身保持简洁，数据库元数据里仍然能直接看到中文语义，
-- 方便后续开发、联调、排查问题以及数据平台侧做表结构识别。

COMMENT ON TABLE app_user IS '健康系统App用户表';
COMMENT ON COLUMN app_user.user_id IS 'App用户ID';
COMMENT ON COLUMN app_user.mobile IS '手机号';
COMMENT ON COLUMN app_user.nickname IS '昵称';
COMMENT ON COLUMN app_user.avatar IS '头像地址';
COMMENT ON COLUMN app_user.password IS '密码';
COMMENT ON COLUMN app_user.status IS '账号状态（1正常 0停用）';
COMMENT ON COLUMN app_user.last_login_ip IS '最后登录IP';
COMMENT ON COLUMN app_user.last_login_time IS '最后登录时间';
COMMENT ON COLUMN app_user.register_source IS '注册来源';
COMMENT ON COLUMN app_user.creator_id IS '创建者ID';
COMMENT ON COLUMN app_user.create_time IS '创建时间';
COMMENT ON COLUMN app_user.updater_id IS '更新者ID';
COMMENT ON COLUMN app_user.update_time IS '更新时间';
COMMENT ON COLUMN app_user.deleted IS '逻辑删除';

COMMENT ON TABLE family_member IS '健康系统家庭成员表';
COMMENT ON COLUMN family_member.member_id IS '家庭成员ID';
COMMENT ON COLUMN family_member.member_code IS '家庭成员业务编码，优先给前端展示使用';
COMMENT ON COLUMN family_member.owner_user_id IS '归属App用户ID';
COMMENT ON COLUMN family_member.member_name IS '成员姓名';
COMMENT ON COLUMN family_member.gender IS '性别（0未知 1男 2女）';
COMMENT ON COLUMN family_member.birthday IS '生日';
COMMENT ON COLUMN family_member.relation_type IS '关系类型';
COMMENT ON COLUMN family_member.height IS '身高(cm)';
COMMENT ON COLUMN family_member.weight IS '体重(kg)';
COMMENT ON COLUMN family_member.blood_type IS '血型';
COMMENT ON COLUMN family_member.allergy_history IS '过敏史';
COMMENT ON COLUMN family_member.chronic_history IS '慢病史';
COMMENT ON COLUMN family_member.remark IS '备注';
COMMENT ON COLUMN family_member.status IS '状态（1正常 0停用）';
COMMENT ON COLUMN family_member.creator_id IS '创建者ID';
COMMENT ON COLUMN family_member.create_time IS '创建时间';
COMMENT ON COLUMN family_member.updater_id IS '更新者ID';
COMMENT ON COLUMN family_member.update_time IS '更新时间';
COMMENT ON COLUMN family_member.deleted IS '逻辑删除';

COMMENT ON TABLE attachment IS '健康系统统一附件表';
COMMENT ON COLUMN attachment.attachment_id IS '附件ID';
COMMENT ON COLUMN attachment.owner_user_id IS '归属App用户ID，后台上传时允许为空';
COMMENT ON COLUMN attachment.attachment_type IS '附件类型，例如 DRUG_IMAGE、DRUG_UNIT_ICON';
COMMENT ON COLUMN attachment.storage_provider IS '存储提供方，当前固定为 LOCAL';
COMMENT ON COLUMN attachment.file_url IS '文件访问地址，当前保存相对路径';
COMMENT ON COLUMN attachment.stored_file_name IS '存储文件名';
COMMENT ON COLUMN attachment.original_file_name IS '原始文件名';
COMMENT ON COLUMN attachment.file_size IS '文件大小（字节）';
COMMENT ON COLUMN attachment.file_extension IS '文件后缀';
COMMENT ON COLUMN attachment.content_type IS '内容类型';
COMMENT ON COLUMN attachment.status IS '状态（1正常 0停用）';
COMMENT ON COLUMN attachment.remark IS '备注';
COMMENT ON COLUMN attachment.creator_id IS '创建者ID';
COMMENT ON COLUMN attachment.create_time IS '创建时间';
COMMENT ON COLUMN attachment.updater_id IS '更新者ID';
COMMENT ON COLUMN attachment.update_time IS '更新时间';
COMMENT ON COLUMN attachment.deleted IS '逻辑删除';

COMMENT ON TABLE drug_unit IS '健康系统药品单位表';
COMMENT ON COLUMN drug_unit.unit_id IS '单位ID';
COMMENT ON COLUMN drug_unit.unit_code IS '单位编码';
COMMENT ON COLUMN drug_unit.unit_name IS '单位名称';
COMMENT ON COLUMN drug_unit.unit_alias IS '单位别名';
COMMENT ON COLUMN drug_unit.precision_scale IS '小数位精度';
COMMENT ON COLUMN drug_unit.sort IS '排序号';
COMMENT ON COLUMN drug_unit.status IS '状态（1正常 0停用）';
COMMENT ON COLUMN drug_unit.icon_attachment_id IS '图标附件ID';
COMMENT ON COLUMN drug_unit.remark IS '备注';
COMMENT ON COLUMN drug_unit.creator_id IS '创建者ID';
COMMENT ON COLUMN drug_unit.create_time IS '创建时间';
COMMENT ON COLUMN drug_unit.updater_id IS '更新者ID';
COMMENT ON COLUMN drug_unit.update_time IS '更新时间';
COMMENT ON COLUMN drug_unit.deleted IS '逻辑删除';

COMMENT ON TABLE drug IS '健康系统药品表（系统药品 + 个人药柜条目）';
COMMENT ON COLUMN drug.drug_id IS '药品ID';
COMMENT ON COLUMN drug.drug_code IS '药品业务编码，优先给前端展示使用';
COMMENT ON COLUMN drug.owner_user_id IS '归属App用户ID，0表示系统下发药品';
COMMENT ON COLUMN drug.drug_name IS '药品名称';
COMMENT ON COLUMN drug.generic_name IS '通用名';
COMMENT ON COLUMN drug.brand_name IS '商品名';
COMMENT ON COLUMN drug.dosage_form IS '剂型';
COMMENT ON COLUMN drug.specification IS '规格';
COMMENT ON COLUMN drug.indication IS '适应症';
COMMENT ON COLUMN drug.usage_instruction IS '用法用量';
COMMENT ON COLUMN drug.adverse_reaction IS '不良反应';
COMMENT ON COLUMN drug.contraindication IS '禁忌';
COMMENT ON COLUMN drug.manufacturer IS '生产厂家';
COMMENT ON COLUMN drug.drug_type IS '药品类型';
COMMENT ON COLUMN drug.source_drug_id IS '来源系统药品ID，仅个人引入系统药品时使用';
COMMENT ON COLUMN drug.stock_unit_id IS '库存单位ID';
COMMENT ON COLUMN drug.stock_unit IS '库存单位，例如片、粒、ml';
COMMENT ON COLUMN drug.image_attachment_id IS '药品图片附件ID';
COMMENT ON COLUMN drug.stock_alert_threshold IS '库存预警阈值，仅个人药品启用库存跟踪';
COMMENT ON COLUMN drug.low_stock_notified IS '当前预警周期内是否已发送过低库存提醒';
COMMENT ON COLUMN drug.low_stock_notify_time IS '最近一次低库存提醒发送时间';
COMMENT ON COLUMN drug.status IS '状态（1正常 0停用）';
COMMENT ON COLUMN drug.remark IS '备注';
COMMENT ON COLUMN drug.creator_id IS '创建者ID';
COMMENT ON COLUMN drug.create_time IS '创建时间';
COMMENT ON COLUMN drug.updater_id IS '更新者ID';
COMMENT ON COLUMN drug.update_time IS '更新时间';
COMMENT ON COLUMN drug.deleted IS '逻辑删除';

COMMENT ON TABLE drug_stock_batch IS '健康系统药品批号效期库存表';
COMMENT ON COLUMN drug_stock_batch.batch_id IS '批次库存ID';
COMMENT ON COLUMN drug_stock_batch.owner_user_id IS '归属App用户ID';
COMMENT ON COLUMN drug_stock_batch.drug_id IS '药品ID';
COMMENT ON COLUMN drug_stock_batch.batch_no IS '药品批号，可为空';
COMMENT ON COLUMN drug_stock_batch.expire_date IS '药品效期';
COMMENT ON COLUMN drug_stock_batch.is_default_batch IS '是否默认批次（批号和效期都缺失时的兜底库存桶）';
COMMENT ON COLUMN drug_stock_batch.stock_quantity IS '当前批次库存数量';
COMMENT ON COLUMN drug_stock_batch.near_expire_notified IS '当前近效期提醒周期内是否已经提醒过';
COMMENT ON COLUMN drug_stock_batch.near_expire_notify_time IS '最近一次近效期提醒发送时间';
COMMENT ON COLUMN drug_stock_batch.creator_id IS '创建者ID';
COMMENT ON COLUMN drug_stock_batch.create_time IS '创建时间';
COMMENT ON COLUMN drug_stock_batch.updater_id IS '更新者ID';
COMMENT ON COLUMN drug_stock_batch.update_time IS '更新时间';
COMMENT ON COLUMN drug_stock_batch.deleted IS '逻辑删除';

COMMENT ON TABLE medication_plan IS '健康系统用药计划表';
COMMENT ON COLUMN medication_plan.plan_id IS '用药计划ID';
COMMENT ON COLUMN medication_plan.plan_code IS '用药计划业务编码，优先给前端展示使用';
COMMENT ON COLUMN medication_plan.owner_user_id IS '归属App用户ID';
COMMENT ON COLUMN medication_plan.member_id IS '家庭成员ID';
COMMENT ON COLUMN medication_plan.drug_id IS '药品ID';
COMMENT ON COLUMN medication_plan.custom_drug_name IS '自定义药名';
COMMENT ON COLUMN medication_plan.start_date IS '开始日期';
COMMENT ON COLUMN medication_plan.end_date IS '结束日期，为空表示长期计划';
COMMENT ON COLUMN medication_plan.reminder_times_json IS '提醒时间点JSON';
COMMENT ON COLUMN medication_plan.meal_timing IS '服药时机';
COMMENT ON COLUMN medication_plan.dose_amount IS '每次剂量';
COMMENT ON COLUMN medication_plan.dose_unit IS '剂量单位';
COMMENT ON COLUMN medication_plan.frequency_type IS '频率类型（DAILY每日 EVERY_OTHER_DAY隔日 WEEKLY每周 INTERVAL_HOURS每几小时 INTERVAL_DAYS每几天）';
COMMENT ON COLUMN medication_plan.weekly_days_json IS '每周提醒星期JSON，仅 frequency_type = WEEKLY 时使用';
COMMENT ON COLUMN medication_plan.interval_hours IS '每几小时提醒一次，仅 frequency_type = INTERVAL_HOURS 时使用';
COMMENT ON COLUMN medication_plan.interval_days IS '每几天提醒一次，仅 frequency_type = INTERVAL_DAYS 时使用';
COMMENT ON COLUMN medication_plan.dose_rule IS '阶段剂量规则，例如前4天1片、再4天1.5片';
COMMENT ON COLUMN medication_plan.remark IS '备注';
COMMENT ON COLUMN medication_plan.status IS '状态（1正常 0停用）';
COMMENT ON COLUMN medication_plan.creator_id IS '创建者ID';
COMMENT ON COLUMN medication_plan.create_time IS '创建时间';
COMMENT ON COLUMN medication_plan.updater_id IS '更新者ID';
COMMENT ON COLUMN medication_plan.update_time IS '更新时间';
COMMENT ON COLUMN medication_plan.deleted IS '逻辑删除';

COMMENT ON TABLE drug_temporary_medication_record IS '健康系统药品临时用药记录表';
COMMENT ON COLUMN drug_temporary_medication_record.record_id IS '临时用药记录ID';
COMMENT ON COLUMN drug_temporary_medication_record.owner_user_id IS '归属App用户ID';
COMMENT ON COLUMN drug_temporary_medication_record.member_id IS '家庭成员ID，可为空';
COMMENT ON COLUMN drug_temporary_medication_record.drug_id IS '药品ID';
COMMENT ON COLUMN drug_temporary_medication_record.used_quantity IS '本次使用数量';
COMMENT ON COLUMN drug_temporary_medication_record.stock_unit_snapshot IS '库存单位快照';
COMMENT ON COLUMN drug_temporary_medication_record.use_time IS '实际用药时间';
COMMENT ON COLUMN drug_temporary_medication_record.symptom IS '用途或症状';
COMMENT ON COLUMN drug_temporary_medication_record.remark IS '备注';
COMMENT ON COLUMN drug_temporary_medication_record.deducted_quantity IS '实际扣减数量';
COMMENT ON COLUMN drug_temporary_medication_record.creator_id IS '创建者ID';
COMMENT ON COLUMN drug_temporary_medication_record.create_time IS '创建时间';
COMMENT ON COLUMN drug_temporary_medication_record.updater_id IS '更新者ID';
COMMENT ON COLUMN drug_temporary_medication_record.update_time IS '更新时间';
COMMENT ON COLUMN drug_temporary_medication_record.deleted IS '逻辑删除';

COMMENT ON TABLE medication_reminder IS '健康系统用药提醒记录表';
COMMENT ON COLUMN medication_reminder.reminder_id IS '提醒记录ID';
COMMENT ON COLUMN medication_reminder.owner_user_id IS '归属App用户ID';
COMMENT ON COLUMN medication_reminder.plan_id IS '用药计划ID';
COMMENT ON COLUMN medication_reminder.member_id IS '家庭成员ID';
COMMENT ON COLUMN medication_reminder.reminder_date IS '提醒日期';
COMMENT ON COLUMN medication_reminder.scheduled_time IS '计划提醒时间';
COMMENT ON COLUMN medication_reminder.drug_name_snapshot IS '药品名称快照';
COMMENT ON COLUMN medication_reminder.dose_amount IS '每次剂量';
COMMENT ON COLUMN medication_reminder.dose_unit IS '剂量单位';
COMMENT ON COLUMN medication_reminder.meal_timing IS '服药时机';
COMMENT ON COLUMN medication_reminder.reminder_status IS '提醒状态（0待处理 1已服药 2已跳过 3已过期）';
COMMENT ON COLUMN medication_reminder.notify_status IS '发送状态（0待发送 1发送成功 2发送失败）';
COMMENT ON COLUMN medication_reminder.notify_time IS '最近一次发送时间';
COMMENT ON COLUMN medication_reminder.notify_fail_reason IS '发送失败原因';
COMMENT ON COLUMN medication_reminder.notify_retry_count IS '发送重试次数';
COMMENT ON COLUMN medication_reminder.feedback_time IS '反馈时间';
COMMENT ON COLUMN medication_reminder.skip_reason IS '跳过原因';
COMMENT ON COLUMN medication_reminder.creator_id IS '创建者ID';
COMMENT ON COLUMN medication_reminder.create_time IS '创建时间';
COMMENT ON COLUMN medication_reminder.updater_id IS '更新者ID';
COMMENT ON COLUMN medication_reminder.update_time IS '更新时间';
COMMENT ON COLUMN medication_reminder.deleted IS '逻辑删除';

COMMENT ON TABLE drug_stock_log IS '健康系统药品库存流水表';
COMMENT ON COLUMN drug_stock_log.log_id IS '库存流水ID';
COMMENT ON COLUMN drug_stock_log.owner_user_id IS '归属App用户ID';
COMMENT ON COLUMN drug_stock_log.drug_id IS '药品ID';
COMMENT ON COLUMN drug_stock_log.change_type IS '库存变更类型';
COMMENT ON COLUMN drug_stock_log.before_quantity IS '变更前库存';
COMMENT ON COLUMN drug_stock_log.change_quantity IS '本次变更数量，增加为正，扣减为负';
COMMENT ON COLUMN drug_stock_log.after_quantity IS '变更后库存';
COMMENT ON COLUMN drug_stock_log.stock_unit_snapshot IS '库存单位快照';
COMMENT ON COLUMN drug_stock_log.alert_threshold_snapshot IS '预警阈值快照';
COMMENT ON COLUMN drug_stock_log.related_plan_id IS '关联用药计划ID';
COMMENT ON COLUMN drug_stock_log.related_reminder_id IS '关联提醒ID';
COMMENT ON COLUMN drug_stock_log.related_temp_medication_id IS '关联临时用药记录ID';
COMMENT ON COLUMN drug_stock_log.operation_remark IS '操作备注';
COMMENT ON COLUMN drug_stock_log.creator_id IS '创建者ID';
COMMENT ON COLUMN drug_stock_log.create_time IS '创建时间';
COMMENT ON COLUMN drug_stock_log.updater_id IS '更新者ID';
COMMENT ON COLUMN drug_stock_log.update_time IS '更新时间';
COMMENT ON COLUMN drug_stock_log.deleted IS '逻辑删除';

COMMENT ON TABLE report IS '健康系统体检报告表';
COMMENT ON COLUMN report.report_id IS '报告ID';
COMMENT ON COLUMN report.owner_user_id IS '归属App用户ID';
COMMENT ON COLUMN report.member_id IS '家庭成员ID';
COMMENT ON COLUMN report.report_name IS '报告名称';
COMMENT ON COLUMN report.report_type IS '报告类型';
COMMENT ON COLUMN report.recognized_report_type IS '后台识别出的报告细分类型';
COMMENT ON COLUMN report.hospital_name IS '医院名称';
COMMENT ON COLUMN report.report_date IS '报告日期';
COMMENT ON COLUMN report.file_url IS '文件访问地址';
COMMENT ON COLUMN report.stored_file_name IS '存储文件名';
COMMENT ON COLUMN report.original_file_name IS '原始文件名';
COMMENT ON COLUMN report.file_size IS '文件大小（字节）';
COMMENT ON COLUMN report.file_extension IS '文件后缀';
COMMENT ON COLUMN report.parse_status IS '解析状态（0待解析 1解析中 2已解析 3解析失败）';
COMMENT ON COLUMN report.ai_parse_cached IS 'AI解析结果缓存标记（0未缓存 1已缓存，再次解析时可直接复用已有结构化结果）';
COMMENT ON COLUMN report.ocr_status IS 'OCR占位处理状态（0待处理 1处理中 2已完成 3失败）';
COMMENT ON COLUMN report.ocr_text_snapshot IS 'OCR占位文本快照';
COMMENT ON COLUMN report.ocr_time IS 'OCR占位处理时间';
COMMENT ON COLUMN report.ai_summary_status IS 'AI总结占位状态（0待处理 1处理中 2已完成 3失败）';
COMMENT ON COLUMN report.ai_summary_content IS 'AI总结占位内容';
COMMENT ON COLUMN report.ai_summary_time IS 'AI总结占位处理时间';
COMMENT ON COLUMN report.analysis_summary IS '解析摘要';
COMMENT ON COLUMN report.result_interpretation IS '结果解读，结合本次结果说明主要结论和实验有效性';
COMMENT ON COLUMN report.remark IS '备注';
COMMENT ON COLUMN report.creator_id IS '创建者ID';
COMMENT ON COLUMN report.create_time IS '创建时间';
COMMENT ON COLUMN report.updater_id IS '更新者ID';
COMMENT ON COLUMN report.update_time IS '更新时间';
COMMENT ON COLUMN report.deleted IS '逻辑删除';

COMMENT ON TABLE report_item IS '健康系统体检报告指标结果表';
COMMENT ON COLUMN report_item.item_id IS '指标结果ID';
COMMENT ON COLUMN report_item.report_id IS '体检报告ID';
COMMENT ON COLUMN report_item.owner_user_id IS '归属App用户ID';
COMMENT ON COLUMN report_item.item_code IS '指标编码';
COMMENT ON COLUMN report_item.standard_item_code IS '标准指标编码';
COMMENT ON COLUMN report_item.item_name IS '指标名称';
COMMENT ON COLUMN report_item.result_value IS '结果值';
COMMENT ON COLUMN report_item.result_unit IS '结果单位';
COMMENT ON COLUMN report_item.reference_min IS '参考范围下限';
COMMENT ON COLUMN report_item.reference_max IS '参考范围上限';
COMMENT ON COLUMN report_item.reference_text IS '参考范围原文';
COMMENT ON COLUMN report_item.item_interpretation IS '指标解读，说明该指标主要反映什么';
COMMENT ON COLUMN report_item.abnormal_flag IS '异常标记（0待判断 1正常 2偏低 3偏高 4异常）';
COMMENT ON COLUMN report_item.sort IS '排序号';
COMMENT ON COLUMN report_item.remark IS '备注';
COMMENT ON COLUMN report_item.creator_id IS '创建者ID';
COMMENT ON COLUMN report_item.create_time IS '创建时间';
COMMENT ON COLUMN report_item.updater_id IS '更新者ID';
COMMENT ON COLUMN report_item.update_time IS '更新时间';
COMMENT ON COLUMN report_item.deleted IS '逻辑删除';

COMMENT ON TABLE health_problem IS '健康系统健康问题表';
COMMENT ON COLUMN health_problem.problem_id IS '健康问题ID';
COMMENT ON COLUMN health_problem.owner_user_id IS '归属App用户ID';
COMMENT ON COLUMN health_problem.member_id IS '家庭成员ID';
COMMENT ON COLUMN health_problem.problem_name IS '问题名称';
COMMENT ON COLUMN health_problem.problem_type IS '问题类型';
COMMENT ON COLUMN health_problem.problem_status IS '问题状态（1跟进中 2已缓解 3已关闭）';
COMMENT ON COLUMN health_problem.risk_level IS '风险等级（1低 2中 3高）';
COMMENT ON COLUMN health_problem.standard_item_code IS '关联的标准指标编码';
COMMENT ON COLUMN health_problem.first_found_date IS '首次发现日期';
COMMENT ON COLUMN health_problem.last_follow_date IS '最近跟进日期';
COMMENT ON COLUMN health_problem.summary IS '问题摘要';
COMMENT ON COLUMN health_problem.remark IS '备注';
COMMENT ON COLUMN health_problem.creator_id IS '创建者ID';
COMMENT ON COLUMN health_problem.create_time IS '创建时间';
COMMENT ON COLUMN health_problem.updater_id IS '更新者ID';
COMMENT ON COLUMN health_problem.update_time IS '更新时间';
COMMENT ON COLUMN health_problem.deleted IS '逻辑删除';

COMMENT ON TABLE health_problem_evidence IS '健康系统健康问题证据表';
COMMENT ON COLUMN health_problem_evidence.evidence_id IS '证据ID';
COMMENT ON COLUMN health_problem_evidence.problem_id IS '健康问题ID';
COMMENT ON COLUMN health_problem_evidence.owner_user_id IS '归属App用户ID';
COMMENT ON COLUMN health_problem_evidence.member_id IS '家庭成员ID';
COMMENT ON COLUMN health_problem_evidence.evidence_type IS '证据类型';
COMMENT ON COLUMN health_problem_evidence.report_id IS '关联报告ID';
COMMENT ON COLUMN health_problem_evidence.report_item_id IS '关联报告指标ID';
COMMENT ON COLUMN health_problem_evidence.evidence_title IS '证据标题';
COMMENT ON COLUMN health_problem_evidence.evidence_summary IS '证据摘要';
COMMENT ON COLUMN health_problem_evidence.evidence_date IS '证据日期';
COMMENT ON COLUMN health_problem_evidence.confidence_level IS '置信等级（0未知 1低 2中 3高）';
COMMENT ON COLUMN health_problem_evidence.confirm_status IS '确认状态（0待确认 1已确认 2已忽略）';
COMMENT ON COLUMN health_problem_evidence.creator_id IS '创建者ID';
COMMENT ON COLUMN health_problem_evidence.create_time IS '创建时间';
COMMENT ON COLUMN health_problem_evidence.updater_id IS '更新者ID';
COMMENT ON COLUMN health_problem_evidence.update_time IS '更新时间';
COMMENT ON COLUMN health_problem_evidence.deleted IS '逻辑删除';

COMMENT ON TABLE app_device IS '健康系统App设备表';
COMMENT ON COLUMN app_device.device_id IS '设备ID';
COMMENT ON COLUMN app_device.owner_user_id IS '归属App用户ID';
COMMENT ON COLUMN app_device.device_code IS '设备唯一编码';
COMMENT ON COLUMN app_device.push_platform IS '推送平台';
COMMENT ON COLUMN app_device.device_token IS '设备Token';
COMMENT ON COLUMN app_device.device_model IS '设备型号';
COMMENT ON COLUMN app_device.manufacturer IS '设备厂商';
COMMENT ON COLUMN app_device.os_version IS '系统版本';
COMMENT ON COLUMN app_device.app_version IS 'App版本';
COMMENT ON COLUMN app_device.last_active_time IS '最后活跃时间';
COMMENT ON COLUMN app_device.status IS '状态（1正常 0停用）';
COMMENT ON COLUMN app_device.creator_id IS '创建者ID';
COMMENT ON COLUMN app_device.create_time IS '创建时间';
COMMENT ON COLUMN app_device.updater_id IS '更新者ID';
COMMENT ON COLUMN app_device.update_time IS '更新时间';
COMMENT ON COLUMN app_device.deleted IS '逻辑删除';

COMMENT ON TABLE app_message IS '健康系统App消息中心表';
COMMENT ON COLUMN app_message.message_id IS '消息ID';
COMMENT ON COLUMN app_message.owner_user_id IS '归属App用户ID';
COMMENT ON COLUMN app_message.member_id IS '家庭成员ID';
COMMENT ON COLUMN app_message.member_name_snapshot IS '家庭成员名称快照';
COMMENT ON COLUMN app_message.business_scene IS '业务场景编码（MEDICATION_REMINDER用药提醒 REPORT_FOLLOW_UP报告跟进）';
COMMENT ON COLUMN app_message.business_id IS '业务主键ID';
COMMENT ON COLUMN app_message.message_title IS '消息标题';
COMMENT ON COLUMN app_message.message_content IS '消息正文';
COMMENT ON COLUMN app_message.payload_json IS '统一业务透传载荷JSON';
COMMENT ON COLUMN app_message.dedup_key IS '消息去重键';
COMMENT ON COLUMN app_message.read_status IS '已读状态（0未读 1已读）';
COMMENT ON COLUMN app_message.read_time IS '已读时间';
COMMENT ON COLUMN app_message.send_status IS '最近一次发送状态（0待发送 1发送成功 2发送失败）';
COMMENT ON COLUMN app_message.send_time IS '最近一次发送时间';
COMMENT ON COLUMN app_message.send_channel IS '最近一次发送通道';
COMMENT ON COLUMN app_message.send_retry_count IS '发送重试次数';
COMMENT ON COLUMN app_message.send_result_message IS '最近一次发送结果说明';
COMMENT ON COLUMN app_message.creator_id IS '创建者ID';
COMMENT ON COLUMN app_message.create_time IS '创建时间';
COMMENT ON COLUMN app_message.updater_id IS '更新者ID';
COMMENT ON COLUMN app_message.update_time IS '更新时间';
COMMENT ON COLUMN app_message.deleted IS '逻辑删除';

COMMENT ON TABLE app_push_delivery_log IS '健康系统App Push设备级派发审计表';
COMMENT ON COLUMN app_push_delivery_log.delivery_id IS '设备级派发审计ID';
COMMENT ON COLUMN app_push_delivery_log.message_id IS '消息ID';
COMMENT ON COLUMN app_push_delivery_log.owner_user_id IS '归属App用户ID';
COMMENT ON COLUMN app_push_delivery_log.member_id IS '家庭成员ID';
COMMENT ON COLUMN app_push_delivery_log.business_scene IS '业务场景编码';
COMMENT ON COLUMN app_push_delivery_log.business_id IS '业务主键ID';
COMMENT ON COLUMN app_push_delivery_log.device_id IS '设备ID';
COMMENT ON COLUMN app_push_delivery_log.device_code IS '设备唯一编码快照';
COMMENT ON COLUMN app_push_delivery_log.push_platform IS '推送平台快照';
COMMENT ON COLUMN app_push_delivery_log.device_status_snapshot IS '发送当时的设备状态快照';
COMMENT ON COLUMN app_push_delivery_log.device_token_masked IS '脱敏后的设备Token';
COMMENT ON COLUMN app_push_delivery_log.send_channel IS '发送通道';
COMMENT ON COLUMN app_push_delivery_log.send_status IS '设备级发送状态（1成功 2失败）';
COMMENT ON COLUMN app_push_delivery_log.failure_reason_category IS '失败原因分类';
COMMENT ON COLUMN app_push_delivery_log.failure_reason_message IS '失败原因说明';
COMMENT ON COLUMN app_push_delivery_log.vendor_code IS '厂商结果码';
COMMENT ON COLUMN app_push_delivery_log.vendor_message IS '厂商结果说明';
COMMENT ON COLUMN app_push_delivery_log.retry_no IS '本条消息针对该设备的第几次尝试';
COMMENT ON COLUMN app_push_delivery_log.send_time IS '发送时间';
COMMENT ON COLUMN app_push_delivery_log.creator_id IS '创建者ID';
COMMENT ON COLUMN app_push_delivery_log.create_time IS '创建时间';
COMMENT ON COLUMN app_push_delivery_log.updater_id IS '更新者ID';
COMMENT ON COLUMN app_push_delivery_log.update_time IS '更新时间';
COMMENT ON COLUMN app_push_delivery_log.deleted IS '逻辑删除';

COMMENT ON TABLE follow_up_task IS '健康系统首页待跟进任务表';
COMMENT ON COLUMN follow_up_task.task_id IS '任务记录ID';
COMMENT ON COLUMN follow_up_task.owner_user_id IS '归属App用户ID';
COMMENT ON COLUMN follow_up_task.member_id IS '家庭成员ID';
COMMENT ON COLUMN follow_up_task.task_type IS '任务类型（REMINDER提醒任务 REPORT_ADVICE报告建议任务 OPERATION运营任务）';
COMMENT ON COLUMN follow_up_task.source_id IS '来源业务ID';
COMMENT ON COLUMN follow_up_task.task_status IS '任务状态（0待跟进 1已延后 2已完成 3已忽略）';
COMMENT ON COLUMN follow_up_task.read_status IS '已读状态（0未读 1已读）';
COMMENT ON COLUMN follow_up_task.read_time IS '已读时间';
COMMENT ON COLUMN follow_up_task.delayed_until IS '延后到期时间';
COMMENT ON COLUMN follow_up_task.complete_time IS '完成时间';
COMMENT ON COLUMN follow_up_task.action_remark IS '操作备注';
COMMENT ON COLUMN follow_up_task.creator_id IS '创建者ID';
COMMENT ON COLUMN follow_up_task.create_time IS '创建时间';
COMMENT ON COLUMN follow_up_task.updater_id IS '更新者ID';
COMMENT ON COLUMN follow_up_task.update_time IS '更新时间';
COMMENT ON COLUMN follow_up_task.deleted IS '逻辑删除';

COMMENT ON TABLE follow_up_task_log IS '健康系统首页待跟进任务操作日志表';
COMMENT ON COLUMN follow_up_task_log.log_id IS '日志ID';
COMMENT ON COLUMN follow_up_task_log.task_id IS '任务记录ID';
COMMENT ON COLUMN follow_up_task_log.owner_user_id IS '归属App用户ID';
COMMENT ON COLUMN follow_up_task_log.member_id IS '家庭成员ID';
COMMENT ON COLUMN follow_up_task_log.task_type IS '任务类型（REMINDER提醒任务 REPORT_ADVICE报告建议任务 OPERATION运营任务）';
COMMENT ON COLUMN follow_up_task_log.source_id IS '来源业务ID';
COMMENT ON COLUMN follow_up_task_log.action_type IS '操作类型（READ已读 DELAY延后 COMPLETE完成 IGNORE忽略 RESTORE恢复）';
COMMENT ON COLUMN follow_up_task_log.before_status IS '操作前状态（0待跟进 1已延后 2已完成 3已忽略）';
COMMENT ON COLUMN follow_up_task_log.after_status IS '操作后状态（0待跟进 1已延后 2已完成 3已忽略）';
COMMENT ON COLUMN follow_up_task_log.action_remark IS '操作备注';
COMMENT ON COLUMN follow_up_task_log.delayed_until_snapshot IS '延后到期时间快照';
COMMENT ON COLUMN follow_up_task_log.creator_id IS '创建者ID';
COMMENT ON COLUMN follow_up_task_log.create_time IS '创建时间';
COMMENT ON COLUMN follow_up_task_log.updater_id IS '更新者ID';
COMMENT ON COLUMN follow_up_task_log.update_time IS '更新时间';
COMMENT ON COLUMN follow_up_task_log.deleted IS '逻辑删除';

COMMENT ON TABLE operation_task IS '健康系统首页运营任务表';
COMMENT ON COLUMN operation_task.operation_task_id IS '运营任务ID';
COMMENT ON COLUMN operation_task.owner_user_id IS '目标App用户ID';
COMMENT ON COLUMN operation_task.member_id IS '可选的目标家庭成员ID';
COMMENT ON COLUMN operation_task.task_title IS '任务标题';
COMMENT ON COLUMN operation_task.task_content IS '任务内容';
COMMENT ON COLUMN operation_task.action_text IS '动作按钮文案';
COMMENT ON COLUMN operation_task.risk_level IS '风险等级（LOW低 MEDIUM中 HIGH高）';
COMMENT ON COLUMN operation_task.priority_weight IS '排序权重，值越大越靠前';
COMMENT ON COLUMN operation_task.target_page_code IS '目标页面编码';
COMMENT ON COLUMN operation_task.target_page_name IS '目标页面名称快照';
COMMENT ON COLUMN operation_task.target_biz_id IS '目标业务主键ID';
COMMENT ON COLUMN operation_task.target_biz_type IS '目标业务类型';
COMMENT ON COLUMN operation_task.target_tab_code IS '目标标签编码';
COMMENT ON COLUMN operation_task.target_anchor_code IS '目标锚点编码';
COMMENT ON COLUMN operation_task.target_anchor_name IS '目标锚点名称快照';
COMMENT ON COLUMN operation_task.start_time IS '生效开始时间';
COMMENT ON COLUMN operation_task.end_time IS '生效结束时间';
COMMENT ON COLUMN operation_task.status IS '状态（1启用 0停用）';
COMMENT ON COLUMN operation_task.remark IS '备注';
COMMENT ON COLUMN operation_task.creator_id IS '创建者ID';
COMMENT ON COLUMN operation_task.create_time IS '创建时间';
COMMENT ON COLUMN operation_task.updater_id IS '更新者ID';
COMMENT ON COLUMN operation_task.update_time IS '更新时间';
COMMENT ON COLUMN operation_task.deleted IS '逻辑删除';

COMMENT ON TABLE family_member_share IS '健康系统家庭成员共享关系表';
COMMENT ON COLUMN family_member_share.share_id IS '共享关系ID';
COMMENT ON COLUMN family_member_share.member_id IS '家庭成员ID';
COMMENT ON COLUMN family_member_share.owner_user_id IS '主账号App用户ID';
COMMENT ON COLUMN family_member_share.collaborator_user_id IS '协同账号App用户ID';
COMMENT ON COLUMN family_member_share.invite_id IS '来源邀请ID';
COMMENT ON COLUMN family_member_share.share_role IS '共享角色（OWNER/EDITOR/VIEWER）';
COMMENT ON COLUMN family_member_share.share_status IS '共享状态（0停用 1生效）';
COMMENT ON COLUMN family_member_share.accepted_time IS '接受共享时间';
COMMENT ON COLUMN family_member_share.remark IS '备注';
COMMENT ON COLUMN family_member_share.creator_id IS '创建者ID';
COMMENT ON COLUMN family_member_share.create_time IS '创建时间';
COMMENT ON COLUMN family_member_share.updater_id IS '更新者ID';
COMMENT ON COLUMN family_member_share.update_time IS '更新时间';
COMMENT ON COLUMN family_member_share.deleted IS '逻辑删除';

COMMENT ON TABLE family_share_invite IS '健康系统家庭成员共享邀请表';
COMMENT ON COLUMN family_share_invite.invite_id IS '共享邀请ID';
COMMENT ON COLUMN family_share_invite.member_id IS '家庭成员ID';
COMMENT ON COLUMN family_share_invite.owner_user_id IS '主账号App用户ID';
COMMENT ON COLUMN family_share_invite.invite_code IS '邀请码';
COMMENT ON COLUMN family_share_invite.invitee_user_id IS '接受邀请的App用户ID';
COMMENT ON COLUMN family_share_invite.share_role IS '邀请角色（EDITOR/VIEWER）';
COMMENT ON COLUMN family_share_invite.invite_status IS '邀请状态（0待接受 1已接受 2已取消 3已过期）';
COMMENT ON COLUMN family_share_invite.expire_time IS '邀请过期时间';
COMMENT ON COLUMN family_share_invite.accepted_time IS '接受时间';
COMMENT ON COLUMN family_share_invite.remark IS '备注';
COMMENT ON COLUMN family_share_invite.creator_id IS '创建者ID';
COMMENT ON COLUMN family_share_invite.create_time IS '创建时间';
COMMENT ON COLUMN family_share_invite.updater_id IS '更新者ID';
COMMENT ON COLUMN family_share_invite.update_time IS '更新时间';
COMMENT ON COLUMN family_share_invite.deleted IS '逻辑删除';

COMMENT ON TABLE indicator_template IS '健康系统体检指标模板表';
COMMENT ON COLUMN indicator_template.template_id IS '指标模板ID';
COMMENT ON COLUMN indicator_template.report_type IS '报告类型';
COMMENT ON COLUMN indicator_template.item_code IS '指标编码';
COMMENT ON COLUMN indicator_template.item_name IS '指标名称';
COMMENT ON COLUMN indicator_template.result_unit IS '结果单位';
COMMENT ON COLUMN indicator_template.reference_min IS '参考范围下限';
COMMENT ON COLUMN indicator_template.reference_max IS '参考范围上限';
COMMENT ON COLUMN indicator_template.reference_text IS '参考范围文本';
COMMENT ON COLUMN indicator_template.suggestion_template IS '建议模板';
COMMENT ON COLUMN indicator_template.sort IS '排序号';
COMMENT ON COLUMN indicator_template.status IS '状态（1启用 0停用）';
COMMENT ON COLUMN indicator_template.creator_id IS '创建者ID';
COMMENT ON COLUMN indicator_template.create_time IS '创建时间';
COMMENT ON COLUMN indicator_template.updater_id IS '更新者ID';
COMMENT ON COLUMN indicator_template.update_time IS '更新时间';
COMMENT ON COLUMN indicator_template.deleted IS '逻辑删除';

CREATE TABLE IF NOT EXISTS member_feature (
    member_feature_id BIGSERIAL PRIMARY KEY,
    feature_code VARCHAR(64) NOT NULL,
    feature_name VARCHAR(100) NOT NULL,
    feature_type VARCHAR(20) NOT NULL,
    quota_period_type VARCHAR(20),
    free_enabled SMALLINT DEFAULT 0 NOT NULL,
    free_limit_value INT,
    feature_sort INT DEFAULT 0 NOT NULL,
    status SMALLINT DEFAULT 1 NOT NULL,
    is_builtin SMALLINT DEFAULT 1 NOT NULL,
    remark VARCHAR(500),
    creator_id BIGINT,
    create_time TIMESTAMPTZ,
    updater_id BIGINT,
    update_time TIMESTAMPTZ,
    deleted SMALLINT DEFAULT 0 NOT NULL
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_member_feature_code ON member_feature (feature_code);
CREATE INDEX IF NOT EXISTS idx_member_feature_status ON member_feature (status);
CREATE INDEX IF NOT EXISTS idx_member_feature_sort ON member_feature (feature_sort);

CREATE TABLE IF NOT EXISTS member_level (
    member_level_id BIGSERIAL PRIMARY KEY,
    level_code VARCHAR(32) NOT NULL,
    level_name VARCHAR(100) NOT NULL,
    level_sort INT DEFAULT 0 NOT NULL,
    price NUMERIC(10, 2) DEFAULT 0 NOT NULL,
    duration_days INT,
    benefit_desc VARCHAR(1000),
    status SMALLINT DEFAULT 1 NOT NULL,
    remark VARCHAR(500),
    creator_id BIGINT,
    create_time TIMESTAMPTZ,
    updater_id BIGINT,
    update_time TIMESTAMPTZ,
    deleted SMALLINT DEFAULT 0 NOT NULL
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_member_level_code ON member_level (level_code);
CREATE INDEX IF NOT EXISTS idx_member_level_status ON member_level (status);
CREATE INDEX IF NOT EXISTS idx_member_level_sort ON member_level (level_sort);

CREATE TABLE IF NOT EXISTS member_level_feature (
    member_level_feature_id BIGSERIAL PRIMARY KEY,
    member_level_id BIGINT NOT NULL,
    member_feature_id BIGINT NOT NULL,
    enabled SMALLINT DEFAULT 0 NOT NULL,
    limit_value INT,
    remark VARCHAR(500),
    creator_id BIGINT,
    create_time TIMESTAMPTZ,
    updater_id BIGINT,
    update_time TIMESTAMPTZ,
    deleted SMALLINT DEFAULT 0 NOT NULL
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_member_level_feature_level_feature
    ON member_level_feature (member_level_id, member_feature_id, deleted);
CREATE INDEX IF NOT EXISTS idx_member_level_feature_level_id ON member_level_feature (member_level_id);
CREATE INDEX IF NOT EXISTS idx_member_level_feature_feature_id ON member_level_feature (member_feature_id);

CREATE TABLE IF NOT EXISTS user_member (
    user_member_id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    member_level_id BIGINT NOT NULL,
    effective_start_time TIMESTAMPTZ NOT NULL,
    effective_end_time TIMESTAMPTZ,
    status VARCHAR(20) NOT NULL,
    source_type VARCHAR(30) NOT NULL,
    source_id BIGINT,
    remark VARCHAR(500),
    creator_id BIGINT,
    create_time TIMESTAMPTZ,
    updater_id BIGINT,
    update_time TIMESTAMPTZ,
    deleted SMALLINT DEFAULT 0 NOT NULL
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_user_member_user_id ON user_member (user_id, deleted);
CREATE INDEX IF NOT EXISTS idx_user_member_level_id ON user_member (member_level_id);
CREATE INDEX IF NOT EXISTS idx_user_member_status ON user_member (status);
CREATE INDEX IF NOT EXISTS idx_user_member_effective_end_time ON user_member (effective_end_time);

CREATE TABLE IF NOT EXISTS member_redeem_code (
    member_redeem_code_id BIGSERIAL PRIMARY KEY,
    batch_no VARCHAR(64) NOT NULL,
    redeem_code VARCHAR(64) NOT NULL,
    member_level_id BIGINT NOT NULL,
    level_code_snapshot VARCHAR(32) NOT NULL,
    level_name_snapshot VARCHAR(100) NOT NULL,
    price_snapshot NUMERIC(10, 2) DEFAULT 0 NOT NULL,
    duration_days_snapshot INT,
    code_status VARCHAR(20) NOT NULL,
    redeemed_user_id BIGINT,
    redeemed_time TIMESTAMPTZ,
    expire_time TIMESTAMPTZ,
    remark VARCHAR(500),
    creator_id BIGINT,
    create_time TIMESTAMPTZ,
    updater_id BIGINT,
    update_time TIMESTAMPTZ,
    deleted SMALLINT DEFAULT 0 NOT NULL
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_member_redeem_code_code ON member_redeem_code (redeem_code);
CREATE INDEX IF NOT EXISTS idx_member_redeem_code_batch_no ON member_redeem_code (batch_no);
CREATE INDEX IF NOT EXISTS idx_member_redeem_code_status ON member_redeem_code (code_status);
CREATE INDEX IF NOT EXISTS idx_member_redeem_code_level_id ON member_redeem_code (member_level_id);

CREATE TABLE IF NOT EXISTS member_feature_quota_usage (
    member_feature_quota_usage_id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    member_feature_id BIGINT NOT NULL,
    feature_code_snapshot VARCHAR(64) NOT NULL,
    quota_period_type VARCHAR(20) NOT NULL,
    period_key VARCHAR(16) NOT NULL,
    used_count INT DEFAULT 0 NOT NULL,
    remark VARCHAR(500),
    creator_id BIGINT,
    create_time TIMESTAMPTZ,
    updater_id BIGINT,
    update_time TIMESTAMPTZ,
    deleted SMALLINT DEFAULT 0 NOT NULL
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_member_feature_quota_usage_user_feature_period
    ON member_feature_quota_usage (user_id, member_feature_id, period_key, deleted);
CREATE INDEX IF NOT EXISTS idx_member_feature_quota_usage_feature_id ON member_feature_quota_usage (member_feature_id);
CREATE INDEX IF NOT EXISTS idx_member_feature_quota_usage_period_key ON member_feature_quota_usage (period_key);

CREATE TABLE IF NOT EXISTS user_member_order (
    user_member_order_id BIGSERIAL PRIMARY KEY,
    order_no VARCHAR(64) NOT NULL,
    subscription_id BIGINT,
    user_id BIGINT NOT NULL,
    member_level_id BIGINT NOT NULL,
    order_type VARCHAR(20) NOT NULL,
    order_status VARCHAR(20) NOT NULL,
    source_type VARCHAR(30) NOT NULL,
    order_amount NUMERIC(10, 2) DEFAULT 0 NOT NULL,
    pay_time TIMESTAMPTZ,
    effective_start_time TIMESTAMPTZ,
    effective_end_time TIMESTAMPTZ,
    remark VARCHAR(500),
    creator_id BIGINT,
    create_time TIMESTAMPTZ,
    updater_id BIGINT,
    update_time TIMESTAMPTZ,
    deleted SMALLINT DEFAULT 0 NOT NULL
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_user_member_order_order_no ON user_member_order (order_no);
CREATE INDEX IF NOT EXISTS idx_user_member_order_user_id ON user_member_order (user_id);
CREATE INDEX IF NOT EXISTS idx_user_member_order_level_id ON user_member_order (member_level_id);
CREATE INDEX IF NOT EXISTS idx_user_member_order_status ON user_member_order (order_status);

CREATE TABLE IF NOT EXISTS user_member_subscription (
    subscription_id BIGSERIAL PRIMARY KEY,
    subscription_no VARCHAR(64) NOT NULL,
    user_id BIGINT NOT NULL,
    member_level_id BIGINT NOT NULL,
    subscription_status VARCHAR(20) NOT NULL,
    auto_renew SMALLINT DEFAULT 0 NOT NULL,
    current_period_start_time TIMESTAMPTZ,
    current_period_end_time TIMESTAMPTZ,
    next_renew_time TIMESTAMPTZ,
    last_renew_time TIMESTAMPTZ,
    failed_renew_count INT DEFAULT 0 NOT NULL,
    cancel_time TIMESTAMPTZ,
    cancel_reason VARCHAR(500),
    remark VARCHAR(500),
    creator_id BIGINT,
    create_time TIMESTAMPTZ,
    updater_id BIGINT,
    update_time TIMESTAMPTZ,
    deleted SMALLINT DEFAULT 0 NOT NULL
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_user_member_subscription_no ON user_member_subscription (subscription_no);
CREATE UNIQUE INDEX IF NOT EXISTS uk_user_member_subscription_user_id ON user_member_subscription (user_id, deleted);
CREATE INDEX IF NOT EXISTS idx_user_member_subscription_level_id ON user_member_subscription (member_level_id);
CREATE INDEX IF NOT EXISTS idx_user_member_subscription_status ON user_member_subscription (subscription_status);

COMMENT ON TABLE member_feature IS '会员权益定义表';
COMMENT ON COLUMN member_feature.member_feature_id IS '会员权益ID';
COMMENT ON COLUMN member_feature.feature_code IS '权益编码';
COMMENT ON COLUMN member_feature.feature_name IS '权益名称';
COMMENT ON COLUMN member_feature.feature_type IS '权益类型 SWITCH/LIMIT/QUOTA';
COMMENT ON COLUMN member_feature.quota_period_type IS '次数周期 DAY/MONTH';
COMMENT ON COLUMN member_feature.free_enabled IS '免费默认是否启用 0否1是';
COMMENT ON COLUMN member_feature.free_limit_value IS '免费默认限制值，null 表示不限';
COMMENT ON COLUMN member_feature.feature_sort IS '排序值';
COMMENT ON COLUMN member_feature.status IS '状态 0停用1启用';
COMMENT ON COLUMN member_feature.is_builtin IS '是否内置 0否1是';
COMMENT ON COLUMN member_feature.remark IS '备注';
COMMENT ON COLUMN member_feature.creator_id IS '创建者ID';
COMMENT ON COLUMN member_feature.create_time IS '创建时间';
COMMENT ON COLUMN member_feature.updater_id IS '更新者ID';
COMMENT ON COLUMN member_feature.update_time IS '更新时间';
COMMENT ON COLUMN member_feature.deleted IS '删除标志（0存在 1删除）';

COMMENT ON TABLE member_level IS '会员等级表';
COMMENT ON COLUMN member_level.member_level_id IS '会员等级ID';
COMMENT ON COLUMN member_level.level_code IS '会员等级编码';
COMMENT ON COLUMN member_level.level_name IS '会员等级名称';
COMMENT ON COLUMN member_level.level_sort IS '排序值';
COMMENT ON COLUMN member_level.price IS '价格';
COMMENT ON COLUMN member_level.duration_days IS '默认时长，单位天；为空表示不自动推导到期时间';
COMMENT ON COLUMN member_level.benefit_desc IS '权益说明';
COMMENT ON COLUMN member_level.status IS '状态 0停用1启用';
COMMENT ON COLUMN member_level.remark IS '备注';
COMMENT ON COLUMN member_level.creator_id IS '创建者ID';
COMMENT ON COLUMN member_level.create_time IS '创建时间';
COMMENT ON COLUMN member_level.updater_id IS '更新者ID';
COMMENT ON COLUMN member_level.update_time IS '更新时间';
COMMENT ON COLUMN member_level.deleted IS '删除标志（0存在 1删除）';

COMMENT ON TABLE member_level_feature IS '会员等级权益矩阵表';
COMMENT ON COLUMN member_level_feature.member_level_feature_id IS '会员等级权益ID';
COMMENT ON COLUMN member_level_feature.member_level_id IS '会员等级ID';
COMMENT ON COLUMN member_level_feature.member_feature_id IS '会员权益ID';
COMMENT ON COLUMN member_level_feature.enabled IS '是否启用 0否1是';
COMMENT ON COLUMN member_level_feature.limit_value IS '等级覆盖额度，null 表示不限';
COMMENT ON COLUMN member_level_feature.remark IS '备注';
COMMENT ON COLUMN member_level_feature.creator_id IS '创建者ID';
COMMENT ON COLUMN member_level_feature.create_time IS '创建时间';
COMMENT ON COLUMN member_level_feature.updater_id IS '更新者ID';
COMMENT ON COLUMN member_level_feature.update_time IS '更新时间';
COMMENT ON COLUMN member_level_feature.deleted IS '删除标志（0存在 1删除）';

COMMENT ON TABLE user_member IS '用户会员关系表';
COMMENT ON COLUMN user_member.user_member_id IS '用户会员ID';
COMMENT ON COLUMN user_member.user_id IS '用户ID';
COMMENT ON COLUMN user_member.member_level_id IS '会员等级ID';
COMMENT ON COLUMN user_member.effective_start_time IS '会员开始时间';
COMMENT ON COLUMN user_member.effective_end_time IS '会员结束时间，为空表示长期有效';
COMMENT ON COLUMN user_member.status IS '会员状态 ACTIVE/EXPIRED/DISABLED';
COMMENT ON COLUMN user_member.source_type IS '来源类型 SUBSCRIPTION/REDEEM_CODE/ADMIN_GRANT';
COMMENT ON COLUMN user_member.source_id IS '来源记录ID';
COMMENT ON COLUMN user_member.remark IS '备注';
COMMENT ON COLUMN user_member.creator_id IS '创建者ID';
COMMENT ON COLUMN user_member.create_time IS '创建时间';
COMMENT ON COLUMN user_member.updater_id IS '更新者ID';
COMMENT ON COLUMN user_member.update_time IS '更新时间';
COMMENT ON COLUMN user_member.deleted IS '删除标志（0存在 1删除）';

COMMENT ON TABLE member_redeem_code IS '会员兑换码表';
COMMENT ON COLUMN member_redeem_code.member_redeem_code_id IS '会员兑换码ID';
COMMENT ON COLUMN member_redeem_code.batch_no IS '生成批次号';
COMMENT ON COLUMN member_redeem_code.redeem_code IS '兑换码';
COMMENT ON COLUMN member_redeem_code.member_level_id IS '会员等级ID';
COMMENT ON COLUMN member_redeem_code.level_code_snapshot IS '等级编码快照';
COMMENT ON COLUMN member_redeem_code.level_name_snapshot IS '等级名称快照';
COMMENT ON COLUMN member_redeem_code.price_snapshot IS '价格快照';
COMMENT ON COLUMN member_redeem_code.duration_days_snapshot IS '时长快照，单位天；为空表示长期有效';
COMMENT ON COLUMN member_redeem_code.code_status IS '兑换码状态 AVAILABLE/REDEEMED/EXPIRED/DISABLED';
COMMENT ON COLUMN member_redeem_code.redeemed_user_id IS '兑换用户ID';
COMMENT ON COLUMN member_redeem_code.redeemed_time IS '兑换时间';
COMMENT ON COLUMN member_redeem_code.expire_time IS '过期时间';
COMMENT ON COLUMN member_redeem_code.remark IS '备注';
COMMENT ON COLUMN member_redeem_code.creator_id IS '创建者ID';
COMMENT ON COLUMN member_redeem_code.create_time IS '创建时间';
COMMENT ON COLUMN member_redeem_code.updater_id IS '更新者ID';
COMMENT ON COLUMN member_redeem_code.update_time IS '更新时间';
COMMENT ON COLUMN member_redeem_code.deleted IS '删除标志（0存在 1删除）';

COMMENT ON TABLE member_feature_quota_usage IS '会员权益次数使用记录表';
COMMENT ON COLUMN member_feature_quota_usage.member_feature_quota_usage_id IS '使用记录ID';
COMMENT ON COLUMN member_feature_quota_usage.user_id IS '用户ID';
COMMENT ON COLUMN member_feature_quota_usage.member_feature_id IS '会员权益ID';
COMMENT ON COLUMN member_feature_quota_usage.feature_code_snapshot IS '权益编码快照';
COMMENT ON COLUMN member_feature_quota_usage.quota_period_type IS '周期类型 DAY/MONTH';
COMMENT ON COLUMN member_feature_quota_usage.period_key IS '周期键，例如 202604 或 20260427';
COMMENT ON COLUMN member_feature_quota_usage.used_count IS '当前周期已使用次数';
COMMENT ON COLUMN member_feature_quota_usage.remark IS '备注';
COMMENT ON COLUMN member_feature_quota_usage.creator_id IS '创建者ID';
COMMENT ON COLUMN member_feature_quota_usage.create_time IS '创建时间';
COMMENT ON COLUMN member_feature_quota_usage.updater_id IS '更新者ID';
COMMENT ON COLUMN member_feature_quota_usage.update_time IS '更新时间';
COMMENT ON COLUMN member_feature_quota_usage.deleted IS '删除标志（0存在 1删除）';

COMMENT ON TABLE user_member_order IS '用户会员订单表';
COMMENT ON COLUMN user_member_order.user_member_order_id IS '用户会员订单ID';
COMMENT ON COLUMN user_member_order.order_no IS '订单号';
COMMENT ON COLUMN user_member_order.subscription_id IS '订阅ID';
COMMENT ON COLUMN user_member_order.user_id IS '用户ID';
COMMENT ON COLUMN user_member_order.member_level_id IS '会员等级ID';
COMMENT ON COLUMN user_member_order.order_type IS '订单类型 OPEN/RENEW/GRANT/REDEEM';
COMMENT ON COLUMN user_member_order.order_status IS '订单状态 SUCCESS/FAILED/CANCELED';
COMMENT ON COLUMN user_member_order.source_type IS '来源类型 APP_USER/AUTO_RENEW/ADMIN/REDEEM_CODE';
COMMENT ON COLUMN user_member_order.order_amount IS '订单金额';
COMMENT ON COLUMN user_member_order.pay_time IS '支付时间';
COMMENT ON COLUMN user_member_order.effective_start_time IS '会员开始时间';
COMMENT ON COLUMN user_member_order.effective_end_time IS '会员结束时间';
COMMENT ON COLUMN user_member_order.remark IS '备注';
COMMENT ON COLUMN user_member_order.creator_id IS '创建者ID';
COMMENT ON COLUMN user_member_order.create_time IS '创建时间';
COMMENT ON COLUMN user_member_order.updater_id IS '更新者ID';
COMMENT ON COLUMN user_member_order.update_time IS '更新时间';
COMMENT ON COLUMN user_member_order.deleted IS '删除标志（0存在 1删除）';

COMMENT ON TABLE user_member_subscription IS '用户会员订阅表';
COMMENT ON COLUMN user_member_subscription.subscription_id IS '订阅ID';
COMMENT ON COLUMN user_member_subscription.subscription_no IS '订阅编号';
COMMENT ON COLUMN user_member_subscription.user_id IS '用户ID';
COMMENT ON COLUMN user_member_subscription.member_level_id IS '会员等级ID';
COMMENT ON COLUMN user_member_subscription.subscription_status IS '订阅状态 ACTIVE/CANCELED/EXPIRED/FAILED';
COMMENT ON COLUMN user_member_subscription.auto_renew IS '是否自动续费 0否1是';
COMMENT ON COLUMN user_member_subscription.current_period_start_time IS '当前周期开始时间';
COMMENT ON COLUMN user_member_subscription.current_period_end_time IS '当前周期结束时间';
COMMENT ON COLUMN user_member_subscription.next_renew_time IS '下次续费时间';
COMMENT ON COLUMN user_member_subscription.last_renew_time IS '最近续费时间';
COMMENT ON COLUMN user_member_subscription.failed_renew_count IS '连续失败次数';
COMMENT ON COLUMN user_member_subscription.cancel_time IS '取消时间';
COMMENT ON COLUMN user_member_subscription.cancel_reason IS '取消原因';
COMMENT ON COLUMN user_member_subscription.remark IS '备注';
COMMENT ON COLUMN user_member_subscription.creator_id IS '创建者ID';
COMMENT ON COLUMN user_member_subscription.create_time IS '创建时间';
COMMENT ON COLUMN user_member_subscription.updater_id IS '更新者ID';
COMMENT ON COLUMN user_member_subscription.update_time IS '更新时间';
COMMENT ON COLUMN user_member_subscription.deleted IS '删除标志（0存在 1删除）';
