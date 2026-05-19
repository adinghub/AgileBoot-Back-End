package com.healthtrail.domain.system.member.command;

import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import lombok.Data;

/**
 * 保存会员等级权益矩阵命令。
 */
@Data
public class SaveMemberLevelFeatureCommand {

    @NotNull(message = "会员等级ID不能为空")
    private Long memberLevelId;

    @Valid
    @NotNull(message = "权益配置列表不能为空")
    private List<Item> items;

    @Data
    public static class Item {
        @NotNull(message = "会员权益ID不能为空")
        private Long memberFeatureId;
        private Integer enabled;
        private Integer limitValue;
        private String remark;
    }
}
