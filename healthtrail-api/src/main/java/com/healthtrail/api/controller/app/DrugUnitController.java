package com.healthtrail.api.controller.app;

import com.healthtrail.common.core.base.BaseController;
import com.healthtrail.common.core.dto.ResponseDTO;
import com.healthtrail.domain.health.drug.unit.DrugUnitApplicationService;
import com.healthtrail.domain.health.drug.unit.dto.DrugUnitDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * App 端药品单位控制器。
 *
 * <p>前端新增/编辑药品时只需要调用这个接口做搜索和选择，
 * 不再让用户自己手动输入大量不统一的单位文本。
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/app/drug-units")
@Tag(name = "App药品单位API", description = "App端药品单位搜索接口")
public class DrugUnitController extends BaseController {

    private final DrugUnitApplicationService drugUnitApplicationService;

    /**
     * 查询可选药品单位列表。
     */
    @Operation(summary = "药品单位列表")
    @GetMapping
    public ResponseDTO<List<DrugUnitDTO>> list(@RequestParam(value = "keyword", required = false) String keyword) {
        return ResponseDTO.ok(drugUnitApplicationService.listEnabledUnits(keyword));
    }
}
