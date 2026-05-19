package com.healthtrail.common.exception.error;

import org.springframework.util.Assert;

/**
 * 错误码集合
 *
 * @author valarchie
 */
public enum ErrorCode implements ErrorCodeInterface {

    /**
     * 错误码集合
     * ******以下是旧的设计****
     * 1~9999 为保留错误码 或者 常用错误码
     * 10000~19999 为内部错误码
     * 20000~29999 客户端错误码 （客户端异常调用之类的错误）
     * 30000~39999 为第三方错误码 （代码正常，但是第三方异常）
     * 40000~49999 为业务逻辑 错误码 （无异常，代码正常流转，并返回提示给用户）
     * 由于系统内的错误码都是独一无二的，所以错误码应该放在common包集中管理
     * ---------------------------
     * 旧的设计的缺陷，比如内部错误码其实并不会很多  但是占用了1~9999的序列，其实是不必要的。
     * 而且错误码不一定位数一定要相同。比如腾讯的微信接口错误码的位数就并不相同。按照常理错误码的数量大小应该是：
     * 内部错误码< 客户端错误码< 第三方错误码< 业务错误码
     * 所以我们应该尽可能的把错误码的数量留给业务错误码
     * ---------------------------
     * *******新的设计**********
     * 1~99 为内部错误码（框架本身的错误）
     * 100~999 客户端错误码 （客户端异常调用之类的错误）
     * 1000~9999为第三方错误码 （代码正常，但是第三方异常）
     * 10000~99999 为业务逻辑 错误码 （无异常，代码正常流转，并返回提示给用户）
     * 由于系统内的错误码都是独一无二的，所以错误码应该放在common包集中管理
     * ---------------------------
     * 总体设计就是值越小  错误严重性越高
     * 目前10000~19999是初始系统内嵌功能使用的错误码，后续开发者可以直接使用20000以上的错误码作为业务错误码
     */

    SUCCESS(0, "操作成功", "SUCCESS"),
    FAILED(99999, "操作失败", "FAILED");

    private final int code;
    private final String msg;
    private final String i18nKey;

    ErrorCode(int code, String msg, String i18nKey) {
        this.code = code;
        this.msg = msg;
        this.i18nKey = i18nKey;
    }

    @Override
    public int code() {
        return this.code;
    }

    @Override
    public String message() {
        return this.msg;
    }

    @Override
    public String i18nKey() {
        return this.i18nKey;
    }

    /**
     * 10000~99999 为业务逻辑 错误码 （无代码异常，代码正常流转，并返回提示给用户）
     * 1XX01   XX是代表模块的意思 比如10101   01是Permission模块
     * 错误码的命名最好以模块为开头  比如  NOT_ALLOWED_TO_OPERATE前面加上PERMISSION = PERMISSION_NOT_ALLOWED_TO_OPERATE
     */
    public enum Business implements ErrorCodeInterface {

        // ----------------------------- COMMON --------------------------------------

        COMMON_OBJECT_NOT_FOUND(10001, "找不到ID为 {} 的 {}", "Business.OBJECT_NOT_FOUND"),

        COMMON_UNSUPPORTED_OPERATION(10002, "不支持的操作", "Business.UNSUPPORTED_OPERATION"),

        COMMON_BULK_DELETE_IDS_IS_INVALID(10003, "批量参数ID列表为空", "Business.BULK_DELETE_IDS_IS_INVALID"),

        COMMON_FILE_NOT_ALLOWED_TO_DOWNLOAD(10004, "文件名称({})非法，不允许下载", "Business.FILE_NOT_ALLOWED_TO_DOWNLOAD"),

        // ----------------------------- PERMISSION -----------------------------------

        PERMISSION_FORBIDDEN_TO_MODIFY_ADMIN(10101, "不允许修改管理员的信息", "Business.FORBIDDEN_TO_MODIFY_ADMIN"),

        PERMISSION_NOT_ALLOWED_TO_OPERATE(10202, "没有权限进行此操作，请联系管理员", "Business.NO_PERMISSION_TO_OPERATE"),

        // ----------------------------- LOGIN -----------------------------------------

        LOGIN_WRONG_USER_PASSWORD(10201, "用户密码错误，请重输", "Business.LOGIN_WRONG_USER_PASSWORD"),

        LOGIN_ERROR(10202, "登录失败：{}", "Business.LOGIN_ERROR"),

        LOGIN_CAPTCHA_CODE_WRONG(10203, "验证码错误", "Business.LOGIN_CAPTCHA_CODE_WRONG"),

        LOGIN_CAPTCHA_CODE_EXPIRE(10204, "验证码过期", "Business.LOGIN_CAPTCHA_CODE_EXPIRE"),

        LOGIN_CAPTCHA_CODE_NULL(10205, "验证码为空", "Business.LOGIN_CAPTCHA_CODE_NULL"),

        // ----------------------------- UPLOAD -----------------------------------------

