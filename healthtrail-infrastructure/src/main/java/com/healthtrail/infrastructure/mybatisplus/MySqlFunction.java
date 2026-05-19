package com.healthtrail.infrastructure.mybatisplus;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;

import java.util.List;

/**
 * 由于 H2 不支持大部分 MySQL 函数，所以测试环境需要在初始化脚本中补充 Java 别名函数。
 * 当前仓库对应的 H2 初始化脚本位于 `h2sql/healthtrail_schema.sql`，其中应配置：
 * `CREATE ALIAS FIND_IN_SET FOR "com.healthtrail.infrastructure.mybatisplus.MySqlFunction.findInSet";`
 *
 * @author valarchie
 */
public class MySqlFunction {

    private MySqlFunction() {
    }

    public static boolean findInSet(String target, String setString) {
        if (setString == null) {
            return false;
        }

        List<String> split = StrUtil.split(setString, ",");

        return CollUtil.contains(split, target);
    }

}
