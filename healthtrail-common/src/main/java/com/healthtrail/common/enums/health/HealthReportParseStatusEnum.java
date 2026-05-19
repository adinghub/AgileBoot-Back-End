package com.healthtrail.common.enums.health;

import com.healthtrail.common.enums.DictionaryEnum;
import com.healthtrail.common.enums.dictionary.CssTag;
import com.healthtrail.common.enums.dictionary.Dictionary;

/**
 * 体检报告解析状态枚举。
 *
 * <p>当前阶段虽然先只做“上传与管理”，但报告上传完成后天然会进入后续解析流程，
 * 因此这里提前把解析状态统一定义出来，避免以后在表里散落各种字符串状态。
 */
@Dictionary(name = "health.reportParseStatus")
public enum HealthReportParseStatusEnum implements DictionaryEnum<Integer> {

    /**
     * 文件已上传，但尚未进入解析。
     */
    WAIT_PARSE(0, "待解析", CssTag.WARNING),

    /**
     * 正在解析中。
     */
    PROCESSING(1, "解析中", CssTag.PRIMARY),

    /**
     * 解析完成。
     */
    COMPLETED(2, "已解析", CssTag.SUCCESS),

    /**
     * 解析失败。
     */
    FAILED(3, "解析失败", CssTag.DANGER);

    private final int value;
    private final String description;
    private final String cssTag;

    HealthReportParseStatusEnum(int value, String description, String cssTag) {
        this.value = value;
        this.description = description;
        this.cssTag = cssTag;
    }

    @Override
    public Integer getValue() {
        return value;
    }

    @Override
    public String description() {
        return description;
    }

    @Override
    public String cssTag() {
        return cssTag;
    }
}