        UPLOAD_FILE_TYPE_NOT_ALLOWED(10401, "不允许上传的文件类型，仅允许：{}", "Business.UPLOAD_FILE_TYPE_NOT_ALLOWED"),

        UPLOAD_FILE_NAME_EXCEED_MAX_LENGTH(10402, "文件名长度超过：{} ", "Business.UPLOAD_FILE_NAME_EXCEED_MAX_LENGTH"),

        UPLOAD_FILE_SIZE_EXCEED_MAX_SIZE(10403, "文件名大小超过：{} MB", "Business.UPLOAD_FILE_SIZE_EXCEED_MAX_SIZE"),

        UPLOAD_IMPORT_EXCEL_FAILED(10404, "导入excel失败：{}", "Business.UPLOAD_IMPORT_EXCEL_FAILED"),

        UPLOAD_FILE_IS_EMPTY(10405, "上传文件为空", "Business.UPLOAD_FILE_IS_EMPTY"),

        UPLOAD_FILE_FAILED(10406, "上传文件失败：{}", "Business.UPLOAD_FILE_FAILED"),

        // ----------------------------- CONFIG -----------------------------------------

        CONFIG_VALUE_IS_NOT_ALLOW_TO_EMPTY(10601, "参数键值不允许为空", "Business.CONFIG_VALUE_IS_NOT_ALLOW_TO_EMPTY"),

        CONFIG_VALUE_IS_NOT_IN_OPTIONS(10602, "参数键值不存在列表中", "Business.CONFIG_VALUE_IS_NOT_IN_OPTIONS"),

        // ------------------------------- POST --------------------------------------------

        POST_NAME_IS_NOT_UNIQUE(10701, "岗位名称:{}, 已存在", "Business.POST_NAME_IS_NOT_UNIQUE"),

        POST_CODE_IS_NOT_UNIQUE(10702, "岗位编号:{}, 已存在", "Business.POST_CODE_IS_NOT_UNIQUE"),

        POST_ALREADY_ASSIGNED_TO_USER_CAN_NOT_BE_DELETED(10703, "职位已分配给用户，请先取消分配再删除", "Business.POST_ALREADY_ASSIGNED_TO_USER_CAN_NOT_BE_DELETED"),

        // ------------------------------- DEPT ---------------------------------------------

        DEPT_NAME_IS_NOT_UNIQUE(10801, "部门名称:{}, 已存在", "Business.DEPT_NAME_IS_NOT_UNIQUE"),

        DEPT_PARENT_ID_IS_NOT_ALLOWED_SELF(10802, "父级部门不能选择自己", "Business.DEPT_PARENT_ID_IS_NOT_ALLOWED_SELF"),

        DEPT_STATUS_ID_IS_NOT_ALLOWED_CHANGE(10803, "子部门还有正在启用的部门，暂时不能停用该部门", "Business.DEPT_STATUS_ID_IS_NOT_ALLOWED_CHANGE"),

        DEPT_EXIST_CHILD_DEPT_NOT_ALLOW_DELETE(10804, "该部门存在下级部门不允许删除", "Business.DEPT_EXIST_CHILD_DEPT_NOT_ALLOW_DELETE"),

        DEPT_EXIST_LINK_USER_NOT_ALLOW_DELETE(10805, "该部门存在关联的用户不允许删除", "Business.DEPT_EXIST_LINK_USER_NOT_ALLOW_DELETE"),

        DEPT_PARENT_DEPT_NO_EXIST_OR_DISABLED(10806, "该父级部门不存在或已停用", "Business.DEPT_PARENT_DEPT_NO_EXIST_OR_DISABLED"),

        // -------------------------------  MENU -------------------------------------------------

        MENU_NAME_IS_NOT_UNIQUE(10901, "新增菜单:{} 失败，菜单名称已存在", "Business.MENU_NAME_IS_NOT_UNIQUE"),

        MENU_EXTERNAL_LINK_MUST_BE_HTTP(10902, "菜单外链必须以 http(s)://开头", "Business.MENU_EXTERNAL_LINK_MUST_BE_HTTP"),

        MENU_PARENT_ID_NOT_ALLOW_SELF(10903, "父级菜单不能选择自身", "Business.MENU_PARENT_ID_NOT_ALLOW_SELF"),

        MENU_EXIST_CHILD_MENU_NOT_ALLOW_DELETE(10904, "存在子菜单不允许删除", "Business.MENU_EXIST_CHILD_MENU_NOT_ALLOW_DELETE"),

        MENU_ALREADY_ASSIGN_TO_ROLE_NOT_ALLOW_DELETE(10905, "菜单已分配给角色，不允许", "Business.MENU_ALREADY_ASSIGN_TO_ROLE_NOT_ALLOW_DELETE"),

        MENU_NOT_ALLOWED_TO_CREATE_BUTTON_ON_IFRAME_OR_OUT_LINK(10906, "不允许在Iframe和外链跳转类型下创建按钮", "Business.MENU_ONLY_ALLOWED_TO_CREATE_BUTTON_ON_PAGE"),

