package com.healthtrail.domain.health.report.command;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Date;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * 新增体检报告命令对象。
 *
 * <p>该命令只承接报告元信息，不直接承接文件对象。
 * 文件上传由控制器在 multipart 请求中单独接收，再交给应用服务统一处理。
 */
@Data
public class AddHealthReportCommand {

    /**
     * 绑定的家庭成员ID。
     * 体检报告必须归属到某个成员，后续异常分析和提醒联动都依赖这个维度。
     */
    @NotNull(message = "家庭成员不能为空")
    private Long memberId;

    /**
     * 报告名称。
     * 如果前端不传，后端会退回使用原始文件名生成默认标题。
     */
    @Size(max = 100, message = "报告名称长度不能超过100个字符")
    private String reportName;

    /**
     * 报告类型，例如体检报告、化验单、检查单。
     */
    @Size(max = 50, message = "报告类型长度不能超过50个字符")
    private String reportType;

    /**
     * 医院或机构名称。
     */
    @Size(max = 100, message = "医院名称长度不能超过100个字符")
    private String hospitalName;

    /**
     * 报告日期。
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date reportDate;

    /**
     * 备注。
     */
    @Size(max = 500, message = "备注长度不能超过500个字符")
    private String remark;
}
