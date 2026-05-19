package com.healthtrail.domain.common.dto;

import cn.hutool.core.lang.tree.Tree;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 树形选中数据
 * @author valarchie
 */
@Data
@NoArgsConstructor
public class TreeSelectedDTO {

    /** 选中的节点ID列表 */
    private List<Long> checkedKeys;
    /** 菜单树 */
    private List<Tree<Long>> menus;
    /** 部门树 */
    private List<Tree<Long>> depts;

}
