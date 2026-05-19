package com.healthtrail.domain.health.drug.dto;

import java.util.List;
import lombok.Data;

/**
 * 药品批量导入结果。
 *
 * <p>批量导入并不适合“一旦有一条失败就整体报错”，
 * 因为用户更需要知道：
 * 1. 本次识别到了多少条有效输入
 * 2. 实际成功导入了多少条
 * 3. 哪些药名因为重复或格式问题被跳过
 *
 * <p>因此这里采用结果汇总 DTO，把本次导入结果一次性返回给前端。
 */
@Data
public class DrugImportResultDTO {

    /**
     * 本次请求中识别出的有效导入行数。
     */
    private Integer requestedCount;

    /**
     * 实际成功导入数量。
     */
    private Integer importedCount;

    /**
     * 跳过数量。
     */
    private Integer skippedCount;

    /**
     * 其中因重名被跳过的数量。
     */
    private Integer duplicateCount;

    /**
     * 本次成功导入的药品快照。
     *
     * <p>前端可直接拿来刷新列表、弹导入结果摘要，
     * 不需要再马上额外请求详情接口。
     */
    private List<DrugDTO> importedDrugs;

    /**
     * 被跳过的药名集合。
     *
     * <p>当前这里主要承载“重复药名”与“缺少药名”的提示结果。
     */
    private List<String> skippedDrugNames;
}