        MENU_ONLY_ALLOWED_TO_CREATE_SUB_MENU_IN_CATALOG(10907, "只允许在目录类型底下创建子菜单", "Business.MENU_ONLY_ALLOWED_TO_CREATE_SUB_MENU_IN_CATALOG"),

        MENU_CAN_NOT_CHANGE_MENU_TYPE(10908, "不允许更改菜单的类型", "Business.MENU_CAN_NOT_CHANGE_MENU_TYPE"),

        // -------------------------------- ROLE -------------------------------------------------

        ROLE_NAME_IS_NOT_UNIQUE(11001, "角色名称：{}, 已存在", "Business.ROLE_NAME_IS_NOT_UNIQUE"),

        ROLE_KEY_IS_NOT_UNIQUE(11002, "角色标识：{}, 已存在", "Business.ROLE_KEY_IS_NOT_UNIQUE"),

        ROLE_DATA_SCOPE_DUPLICATED_DEPT(11003, "重复的部门id", "Business.ROLE_DATA_SCOPE_DUPLICATED_DEPT"),

        ROLE_ALREADY_ASSIGN_TO_USER(11004, "角色已分配给用户，请先取消分配，再删除角色", "Business.ROLE_ALREADY_ASSIGN_TO_USER"),

        ROLE_IS_NOT_AVAILABLE(11005, "角色：{} 已禁用，无法分配给用户", "Business.ROLE_IS_NOT_AVAILABLE"),

        // ---------------------------------- USER -----------------------------------------------

        USER_NON_EXIST(10501, "登录用户：{} 不存在", "Business.USER_NON_EXIST"),

        USER_IS_DISABLE(10502, "对不起， 您的账号：{} 已停用", "Business.USER_IS_DISABLE"),

        USER_CACHE_IS_EXPIRE(11003, "用户缓存信息已经过期", "Business.USER_CACHE_IS_EXPIRE"),

        USER_FAIL_TO_GET_USER_ID(11004, "获取用户ID失败", "Business.USER_FAIL_TO_GET_USER_ID"),

        USER_FAIL_TO_GET_DEPT_ID(10504, "获取用户部门ID失败", "Business.USER_FAIL_TO_GET_DEPT_ID"),

        USER_FAIL_TO_GET_ACCOUNT(10505, "获取用户账户失败", "Business.USER_FAIL_TO_GET_ACCOUNT"),

        USER_FAIL_TO_GET_USER_INFO(10506, "获取用户信息失败", "Business.USER_FAIL_TO_GET_USER_INFO"),

        USER_IMPORT_DATA_IS_NULL(10507, "导入的用户为空", "Business.USER_IMPORT_DATA_IS_NULL"),

        USER_PHONE_NUMBER_IS_NOT_UNIQUE(10508, "该电话号码已被其他用户占用", "Business.USER_PHONE_NUMBER_IS_NOT_UNIQUE"),

        USER_EMAIL_IS_NOT_UNIQUE(10509, "该邮件地址已被其他用户占用", "Business.USER_EMAIL_IS_NOT_UNIQUE"),

        USER_PASSWORD_IS_NOT_CORRECT(10510, "用户密码错误", "Business.USER_PASSWORD_IS_NOT_CORRECT"),

        USER_NEW_PASSWORD_IS_THE_SAME_AS_OLD(10511, "用户新密码与旧密码相同", "Business.USER_NEW_PASSWORD_IS_THE_SAME_AS_OLD"),

        USER_UPLOAD_FILE_FAILED(10512, "用户上传文件失败", "Business.USER_UPLOAD_FILE_FAILED"),

        USER_NAME_IS_NOT_UNIQUE(10513, "用户名已被其他用户占用", "Business.USER_NAME_IS_NOT_UNIQUE"),

        USER_CURRENT_USER_CAN_NOT_BE_DELETE(10514, "当前用户不允许被删除", "Business.USER_CURRENT_USER_CAN_NOT_BE_DELETE"),

        USER_ADMIN_CAN_NOT_BE_MODIFY(10515, "管理员不允许做任何修改", "Business.USER_ADMIN_CAN_NOT_BE_MODIFY"),

        // -------------------------------- APP USER ---------------------------------------------

        APP_USER_REGISTER_PASSWORD_CONFIRM_NOT_MATCH(12001, "两次输入的密码不一致", "Business.APP_USER_REGISTER_PASSWORD_CONFIRM_NOT_MATCH"),

        APP_USER_MOBILE_IS_NOT_UNIQUE(12002, "该手机号已被注册", "Business.APP_USER_MOBILE_IS_NOT_UNIQUE"),

        APP_USER_LOGIN_MOBILE_OR_PASSWORD_INVALID(12003, "手机号或密码错误", "Business.APP_USER_LOGIN_MOBILE_OR_PASSWORD_INVALID"),

        APP_USER_IS_DISABLE(12004, "该App账号已被停用", "Business.APP_USER_IS_DISABLE"),

