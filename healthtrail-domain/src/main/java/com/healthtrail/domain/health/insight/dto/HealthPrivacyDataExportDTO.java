package com.healthtrail.domain.health.insight.dto;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.Data;

/**
 * 健康长期管理隐私数据导出对象。
 *
 * <p>二期先提供结构化数据包，不直接生成文件。App 可以把它转成复制文本、系统分享内容，
 * 或后续本地渲染为文件。服务端只组装当前账号有权访问的数据，避免端上自己跨模块拼装造成权限口径不一致。
 */
@Data
public class HealthPrivacyDataExportDTO {

    /** 生成时间 */
    private Date generatedTime;

    /** 导出标题 */
    private String title;

    /** 导出摘要 */
    private String summary;

    /** 数据分区列表 */
    private List<DataSectionDTO> sections = new ArrayList<>();

    /** 提示列表 */
    private List<String> tips = new ArrayList<>();

    @Data
    public static class DataSectionDTO {
        /** 分类编码 */
        private String categoryCode;
        /** 分类名称 */
        private String categoryName;
        /** 数据项数量 */
        private int itemCount;
        /** 明细数据列表 */
        private List<Map<String, Object>> items = new ArrayList<>();
    }

    public static DataSectionDTO section(String categoryCode, String categoryName, List<Map<String, Object>> items) {
        DataSectionDTO dto = new DataSectionDTO();
        dto.setCategoryCode(categoryCode);
        dto.setCategoryName(categoryName);
        dto.setItems(items == null ? new ArrayList<>() : items);
        dto.setItemCount(dto.getItems().size());
        return dto;
    }

    public static Map<String, Object> item() {
        return new LinkedHashMap<>();
    }
}
