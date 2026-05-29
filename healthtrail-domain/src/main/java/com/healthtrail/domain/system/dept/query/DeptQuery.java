package com.healthtrail.domain.system.dept.query;

import com.healthtrail.common.core.page.AbstractQuery;
import com.healthtrail.domain.system.dept.db.SysDeptEntity;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 部门查询对象
 * @author valarchie
 */
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
public class DeptQuery extends AbstractQuery<SysDeptEntity> {

    /** 部门ID */
    private Long deptId;

    /** 父部门ID */
    private Long parentId;


    @Override
    public QueryWrapper<SysDeptEntity> addQueryCondition() {
        // TODO parentId 这个似乎没使用
        return new QueryWrapper<SysDeptEntity>()
//            .eq(status != null, "status", status)
            .eq(parentId != null, "parent_id", parentId);
//            .like(StrUtil.isNotEmpty(deptName), "dept_name", deptName);
//            .and(deptId != null && isExcludeCurrentDept, o ->
//                o.ne("dept_id", deptId)
//                    .or()
//            );
    }
}
