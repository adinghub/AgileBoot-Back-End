package com.healthtrail.api.controller.app;

import com.healthtrail.common.core.base.BaseController;
import com.healthtrail.common.core.dto.ResponseDTO;
import com.healthtrail.common.core.page.PageDTO;
import com.healthtrail.domain.health.drug.DrugApplicationService;
import com.healthtrail.domain.health.drug.command.AddDrugCommand;
import com.healthtrail.domain.health.drug.command.ImportSystemDrugCommand;
import com.healthtrail.domain.health.drug.command.ImportDrugCommand;
import com.healthtrail.domain.health.drug.command.IncreaseDrugStockCommand;
import com.healthtrail.domain.health.drug.command.TemporaryUseDrugStockCommand;
import com.healthtrail.domain.health.drug.command.UpdateDrugStockConfigCommand;
import com.healthtrail.domain.health.drug.command.UpdateDrugCommand;
import com.healthtrail.domain.health.drug.dto.DrugDTO;
import com.healthtrail.domain.health.drug.dto.DrugImportResultDTO;
import com.healthtrail.domain.health.drug.dto.DrugStockLogDTO;
import java.util.List;
import com.healthtrail.domain.health.drug.query.DrugQuery;
import com.healthtrail.infrastructure.user.AuthenticationUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * App 端药品控制器。
 *
 * <p>App 端药品列表需要同时看到两类数据：
 * 1. 后台统一下发的系统药品
 * 2. 当前用户自己维护的个人药品
 *
 * <p>但真正允许编辑、删除的只有当前用户自己的药品，系统药品对 App 端仅开放查询和选用。
 */
@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/app/drugs")
@Tag(name = "药品API", description = "App端系统药品与个人药品接口")
public class DrugController extends BaseController {

    private final DrugApplicationService drugApplicationService;

    /**
     * 分页查询当前用户可见的药品列表。
     * 这里会主动追加系统药品，确保创建用药计划时用户可以直接选用后台预置药品。
     */
    @Operation(summary = "药品列表")
    @GetMapping
    public ResponseDTO<PageDTO<DrugDTO>> list(DrugQuery query) {
        query.setOwnerUserId(AuthenticationUtils.getAppLoginUser().getUserId());
        query.setIncludeSystemDrugs(true);
        return ResponseDTO.ok(drugApplicationService.getDrugList(query));
    }

    /**
     * 查询药品详情。
     * 详情允许查看系统药品和当前用户自己的药品，但不会放开对系统药品的编辑权限。
     */
    @Operation(summary = "药品详情")
    @GetMapping("/{drugId}")
    public ResponseDTO<DrugDTO> getInfo(@PathVariable Long drugId) {
        Long currentUserId = AuthenticationUtils.getAppLoginUser().getUserId();
        return ResponseDTO.ok(drugApplicationService.getDrugInfo(drugId, currentUserId));
    }

    /**
     * 查询药品库存流水。
     *
     * <p>药品详情页现在会把最近库存变更直接展示出来，
     * 这里返回的是当前用户这条个人药品的完整库存流水时间线。
     */
    @Operation(summary = "药品库存流水")
    @GetMapping("/{drugId}/stock/logs")
    public ResponseDTO<List<DrugStockLogDTO>> getStockLogs(@PathVariable Long drugId) {
        Long currentUserId = AuthenticationUtils.getAppLoginUser().getUserId();
        return ResponseDTO.ok(drugApplicationService.getDrugStockLogs(drugId, currentUserId));
    }

    /**
     * 新增药品。
     */
    @Operation(summary = "新增药品")
    @PostMapping
    public ResponseDTO<Void> add(@Valid @RequestBody AddDrugCommand addCommand) {
        Long currentUserId = AuthenticationUtils.getAppLoginUser().getUserId();
        drugApplicationService.addDrug(addCommand, currentUserId);
        return ResponseDTO.ok();
    }

