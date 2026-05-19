package com.healthtrail.api.config;

import com.healthtrail.api.HealthTrailApiApplication;
import javax.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

/**
 * 验证 API 启动入口在 test 内存模式下的 H2 初始化链路。
 *
 * <p>这条测试专门覆盖本次线上前置问题：
 * API 模块虽然激活了 `basic,test`，但因为缺少自身的 `application-test.yml`，
 * 导致 dynamic-datasource 没有真正装配出主数据源，应用启动后定时任务第一次查库就会报错。
 *
 * <p>这里不去校验复杂业务流程，只守住三个最关键的基础事实：
 * 1. API 模块自己能够在 `basic,test` 下独立拉起 Spring 上下文。
 * 2. H2 兼容脚本已经被成功执行，基础库表和健康业务表都已存在。
 * 3. 提醒定时任务依赖的核心表能够被访问，后续到点调度不会再因为“主数据源不存在”直接失败。
 */
@SpringBootTest(
    classes = HealthTrailApiApplication.class,
    webEnvironment = SpringBootTest.WebEnvironment.NONE,
    properties = {
        // 测试目标是验证 H2 初始化链路，而不是验证定时任务调度时机。
        // 这里把 cron 改成极低触发频率，避免测试执行窗口内刚好命中调度，影响断言稳定性。
        "health.medication.reminder.schedule.expire-cron=0 0 0 1 1 ?",
        "health.medication.reminder.schedule.generate-cron=0 0 0 1 1 ?",
        "health.medication.reminder.schedule.dispatch-cron=0 0 0 1 1 ?"
    }
)
@ActiveProfiles({"basic", "test"})
class ApiTestProfileH2InitializationTest {

    @Resource
    private JdbcTemplate jdbcTemplate;

    @Test
    void shouldLoadBaseAndMedicationSchemaWhenApiRunsInTestProfile() {
        // 先验证后台基础表，确保 API 启动入口没有因为 profile 资源缺失而跳过基础脚本。
        assertTableExists("sys_user");
        assertTableExists("sys_menu");

        // 再验证健康域与提醒调度直接依赖的核心表，避免应用虽然能启动，但一到定时任务查库就出错。
        assertTableExists("attachment");
        assertTableExists("drug_unit");
        assertTableExists("drug");
        assertTableExists("medication_plan");
        assertTableExists("medication_reminder");

        // 最后补一个最小种子数据断言，确保不是“空表也算初始化成功”的假通过。
        assertRowCountAtLeast("sys_user", 1);
        assertRowCountAtLeast("drug_unit", 8);
        assertRowCountAtLeast("drug", 1);
    }

    /**
     * 通过 information_schema 校验表存在性。
     *
     * <p>这里强制限定 H2 默认的 `PUBLIC` schema，
     * 是为了确保 API 的 test 内存模式与“去掉 app schema 后”的正式 PostgreSQL 落点一致，
     * 防止出现“表建出来了，但不在业务实际访问位置”的假成功。
     */
    private void assertTableExists(String tableName) {
        Integer tableCount = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) " +
                "FROM information_schema.tables " +
                "WHERE upper(table_schema) = 'PUBLIC' AND upper(table_name) = upper(?)",
            Integer.class,
            tableName
        );
        Assertions.assertNotNull(tableCount, "查询表元数据时不应返回空结果");
        Assertions.assertTrue(tableCount > 0, "缺少初始化表: " + tableName);
    }

    /**
     * 校验表中至少存在若干条记录。
     *
     * <p>这里故意不用“精确等于”，因为种子数据后续还可能继续扩充。
     * 我们真正想防住的是“脚本根本没执行”或“执行顺序错乱导致初始化为空”的问题。
     */
    private void assertRowCountAtLeast(String tableName, long expectedMinimum) {
        Long actualCount = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM " + tableName,
            Long.class
        );
        Assertions.assertNotNull(actualCount, "统计表数据量时不应返回空结果");
        Assertions.assertTrue(
            actualCount >= expectedMinimum,
            "表 " + tableName + " 的记录数不足，期望至少 " + expectedMinimum + "，实际为 " + actualCount
        );
    }
}
