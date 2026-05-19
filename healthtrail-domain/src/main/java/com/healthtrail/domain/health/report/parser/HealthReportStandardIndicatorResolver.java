package com.healthtrail.domain.health.report.parser;

import cn.hutool.core.util.StrUtil;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * 标准指标编码解析器。
 *
 * <p>不同医院会用不同名称描述同一个指标。这里先用内置常见规则兜底，
 * 后续可以替换为数据库字典或医学知识库。
 */
@Component
public class HealthReportStandardIndicatorResolver {

    private static final Map<String, String> KEYWORD_CODE_MAP = new LinkedHashMap<>();

    static {
        KEYWORD_CODE_MAP.put("空腹血糖", "GLU");
        KEYWORD_CODE_MAP.put("葡萄糖", "GLU");
        KEYWORD_CODE_MAP.put("血糖", "GLU");
        KEYWORD_CODE_MAP.put("glu", "GLU");
        KEYWORD_CODE_MAP.put("尿酸", "UA");
        KEYWORD_CODE_MAP.put("ua", "UA");
        KEYWORD_CODE_MAP.put("总胆固醇", "TC");
        KEYWORD_CODE_MAP.put("胆固醇", "TC");
        KEYWORD_CODE_MAP.put("甘油三酯", "TG");
        KEYWORD_CODE_MAP.put("低密度脂蛋白", "LDL_C");
        KEYWORD_CODE_MAP.put("高密度脂蛋白", "HDL_C");
        KEYWORD_CODE_MAP.put("白细胞", "WBC");
        KEYWORD_CODE_MAP.put("红细胞", "RBC");
        KEYWORD_CODE_MAP.put("血红蛋白", "HGB");
        KEYWORD_CODE_MAP.put("血小板", "PLT");
        KEYWORD_CODE_MAP.put("丙氨酸氨基转移酶", "ALT");
        KEYWORD_CODE_MAP.put("谷丙转氨酶", "ALT");
        KEYWORD_CODE_MAP.put("天门冬氨酸氨基转移酶", "AST");
        KEYWORD_CODE_MAP.put("谷草转氨酶", "AST");
        KEYWORD_CODE_MAP.put("肌酐", "CREA");
        KEYWORD_CODE_MAP.put("尿素", "UREA");
    }

    public String resolve(String itemCode, String itemName) {
        if (StrUtil.isNotBlank(itemCode)) {
            String normalizedCode = itemCode.trim().toUpperCase(Locale.ROOT);
            if (normalizedCode.length() <= 64) {
                return normalizedCode;
            }
        }
        String normalizedName = StrUtil.blankToDefault(itemName, "").trim().toLowerCase(Locale.ROOT);
        for (Map.Entry<String, String> entry : KEYWORD_CODE_MAP.entrySet()) {
            if (normalizedName.contains(entry.getKey().toLowerCase(Locale.ROOT))) {
                return entry.getValue();
            }
        }
        if (StrUtil.isBlank(itemName)) {
            return null;
        }
        return "CUSTOM_" + Integer.toHexString(itemName.trim().hashCode()).toUpperCase(Locale.ROOT);
    }
}