        APP_USER_NON_EXIST(12005, "App用户不存在", "Business.APP_USER_NON_EXIST"),

        APP_MEDICATION_PLAN_DRUG_NAME_REQUIRED(12006, "药品信息不能为空，请选择药品或填写自定义药名", "Business.APP_MEDICATION_PLAN_DRUG_NAME_REQUIRED"),

        APP_MEDICATION_PLAN_DATE_RANGE_INVALID(12007, "用药计划的开始日期不能晚于结束日期", "Business.APP_MEDICATION_PLAN_DATE_RANGE_INVALID"),

        APP_MEDICATION_PLAN_REMINDER_TIMES_EMPTY(12008, "提醒时间不能为空", "Business.APP_MEDICATION_PLAN_REMINDER_TIMES_EMPTY"),

        APP_MEDICATION_PLAN_REMINDER_TIME_FORMAT_INVALID(12009, "提醒时间格式不正确，请使用HH:mm格式", "Business.APP_MEDICATION_PLAN_REMINDER_TIME_FORMAT_INVALID"),

        APP_MEDICATION_REMINDER_STATUS_NOT_ALLOW_OPERATE(12010, "当前提醒状态不允许执行该操作", "Business.APP_MEDICATION_REMINDER_STATUS_NOT_ALLOW_OPERATE"),

        HEALTH_DRUG_NAME_IS_NOT_UNIQUE(12011, "药品名称已存在，可补充规格/备注区分", "Business.HEALTH_DRUG_NAME_IS_NOT_UNIQUE"),

        APP_DEVICE_CODE_REQUIRED(12012, "设备编码不能为空", "Business.APP_DEVICE_CODE_REQUIRED"),

        APP_DEVICE_TOKEN_REQUIRED(12013, "设备Token不能为空", "Business.APP_DEVICE_TOKEN_REQUIRED"),

        APP_HEALTH_REPORT_NAME_TOO_LONG(12014, "报告名称长度不能超过100个字符", "Business.APP_HEALTH_REPORT_NAME_TOO_LONG"),

        APP_HEALTH_REPORT_ITEM_NAME_REQUIRED(12015, "报告指标名称不能为空", "Business.APP_HEALTH_REPORT_ITEM_NAME_REQUIRED"),

        APP_HEALTH_REPORT_ITEM_RESULT_REQUIRED(12016, "报告指标结果不能为空", "Business.APP_HEALTH_REPORT_ITEM_RESULT_REQUIRED"),

        APP_HEALTH_REPORT_ITEM_NAME_TOO_LONG(12017, "报告指标名称长度不能超过100个字符", "Business.APP_HEALTH_REPORT_ITEM_NAME_TOO_LONG"),

        APP_HEALTH_REPORT_ITEM_RESULT_TOO_LONG(12018, "报告指标结果长度不能超过100个字符", "Business.APP_HEALTH_REPORT_ITEM_RESULT_TOO_LONG"),

        APP_HEALTH_REPORT_ITEM_REFERENCE_TOO_LONG(12019, "报告指标参考范围长度不能超过100个字符", "Business.APP_HEALTH_REPORT_ITEM_REFERENCE_TOO_LONG"),

        APP_HEALTH_REPORT_ITEM_REFERENCE_RANGE_INVALID(12020, "报告指标参考范围下限不能大于上限", "Business.APP_HEALTH_REPORT_ITEM_REFERENCE_RANGE_INVALID"),

        APP_HEALTH_REPORT_ITEM_INTERPRETATION_TOO_LONG(12062, "报告指标解读长度不能超过300个字符", "Business.APP_HEALTH_REPORT_ITEM_INTERPRETATION_TOO_LONG"),

        APP_FOLLOW_UP_TASK_TYPE_INVALID(12021, "首页待跟进任务类型不正确", "Business.APP_FOLLOW_UP_TASK_TYPE_INVALID"),

        APP_FOLLOW_UP_TASK_DELAY_TIME_INVALID(12022, "任务延后时间必须晚于当前时间", "Business.APP_FOLLOW_UP_TASK_DELAY_TIME_INVALID"),

        APP_FOLLOW_UP_TASK_ACTION_NOT_ALLOWED(12023, "当前任务类型不支持该操作", "Business.APP_FOLLOW_UP_TASK_ACTION_NOT_ALLOWED"),

        APP_FOLLOW_UP_TASK_DELAY_PARAM_REQUIRED(12024, "延后时间或延后选项至少传一个", "Business.APP_FOLLOW_UP_TASK_DELAY_PARAM_REQUIRED"),

        APP_FOLLOW_UP_TASK_DELAY_OPTION_INVALID(12025, "任务延后选项不正确", "Business.APP_FOLLOW_UP_TASK_DELAY_OPTION_INVALID"),

        APP_FOLLOW_UP_TASK_NOT_FOUND(12026, "首页待跟进任务不存在", "Business.APP_FOLLOW_UP_TASK_NOT_FOUND"),

