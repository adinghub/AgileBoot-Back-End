package com.healthtrail.admin.config;

import com.healthtrail.admin.HealthTrailAdminApplication;
import javax.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * 验证 test 模式下的 H2 初始化链路是否可用。
 *
 * <p>这组断言专门覆盖两类风险：
 * 1. PostgreSQL 基线脚本调整后，H2 兼容脚本没有同步更新，导致 test 模式再次启动失败。
 * 2. 健康业务脚本虽然存在，但没有真正被 test Profile 加载，导致运行时出现“缺表”问题。
 *
 * <p>测试不关注具体业务流程，只验证最关键的基础事实：
 * 1. Spring 上下文能够在 basic + test Profile 下正常启动。
 * 2. 基础库表和健康业务表都已经完成初始化。
 * 3. 种子数据已经被写入，后续依赖默认数据的测试可以继续运行。
 */
@SpringBootTest(
    classes = HealthTrailAdminApplication.class,
    webEnvironment = SpringBootTest.WebEnvironment.NONE
)
@ActiveProfiles({"basic", "test"})
@RunWith(SpringRunner.class)
class TestProfileH2InitializationTest {

    @Resource
    private JdbcTemplate jdbcTemplate;

    @Test
    void shouldLoadBaseAndHealthSchemaInTestProfile() {
        // 验证后台基础表已经建好，避免基础初始化脚本回归失效。
        assertTableExists("sys_user");
        assertTableExists("sys_menu");

        // 验证健康业务表也已经建好，避免 test 环境只初始化后台基础表，遗漏健康域对象。
        assertTableExists("attachment");
        assertTableExists("drug_unit");
        assertTableExists("drug");
        assertTableExists("medication_plan");
        assertTableExists("report");
        assertTableExists("indicator_template");

        // 校验基础种子数据存在，确保依赖默认管理员和菜单的测试场景可以直接复用。
        assertRowCountAtLeast("sys_user", 3);
        assertRowCountAtLeast("sys_menu", 60);

        // 校验健康模块种子数据存在，确保系统常用药等默认业务数据已被正确装载。
        assertRowCountAtLeast("drug_unit", 8);
        assertRowCountAtLeast("drug", 2);
    }

    /**
     * 通过 information_schema 校验表是否存在。
     *
     * <p>这里显式限定 H2 默认的 `PUBLIC` schema，
     * 是为了确保测试和“去掉 app schema 后”的 PostgreSQL 默认落点保持一致，
     * 避免出现“表虽然存在，但实际落在错误 schema”里的假通过。
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
     * 校验表中的最小记录数。
     *
     * <p>这里不对精确数量做强绑定，而是只验证“至少有多少条”，
     * 这样后续如果继续补充默认数据，不会让这条回归测试变得脆弱。
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
