package com.healthtrail.common.core.base;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.annotations.ApiModelProperty;
import java.util.Date;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Entity基类
 *
 * @author valarchie
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class BaseEntity<T extends Model<?>> extends Model<T> {

    @ApiModelProperty("创建者ID")
    @TableField(value = "creator_id", fill = FieldFill.INSERT)
    private Long creatorId;

    @ApiModelProperty("创建时间")
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private Date createTime;

    @ApiModelProperty("更新者ID")
    @TableField(value = "updater_id", fill = FieldFill.UPDATE, updateStrategy = FieldStrategy.NOT_NULL)
    private Long updaterId;

    @ApiModelProperty("更新时间")
    @TableField(value = "update_time", fill = FieldFill.UPDATE)
    private Date updateTime;

    /**
     * 逻辑删除标志。
     *
     * <p>当前项目数据库里的 `deleted` 字段在 PostgreSQL / MySQL 中统一按数值型存储：
     * 1. `0` 代表未删除；
     * 2. `1` 代表已删除。
     *
     * <p>这里显式使用 Integer，而不是 Boolean，原因是：
     * 1. PostgreSQL 不会把 Java Boolean 自动兼容成 smallint；
     * 2. 继续使用 Boolean 会在 insert/update 时触发类型不匹配；
     * 3. 用 `@TableLogic(value = "0", delval = "1")` 后，MyBatis-Plus 仍然能正常处理逻辑删除。
     */
    @ApiModelProperty("删除标志（0代表存在 1代表删除）")
    @TableField("deleted")
    @TableLogic(value = "0", delval = "1")
    private Integer deleted;

}