        APP_MESSAGE_NOT_FOUND(12027, "消息不存在", "Business.APP_MESSAGE_NOT_FOUND"),

        APP_MESSAGE_RESEND_DATA_INVALID(12028, "消息缺少必要发送数据，无法重发", "Business.APP_MESSAGE_RESEND_DATA_INVALID"),

        APP_FAMILY_MEMBER_SHARE_NOT_FOUND(12029, "家庭共享关系不存在", "Business.APP_FAMILY_MEMBER_SHARE_NOT_FOUND"),

        APP_FAMILY_SHARE_ROLE_INVALID(12030, "家庭共享角色不正确", "Business.APP_FAMILY_SHARE_ROLE_INVALID"),

        APP_FAMILY_SHARE_INVITE_NOT_FOUND(12031, "家庭共享邀请不存在", "Business.APP_FAMILY_SHARE_INVITE_NOT_FOUND"),

        APP_FAMILY_SHARE_INVITE_STATUS_INVALID(12032, "家庭共享邀请状态不允许执行该操作", "Business.APP_FAMILY_SHARE_INVITE_STATUS_INVALID"),

        APP_FAMILY_SHARE_INVITE_EXPIRED(12033, "家庭共享邀请已过期", "Business.APP_FAMILY_SHARE_INVITE_EXPIRED"),

        APP_FAMILY_CAN_NOT_SHARE_TO_SELF(12034, "不能把家庭成员共享给自己", "Business.APP_FAMILY_CAN_NOT_SHARE_TO_SELF"),

        APP_FAMILY_MEMBER_ALREADY_SHARED(12035, "该家庭成员已经共享给当前账号", "Business.APP_FAMILY_MEMBER_ALREADY_SHARED"),

        APP_FAMILY_MEMBER_NO_EDIT_PERMISSION(12036, "当前账号没有该家庭成员的协同管理权限", "Business.APP_FAMILY_MEMBER_NO_EDIT_PERMISSION"),

        APP_MEDICATION_HISTORY_DATE_RANGE_INVALID(12037, "服药历史查询开始时间不能晚于结束时间", "Business.APP_MEDICATION_HISTORY_DATE_RANGE_INVALID"),

        HEALTH_INDICATOR_TEMPLATE_ITEM_NAME_REQUIRED(12038, "指标模板名称不能为空", "Business.HEALTH_INDICATOR_TEMPLATE_ITEM_NAME_REQUIRED"),

        HEALTH_INDICATOR_TEMPLATE_REFERENCE_RANGE_INVALID(12039, "指标模板参考范围下限不能大于上限", "Business.HEALTH_INDICATOR_TEMPLATE_REFERENCE_RANGE_INVALID"),

        APP_REPORT_OCR_PLACEHOLDER_NOT_READY(12040, "当前报告暂不满足OCR占位解析条件", "Business.APP_REPORT_OCR_PLACEHOLDER_NOT_READY"),

        APP_REPORT_AI_SUMMARY_PLACEHOLDER_NOT_READY(12041, "当前报告暂不满足AI总结占位生成条件", "Business.APP_REPORT_AI_SUMMARY_PLACEHOLDER_NOT_READY"),

        APP_MEDICATION_PLAN_WEEKLY_DAYS_REQUIRED(12042, "每周提醒计划至少需要选择一天", "Business.APP_MEDICATION_PLAN_WEEKLY_DAYS_REQUIRED"),

        APP_MEDICATION_PLAN_INTERVAL_HOURS_INVALID(12043, "每几小时提醒的间隔必须在1到24之间", "Business.APP_MEDICATION_PLAN_INTERVAL_HOURS_INVALID"),

        APP_MEDICATION_PLAN_INTERVAL_DAYS_INVALID(12060, "每几天提醒的间隔必须在1到365之间", "Business.APP_MEDICATION_PLAN_INTERVAL_DAYS_INVALID"),

        APP_MEDICATION_PLAN_DOSE_RULE_INVALID(12061, "阶段剂量规则不正确，请检查阶段天数、剂量和单位", "Business.APP_MEDICATION_PLAN_DOSE_RULE_INVALID"),

        HEALTH_OPERATION_TASK_TITLE_REQUIRED(12044, "运营任务标题不能为空", "Business.HEALTH_OPERATION_TASK_TITLE_REQUIRED"),

        HEALTH_OPERATION_TASK_TIME_RANGE_INVALID(12045, "运营任务开始时间不能晚于结束时间", "Business.HEALTH_OPERATION_TASK_TIME_RANGE_INVALID"),

        HEALTH_OPERATION_TASK_NOT_FOUND(12046, "首页运营任务不存在", "Business.HEALTH_OPERATION_TASK_NOT_FOUND"),

        HEALTH_DRUG_STOCK_UNIT_REQUIRED(12047, "启用库存跟踪时必须填写库存单位", "Business.HEALTH_DRUG_STOCK_UNIT_REQUIRED"),

