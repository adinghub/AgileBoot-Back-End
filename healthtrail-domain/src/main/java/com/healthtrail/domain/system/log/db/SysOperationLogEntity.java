package com.healthtrail.domain.system.log.db;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.io.Serializable;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;

/**
 * 系统操作日志记录表，记录管理员的操作行为及请求参数
 *
 * @author valarchie
 * @since 2022-10-02
 */
@Getter
@Setter
@TableName("sys_operation_log")
@ApiModel(value = "SysOperationLogEntity对象", description = "操作日志记录")
public class SysOperationLogEntity extends Model<SysOperationLogEntity> {

    private static final long serialVersionUID = 1L;

    /** 操作日志主键ID */
    @ApiModelProperty("日志主键")
    @TableId(value = "operation_id", type = IdType.AUTO)
    private Long operationId;

    /** 业务操作类型：0其它 1新增 2修改 3删除 */
    @ApiModelProperty("业务类型（0其它 1新增 2修改 3删除）")
    @TableField("business_type")
    private Integer businessType;

    /** HTTP请求方式，如GET、POST */
    @ApiModelProperty("请求方式")
    @TableField("request_method")
    private Integer requestMethod;

    /** 请求所属的业务模块 */
    @ApiModelProperty("请求模块")
    @TableField("request_module")
    private String requestModule;

    /** 请求的URL地址 */
    @ApiModelProperty("请求URL")
    @TableField("request_url")
    private String requestUrl;

    /** 被调用的Java方法名 */
    @ApiModelProperty("调用方法")
    @TableField("called_method")
    private String calledMethod;

    /** 操作人类别：0其它 1后台用户 2手机端用户 */
    @ApiModelProperty("操作类别（0其它 1后台用户 2手机端用户）")
    @TableField("operator_type")
    private Integer operatorType;

    /** 操作人的用户ID */
    @ApiModelProperty("用户ID")
    @TableField("user_id")
    private Long userId;

    /** 操作人用户名 */
    @ApiModelProperty("操作人员")
    @TableField("username")
    private String username;

    /** 操作人的IP地址 */
    @ApiModelProperty("操作人员ip")
    @TableField("operator_ip")
    private String operatorIp;

    /** 操作人的地理位置 */
    @ApiModelProperty("操作地点")
    @TableField("operator_location")
    private String operatorLocation;

    /** 操作人所属部门ID */
    @ApiModelProperty("部门ID")
    @TableField("dept_id")
    private Long deptId;

    /** 操作人所属部门名称 */
    @ApiModelProperty("部门名称")
    @TableField("dept_name")
    private String deptName;

    /** 请求的入参JSON字符串 */
    @ApiModelProperty("请求参数")
    @TableField("operation_param")
    private String operationParam;

    /** 请求的返回结果JSON字符串 */
    @ApiModelProperty("返回参数")
    @TableField("operation_result")
    private String operationResult;

    /** 操作执行状态，1正常 0异常 */
    @ApiModelProperty("操作状态（1正常 0异常）")
    @TableField("status")
    private Integer status;

    /** 异常时的错误堆栈信息 */
    @ApiModelProperty("错误消息")
    @TableField("error_stack")
    private String errorStack;

    /** 操作发生时间 */
    @ApiModelProperty("操作时间")
    @TableField("operation_time")
    private Date operationTime;

    /** 逻辑删除标记，0未删除 1已删除 */
    @ApiModelProperty("逻辑删除")
    @TableField("deleted")
    @TableLogic(value = "0", delval = "1")
    private Integer deleted;


    @Override
    public Serializable pkVal() {
        return this.operationId;
    }

}
