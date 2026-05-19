package com.healthtrail.domain.health.attachment;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * 附件类型常量。
 *
 * <p>当前先围绕药品能力收口两类附件：
 * 1. 药品图片
 * 2. 药品单位图标
 *
 * <p>后续如果有更多业务需要复用统一附件表，
 * 只要在这里继续扩展类型常量和允许列表即可。
 */
public final class AttachmentTypeConstants {

    /**
     * 药品图片。
     */
    public static final String DRUG_IMAGE = "DRUG_IMAGE";

    /**
     * 药品单位图标。
     */
    public static final String DRUG_UNIT_ICON = "DRUG_UNIT_ICON";

    /**
     * 当前允许写入统一附件表的类型集合。
     */
    private static final Set<String> SUPPORTED_TYPES = new HashSet<>(Arrays.asList(
        DRUG_IMAGE,
        DRUG_UNIT_ICON
    ));

    private AttachmentTypeConstants() {
    }

    /**
     * 判断附件类型是否受支持。
     *
     * @param attachmentType 附件类型
     * @return true 表示当前实现支持该类型
     */
    public static boolean isSupported(String attachmentType) {
        return SUPPORTED_TYPES.contains(attachmentType);
    }
}