        APP_MEDICATION_PLAN_DOSE_UNIT_REQUIRED_FOR_STOCK_TRACKING(12048, "绑定库存药品时必须填写服药剂量单位", "Business.APP_MEDICATION_PLAN_DOSE_UNIT_REQUIRED_FOR_STOCK_TRACKING"),

        APP_MEDICATION_PLAN_DOSE_UNIT_NOT_MATCH_STOCK_UNIT(12049, "服药剂量单位必须与药品库存单位一致", "Business.APP_MEDICATION_PLAN_DOSE_UNIT_NOT_MATCH_STOCK_UNIT"),

        HEALTH_DRUG_IMPORT_ONLY_ALLOW_SYSTEM_DRUG(12050, "仅支持从系统药品引入到个人药柜", "Business.HEALTH_DRUG_IMPORT_ONLY_ALLOW_SYSTEM_DRUG"),

        HEALTH_DRUG_STOCK_UNIT_NOT_MATCH(12051, "库存单位与药品当前库存单位不一致", "Business.HEALTH_DRUG_STOCK_UNIT_NOT_MATCH"),

        HEALTH_DRUG_STOCK_EXPIRE_DATE_REQUIRED(12052, "录入批号效期库存时必须填写效期", "Business.HEALTH_DRUG_STOCK_EXPIRE_DATE_REQUIRED"),

        HEALTH_DRUG_STOCK_BATCH_DETAIL_REQUIRE_QUANTITY(12053, "填写批号或效期时必须同时填写库存数量", "Business.HEALTH_DRUG_STOCK_BATCH_DETAIL_REQUIRE_QUANTITY"),

        HEALTH_DRUG_UNIT_NAME_REQUIRED(12054, "药品单位名称不能为空", "Business.HEALTH_DRUG_UNIT_NAME_REQUIRED"),

        HEALTH_DRUG_UNIT_NAME_IS_NOT_UNIQUE(12055, "药品单位名称已存在", "Business.HEALTH_DRUG_UNIT_NAME_IS_NOT_UNIQUE"),

        HEALTH_DRUG_UNIT_CODE_IS_NOT_UNIQUE(12056, "药品单位编码已存在", "Business.HEALTH_DRUG_UNIT_CODE_IS_NOT_UNIQUE"),

        HEALTH_DRUG_UNIT_DISABLED(12057, "药品单位不存在或已停用", "Business.HEALTH_DRUG_UNIT_DISABLED"),

        HEALTH_ATTACHMENT_TYPE_INVALID(12058, "附件类型不正确", "Business.HEALTH_ATTACHMENT_TYPE_INVALID"),

        HEALTH_DRUG_UNIT_ALREADY_USED(12059, "当前药品单位已被药品引用，暂不允许删除", "Business.HEALTH_DRUG_UNIT_ALREADY_USED"),

        HEALTH_DRUG_STOCK_NOT_ENOUGH(12060, "当前药品库存不足，无法完成本次扣减", "Business.HEALTH_DRUG_STOCK_NOT_ENOUGH"),

        HEALTH_DRUG_TEMPORARY_USE_REQUIRE_STOCK_TRACKING(12061, "当前药品尚未启用库存跟踪，暂时不能登记临时用药", "Business.HEALTH_DRUG_TEMPORARY_USE_REQUIRE_STOCK_TRACKING"),

        MEMBER_FEATURE_CODE_IS_NOT_UNIQUE(12063, "会员权益编码已存在", "Business.MEMBER_FEATURE_CODE_IS_NOT_UNIQUE"),

        MEMBER_LEVEL_CODE_IS_NOT_UNIQUE(12064, "会员等级编码已存在", "Business.MEMBER_LEVEL_CODE_IS_NOT_UNIQUE"),

        MEMBER_LEVEL_DISABLED(12065, "会员等级不存在或已停用", "Business.MEMBER_LEVEL_DISABLED"),

        MEMBER_REDEEM_CODE_NOT_FOUND(12066, "会员兑换码不存在", "Business.MEMBER_REDEEM_CODE_NOT_FOUND"),

        MEMBER_REDEEM_CODE_STATUS_INVALID(12067, "当前兑换码状态不允许执行该操作", "Business.MEMBER_REDEEM_CODE_STATUS_INVALID"),

        MEMBER_ACTIVE_LEVEL_CONFLICT(12068, "当前账号存在其他有效会员等级，暂不支持跨等级叠加", "Business.MEMBER_ACTIVE_LEVEL_CONFLICT"),

        MEMBER_FEATURE_BUILTIN_NOT_ALLOW_DELETE(12069, "内置会员权益不允许删除", "Business.MEMBER_FEATURE_BUILTIN_NOT_ALLOW_DELETE"),

        MEMBER_REDEEM_CODE_GENERATE_COUNT_INVALID(12070, "批量生成兑换码数量必须在1到500之间", "Business.MEMBER_REDEEM_CODE_GENERATE_COUNT_INVALID"),

