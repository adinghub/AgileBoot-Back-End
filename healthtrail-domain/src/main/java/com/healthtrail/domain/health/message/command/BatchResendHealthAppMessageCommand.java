package com.healthtrail.domain.health.message.command;

import java.util.List;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.Data;

/**
 * 后台批量重发消息命令。
 *
 * <p>后台批量重发的主要使用场景通常是：
 * 1. 在列表页勾选多条失败消息后统一补发
 * 2. 对某个用户最近多条未成功消息做一次集中补救
 *
 * <p>这里采用“显式传消息ID列表”的方式，而不是直接按查询条件批量执行，
 * 是为了让后台每次操作都更可控，避免误把不该重发的消息一并带上。
 */
@Data
public class BatchResendHealthAppMessageCommand {

    /**
     * 需要批量重发的消息ID列表。
     *
     * <p>这里限制最大 100 条，是为了避免：
     * 1. 单次请求时间过长
     * 2. 一次性对同一批用户触发过多补发
     * 3. 后台误操作时放大影响范围
     */
    @NotEmpty(message = "消息ID列表不能为空")
    @Size(max = 100, message = "单次最多重发100条消息")
    private List<@NotNull(message = "消息ID不能为空") Long> messageIds;
}
