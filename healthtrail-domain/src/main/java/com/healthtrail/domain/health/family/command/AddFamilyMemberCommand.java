package com.healthtrail.domain.health.family.command;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.math.BigDecimal;
import java.util.Date;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.Data;

/**
 * 新增家庭成员命令对象。
 *
 * <p>当前阶段先聚焦健康系统的核心基础信息，
 * 让家庭成员能承接后续的用药提醒、体检报告、健康档案等业务。
 */
@Data
public class AddFamilyMemberCommand {

    /**
     * 成员姓名。
     */
    @NotBlank(message = "成员姓名不能为空")
    @Size(max = 30, message = "成员姓名长度不能超过30个字符")
    private String memberName;

    /**
     * 性别。
     * 建议复用系统现有 GenderEnum 定义：0未知 1男 2女。
     */
    @NotNull(message = "性别不能为空")
    private Integer gender;

    /**
     * 生日。
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date birthday;

    /**
     * 与当前 App 账户的关系，例如本人、父亲、母亲、孩子。
     * 一期先用字符串存储，便于快速扩展和前端自由展示。
     */
    @NotBlank(message = "关系不能为空")
    @Size(max = 20, message = "关系长度不能超过20个字符")
    private String relationType;

    /**
     * 身高，单位 cm。
     */
    private BigDecimal height;

    /**
     * 体重，单位 kg。
     */
    private BigDecimal weight;

    /**
     * 血型。
     */
    @Size(max = 10, message = "血型长度不能超过10个字符")
    private String bloodType;

    /**
     * 过敏史。
     */
    @Size(max = 500, message = "过敏史长度不能超过500个字符")
    private String allergyHistory;

    /**
     * 慢病史。
     */
    @Size(max = 500, message = "慢病史长度不能超过500个字符")
    private String chronicHistory;

    /**
     * 备注。
     */
    @Size(max = 500, message = "备注长度不能超过500个字符")
    private String remark;

    /**
     * 状态，默认建议传 1。
     */
    private Integer status;
}