        MEMBER_SUBSCRIPTION_NOT_FOUND(12071, "会员订阅不存在", "Business.MEMBER_SUBSCRIPTION_NOT_FOUND"),

        MEMBER_SUBSCRIPTION_AUTO_RENEW_NOT_SUPPORTED(12072, "当前会员关系不支持自动续费", "Business.MEMBER_SUBSCRIPTION_AUTO_RENEW_NOT_SUPPORTED"),

        MEMBER_FEATURE_NOT_FOUND(12073, "会员权益不存在", "Business.MEMBER_FEATURE_NOT_FOUND"),

        MEMBER_LEVEL_FEATURE_SAVE_EMPTY(12074, "会员等级权益配置不能为空", "Business.MEMBER_LEVEL_FEATURE_SAVE_EMPTY"),

        MEMBER_ACCESS_DENIED(12075, "当前功能暂不满足会员使用条件：{}", "Business.MEMBER_ACCESS_DENIED"),

        MEMBER_GATE_CODE_IS_NOT_UNIQUE(12076, "会员门禁点编码已存在", "Business.MEMBER_GATE_CODE_IS_NOT_UNIQUE"),

        MEMBER_GATE_NOT_FOUND(12077, "会员门禁点不存在", "Business.MEMBER_GATE_NOT_FOUND"),

        MEMBER_GATE_BUILTIN_NOT_ALLOW_DELETE(12078, "内置会员门禁点不允许删除", "Business.MEMBER_GATE_BUILTIN_NOT_ALLOW_DELETE"),

        MEMBER_GATE_RULE_REQUIRE_FEATURE_OR_LEVEL(12079, "会员门禁规则至少需要配置权益编码或等级白名单", "Business.MEMBER_GATE_RULE_REQUIRE_FEATURE_OR_LEVEL"),

        MEMBER_GATE_ALLOWED_LEVEL_NOT_FOUND(12080, "会员门禁规则里存在未定义的等级编码：{}", "Business.MEMBER_GATE_ALLOWED_LEVEL_NOT_FOUND"),

        APP_CHRONIC_DISEASE_TYPE_NOT_FOUND(12081, "慢病病种不存在或已停用", "Business.APP_CHRONIC_DISEASE_TYPE_NOT_FOUND"),

        APP_CHRONIC_DISEASE_PROFILE_NOT_FOUND(12082, "慢病专项档案不存在", "Business.APP_CHRONIC_DISEASE_PROFILE_NOT_FOUND"),

        APP_CHRONIC_DISEASE_PROFILE_DUPLICATED(12083, "该家庭成员已存在相同慢病专项档案", "Business.APP_CHRONIC_DISEASE_PROFILE_DUPLICATED"),

        APP_CHRONIC_DISEASE_TYPE_CODE_DUPLICATED(12084, "慢病病种编码已存在", "Business.APP_CHRONIC_DISEASE_TYPE_CODE_DUPLICATED"),

        APP_CHRONIC_INDICATOR_TARGET_EMPTY(12085, "请至少填写目标下限、目标上限或目标说明", "Business.APP_CHRONIC_INDICATOR_TARGET_EMPTY"),

        APP_CHRONIC_INDICATOR_TARGET_RANGE_INVALID(12086, "指标目标范围下限不能大于上限", "Business.APP_CHRONIC_INDICATOR_TARGET_RANGE_INVALID"),

        ;


        private final int code;
        private final String msg;

        private final String i18nKey;

        Business(int code, String msg, String i18nKey) {
            Assert.isTrue(code > 10000 && code < 99999,
                "错误码code值定义失败，Business错误码code值范围在10000~99099之间，请查看ErrorCode.Business类，当前错误码码为" + name());

            String errorTypeName = this.getClass().getSimpleName();
            Assert.isTrue(i18nKey != null && i18nKey.startsWith(errorTypeName),
                String.format("错误码i18nKey值定义失败，%s错误码i18nKey值必须以%s开头，当前错误码为%s", errorTypeName, errorTypeName, name()));
            this.code = code;
            this.msg = msg;
            this.i18nKey = i18nKey;
        }

        @Override
        public int code() {
            return this.code;
        }

        @Override
        public String message() {
            return this.msg;
        }

        @Override
        public String i18nKey() {
            return i18nKey;
        }
    }


    /**
     * 1000~9999是外部错误码  比如调用支付失败
     */
    public enum External implements ErrorCodeInterface {

        /**
         * 支付宝调用失败
         */
        FAIL_TO_PAY_ON_ALIPAY(1001, "支付宝调用失败", "External.FAIL_TO_PAY_ON_ALIPAY");


        private final int code;
        private final String msg;

        private final String i18nKey;

        External(int code, String msg, String i18nKey) {
            Assert.isTrue(code > 1000 && code < 9999,
                "错误码code值定义失败，External错误码code值范围在1000~9999之间，请查看ErrorCode.External类，当前错误码码为" + name());

            String errorTypeName = this.getClass().getSimpleName();
            Assert.isTrue(i18nKey != null && i18nKey.startsWith(errorTypeName),
                String.format("错误码i18nKey值定义失败，%s错误码i18nKey值必须以%s开头，当前错误码为%s", errorTypeName, errorTypeName, name()));
            this.code = code;
            this.msg = msg;
            this.i18nKey = i18nKey;
        }

