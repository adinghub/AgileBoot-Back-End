package com.healthtrail.domain.system.dept.dto;

import com.healthtrail.common.enums.common.StatusEnum;
import com.healthtrail.common.enums.BasicEnumUtil;
import com.healthtrail.domain.system.dept.db.SysDeptEntity;
import java.util.Date;
import lombok.Data;

/**
 * 部门返回对象
 * @author valarchie
 */
@Data
public class DeptDTO {

    public DeptDTO(SysDeptEntity entity) {
        if (entity != null) {
            this.id = entity.getDeptId();
            this.parentId = entity.getParentId();
            this.deptName = entity.getDeptName();
            this.orderNum = entity.getOrderNum();
            this.leaderName = entity.getLeaderName();
            this.email = entity.getEmail();
            this.phone = entity.getPhone();
            this.status = entity.getStatus();
            this.createTime = entity.getCreateTime();
            this.statusStr = BasicEnumUtil.getDescriptionByValue(StatusEnum.class, entity.getStatus());
        }
    }

    /** 部门ID */
    private Long id;
    /** 父部门ID */
    private Long parentId;
    /** 部门名称 */
    private String deptName;
    /** 显示顺序 */
    private Integer orderNum;
    /** 负责人 */
    private String leaderName;
    /** 联系电话 */
    private String phone;
    /** 邮箱 */
    private String email;
    /** 状态 */
    private Integer status;
    /** 状态文字 */
    private String statusStr;
    /** 创建时间 */
    private Date createTime;

}
