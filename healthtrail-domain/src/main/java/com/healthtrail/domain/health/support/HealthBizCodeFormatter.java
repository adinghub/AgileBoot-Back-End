package com.healthtrail.domain.health.support;

/**
 * 健康业务编码格式化工具。
 *
 * <p>本次“人员 / 药品 / 计划”补编码采用固定规则：
 * 1. 使用稳定英文前缀区分业务类型
 * 2. 直接复用已经落库的主键值做直接拼接，不补零
 * 3. 不额外引入号段表，避免迁移、回填和运维复杂度显著上升
 *
 * <p>这样设计的好处是：
 * 1. 历史数据可以按主键一次性回填
 * 2. 新增流程只需要“先插入拿到主键，再反写编码”即可完成
 * 3. 编码天然可追溯，不会出现同一条记录换环境后编码含义不明的问题
 */
public final class HealthBizCodeFormatter {

    private static final String MEMBER_PREFIX = "MBR";

    private static final String DRUG_PREFIX = "DRG";

    private static final String PLAN_PREFIX = "PLN";

    private HealthBizCodeFormatter() {
    }

    /**
     * 格式化家庭成员编码。
     */
    public static String formatMemberCode(Long memberId) {
        return format(MEMBER_PREFIX, memberId);
    }

    /**
     * 格式化药品编码。
     */
    public static String formatDrugCode(Long drugId) {
        return format(DRUG_PREFIX, drugId);
    }

    /**
     * 格式化用药计划编码。
     */
    public static String formatPlanCode(Long planId) {
        return format(PLAN_PREFIX, planId);
    }

    /**
     * 根据业务前缀和主键生成稳定编码。
     *
     * <p>如果主键尚未生成，则直接返回 null，
     * 由调用方决定是在插入后补写，还是在查询时做兜底格式化。
     */
    private static String format(String prefix, Long id) {
        if (id == null) {
            return null;
        }
        return prefix + id;
    }
}