        @Override
        public int code() {
            return this.code;
        }

        @Override
        public String message() {
            return this.msg;
        }

        @Override
        public String i18nKey() {
            return this.i18nKey;
        }


    }


    /**
     * 100~999是客户端错误码
     * 客户端如 Web+小程序+手机端  调用出错
     * 可能由于参数问题或者授权问题或者调用过去频繁
     */
    public enum Client implements ErrorCodeInterface {

        COMMON_FORBIDDEN_TO_CALL(101, "禁止调用", "Client.COMMON_FORBIDDEN_TO_CALL"),

        COMMON_REQUEST_TOO_OFTEN(102, "调用太过频繁", "Client.COMMON_REQUEST_TOO_OFTEN"),

        COMMON_REQUEST_PARAMETERS_INVALID(103, "请求参数异常，{}", "Client.COMMON_REQUEST_PARAMETERS_INVALID"),

        COMMON_REQUEST_METHOD_INVALID(104, "请求方式: {} 不支持", "Client.COMMON_REQUEST_METHOD_INVALID"),

        COMMON_REQUEST_RESUBMIT(105, "请求重复提交", "Client.COMMON_REQUEST_RESUBMIT"),

        COMMON_NO_AUTHORIZATION(106, "请求接口：{} 失败，用户未授权", "Client.COMMON_NO_AUTHORIZATION"),

        INVALID_TOKEN(107, "token异常", "Client.INVALID_TOKEN"),

        TOKEN_PROCESS_FAILED(108, "token处理失败：{}", "Client.TOKEN_PROCESS_FAILED"),

        ;

        private final int code;
        private final String msg;
        private final String i18nKey;

        Client(int code, String msg, String i18nKey) {
            Assert.isTrue(code > 100 && code < 999,
                "错误码code值定义失败，Client错误码code值范围在100~999之间，请查看ErrorCode.Client类，当前错误码码为" + name());

            String errorTypeName = this.getClass().getSimpleName();
            Assert.isTrue(i18nKey != null && i18nKey.startsWith(errorTypeName),
                String.format("错误码i18nKey值定义失败，%s错误码i18nKey值必须以%s开头，当前错误码为%s", errorTypeName, errorTypeName, name()));
            this.code = code;
            this.msg = msg;
            this.i18nKey = i18nKey;
        }

        @Override
        public int code() {
            return this.code;
        }

        @Override
        public String message() {
            return this.msg;
        }

        @Override
        public String i18nKey() {
            return this.i18nKey;
        }

    }


    /**
     * 0~99是内部错误码  例如 框架内部问题之类的
     */
    public enum Internal implements ErrorCodeInterface {
        /**
         * 内部错误码
         */
        INVALID_PARAMETER(1, "参数异常：{}", "Internal.INVALID_PARAMETER"),

        /**
         * 该错误主要用于返回  未知的异常（大部分是RuntimeException） 程序未能捕获 未能预料的错误
         */
        INTERNAL_ERROR(2, "系统内部错误：{}", "Internal.INTERNAL_ERROR"),

        GET_ENUM_FAILED(3, "获取枚举类型失败, 枚举类：{}", "Internal.GET_ENUM_FAILED"),

        GET_CACHE_FAILED(4, "获取缓存失败：{}", "Internal.GET_CACHE_FAILED"),

        DB_INTERNAL_ERROR(5, "数据库异常", "Internal.DB_INTERNAL_ERROR"),

        LOGIN_CAPTCHA_GENERATE_FAIL(7, "验证码生成失败", "Internal.LOGIN_CAPTCHA_GENERATE_FAIL"),

        EXCEL_PROCESS_ERROR(8, "excel处理失败：{}", "Internal.EXCEL_PROCESS_ERROR"),

        ;

        private final int code;
        private final String msg;

        private final String i18nKey;

        Internal(int code, String msg, String i18nKey) {
            Assert.isTrue(code < 100,
                "错误码code值定义失败，Internal错误码code值范围在100~999之间，请查看ErrorCode.Internal类，当前错误码码为" + name());

            String errorTypeName = this.getClass().getSimpleName();
            Assert.isTrue(i18nKey != null && i18nKey.startsWith(errorTypeName),
                String.format("错误码i18nKey值定义失败，%s错误码i18nKey值必须以%s开头，当前错误码为%s", errorTypeName, errorTypeName, name()));
            this.code = code;
            this.msg = msg;
            this.i18nKey = i18nKey;
        }

        @Override
        public int code() {
            return this.code;
        }

        @Override
        public String message() {
            return this.msg;
        }

        @Override
        public String i18nKey() {
            return this.i18nKey;
        }

    }

}