    /**
     * 批量导入个人药品。
     *
     * <p>该接口定位为“文本批量导入”，
     * 适合 App 用户从外部清单或聊天记录中一次性粘贴多行药品信息。
     */
    @Operation(summary = "批量导入个人药品")
    @PostMapping("/import")
    public ResponseDTO<DrugImportResultDTO> importDrugs(@Valid @RequestBody ImportDrugCommand importCommand) {
        Long currentUserId = AuthenticationUtils.getAppLoginUser().getUserId();
        return ResponseDTO.ok(drugApplicationService.importDrugs(importCommand, currentUserId));
    }

    /**
     * 把系统药品引入到当前用户的个人药柜。
     *
     * <p>引入后会生成一条真正归属于当前用户的个人药品记录，
     * 后续库存维护、用药自动扣减都围绕这条个人数据进行。
     */
    @Operation(summary = "引入系统药品到个人药柜")
    @PostMapping("/{drugId}/import")
    public ResponseDTO<DrugDTO> importSystemDrug(@PathVariable Long drugId,
        @Valid @RequestBody(required = false) ImportSystemDrugCommand importCommand) {
        Long currentUserId = AuthenticationUtils.getAppLoginUser().getUserId();
        return ResponseDTO.ok(drugApplicationService.importSystemDrug(drugId, importCommand, currentUserId));
    }

    /**
     * 给个人药品增加库存。
     */
    @Operation(summary = "个人药品补库存")
    @PostMapping("/{drugId}/stock/increase")
    public ResponseDTO<DrugDTO> increaseStock(@PathVariable Long drugId,
        @Valid @RequestBody IncreaseDrugStockCommand increaseCommand) {
        Long currentUserId = AuthenticationUtils.getAppLoginUser().getUserId();
        return ResponseDTO.ok(drugApplicationService.increaseDrugStock(drugId, increaseCommand, currentUserId));
    }

    /**
     * 登记一次临时用药并扣减库存。
     *
     * <p>该接口用于“没有建立用药计划，但这次确实吃了药”的场景：
     * 1. 不创建计划
     * 2. 不创建提醒
     * 3. 直接写一条临时用药记录
     * 4. 同步扣减个人药柜库存
     */
    @Operation(summary = "临时用药扣库存")
    @PostMapping("/{drugId}/stock/temporary-use")
    public ResponseDTO<DrugDTO> temporaryUseStock(@PathVariable Long drugId,
        @Valid @RequestBody TemporaryUseDrugStockCommand temporaryUseCommand) {
        Long currentUserId = AuthenticationUtils.getAppLoginUser().getUserId();
        return ResponseDTO.ok(drugApplicationService.temporaryUseDrugStock(drugId, temporaryUseCommand, currentUserId));
    }

    /**
     * 修改个人药品库存配置。
     *
     * <p>当前主要支持库存单位和预警值维护，
     * 不直接改库存绝对值，库存数量增量调整统一走补库存接口。
     */
    @Operation(summary = "修改库存配置")
    @PutMapping("/{drugId}/stock/config")
    public ResponseDTO<DrugDTO> updateStockConfig(@PathVariable Long drugId,
        @Valid @RequestBody UpdateDrugStockConfigCommand updateCommand) {
        Long currentUserId = AuthenticationUtils.getAppLoginUser().getUserId();
        return ResponseDTO.ok(drugApplicationService.updateDrugStockConfig(drugId, updateCommand, currentUserId));
    }

    /**
     * 修改药品。
     */
    @Operation(summary = "修改药品")
    @PutMapping("/{drugId}")
    public ResponseDTO<Void> edit(@PathVariable Long drugId, @Valid @RequestBody UpdateDrugCommand updateCommand) {
        Long currentUserId = AuthenticationUtils.getAppLoginUser().getUserId();
        updateCommand.setDrugId(drugId);
        drugApplicationService.updateDrug(updateCommand, currentUserId);
        return ResponseDTO.ok();
    }

    /**
     * 删除药品。
     */
    @Operation(summary = "删除药品")
    @DeleteMapping("/{drugId}")
    public ResponseDTO<Void> remove(@PathVariable Long drugId) {
        Long currentUserId = AuthenticationUtils.getAppLoginUser().getUserId();
        drugApplicationService.removeDrug(drugId, currentUserId);
        return ResponseDTO.ok();
    }
}
