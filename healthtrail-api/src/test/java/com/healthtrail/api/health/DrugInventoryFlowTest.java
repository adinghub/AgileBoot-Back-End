package com.healthtrail.api.health;

import cn.hutool.core.date.DateUtil;
import com.healthtrail.api.HealthTrailApiApplication;
import com.healthtrail.common.enums.common.StatusEnum;
import com.healthtrail.common.enums.health.HealthAppMessageSceneEnum;
import com.healthtrail.common.enums.health.HealthAppMessageSendStatusEnum;
import com.healthtrail.domain.health.device.db.HealthAppDeviceEntity;
import com.healthtrail.domain.health.device.db.HealthAppDeviceService;
import com.healthtrail.domain.health.drug.DrugApplicationService;
import com.healthtrail.domain.health.drug.command.AddDrugCommand;
import com.healthtrail.domain.health.drug.command.ImportSystemDrugCommand;
import com.healthtrail.domain.health.drug.command.IncreaseDrugStockCommand;
import com.healthtrail.domain.health.drug.command.TemporaryUseDrugStockCommand;
import com.healthtrail.domain.health.drug.dto.DrugDTO;
import com.healthtrail.domain.health.drug.db.DrugEntity;
import com.healthtrail.domain.health.drug.db.HealthDrugService;
import com.healthtrail.domain.health.drug.db.HealthDrugStockBatchEntity;
import com.healthtrail.domain.health.drug.db.HealthDrugStockBatchService;
import com.healthtrail.domain.health.drug.db.HealthDrugStockLogService;
import com.healthtrail.domain.health.drug.db.HealthDrugTemporaryMedicationRecordService;
import com.healthtrail.domain.health.family.db.HealthFamilyMemberEntity;
import com.healthtrail.domain.health.family.db.HealthFamilyMemberService;
import com.healthtrail.domain.health.medication.MedicationApplicationService;
import com.healthtrail.domain.health.medication.command.AddMedicationPlanCommand;
import com.healthtrail.domain.health.medication.command.UpdateMedicationPlanCommand;
import com.healthtrail.domain.health.medication.db.HealthMedicationPlanEntity;
import com.healthtrail.domain.health.medication.db.HealthMedicationPlanService;
import com.healthtrail.domain.health.medication.db.HealthMedicationReminderEntity;
import com.healthtrail.domain.health.medication.db.HealthMedicationReminderService;
import com.healthtrail.domain.health.message.db.HealthAppMessageEntity;
import com.healthtrail.domain.health.message.db.HealthAppMessageService;
import com.healthtrail.domain.health.user.db.HealthAppUserEntity;
import com.healthtrail.domain.health.user.db.HealthAppUserService;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.Date;
import javax.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * 个人药柜库存主链路集成测试。
 *
 * <p>这条测试不只校验“应用能启动”，
 * 而是把库存核心闭环真正走一遍：
 * 1. 引入系统药品到个人药柜
 * 2. 创建绑定个人药品的用药计划
 * 3. 标记提醒已服药并自动扣减库存
 * 4. 触发低库存消息
 * 5. 再补库存恢复安全区
 */
@SpringBootTest(
    classes = HealthTrailApiApplication.class,
    webEnvironment = SpringBootTest.WebEnvironment.NONE,
    properties = {
        "health.medication.reminder.schedule.expire-cron=0 0 0 1 1 ?",
        "health.medication.reminder.schedule.generate-cron=0 0 0 1 1 ?",
        "health.medication.reminder.schedule.dispatch-cron=0 0 0 1 1 ?",
        "health.drug.expire-alert.schedule.check-cron=0 0 0 1 1 ?"
    }
)
@ActiveProfiles({"basic", "test"})
class DrugInventoryFlowTest {

    @Resource
    private DrugApplicationService drugApplicationService;

    @Resource
    private MedicationApplicationService medicationApplicationService;

    @Resource
    private HealthDrugService healthDrugService;

    @Resource
    private HealthDrugStockLogService healthDrugStockLogService;

    @Resource
    private HealthDrugStockBatchService healthDrugStockBatchService;

    @Resource
    private HealthDrugTemporaryMedicationRecordService temporaryMedicationRecordService;

    @Resource
    private HealthMedicationPlanService healthMedicationPlanService;

    @Resource
    private HealthMedicationReminderService medicationReminderService;

    @Resource
    private HealthAppMessageService healthAppMessageService;

    @Resource
    private HealthAppUserService healthAppUserService;

    @Resource
    private HealthFamilyMemberService healthFamilyMemberService;

    @Resource
    private HealthAppDeviceService healthAppDeviceService;

    @Test
    void shouldAddPersonalDrugWithSmallintLowStockFlagInTestSchema() {
        Long userId = createAppUser().getUserId();

        AddDrugCommand addCommand = new AddDrugCommand();
        addCommand.setDrugName("肠炎宁片");
        addCommand.setStockQuantity(new BigDecimal("36"));
        addCommand.setExpireDate(DateUtil.parseDate("2028-07-01"));
        addCommand.setStockUnitId(2L);
        addCommand.setStockAlertThreshold(new BigDecimal("5"));
        addCommand.setStatus(StatusEnum.ENABLE.getValue());

        drugApplicationService.addDrug(addCommand, userId);

        DrugEntity savedDrug = healthDrugService.lambdaQuery()
            .eq(DrugEntity::getOwnerUserId, userId)
            .eq(DrugEntity::getDrugName, "肠炎宁片")
            .orderByDesc(DrugEntity::getDrugId)
            .last("limit 1")
            .one();
        Assertions.assertNotNull(savedDrug, "App 新增个人药品后应成功落库");
        Assertions.assertEquals(2L, savedDrug.getStockUnitId(), "标准单位 ID 应保留到药品主表");
        Assertions.assertEquals("粒", savedDrug.getStockUnit(), "标准单位名称应由后端根据单位主数据回填");
        Assertions.assertFalse(Boolean.TRUE.equals(savedDrug.getLowStockNotified()),
            "新增药品时，SMALLINT 预警标记字段应被正确写成未提醒状态，而不是因为 Boolean 类型不匹配写库失败");
    }

    @Test
    void shouldUseDefaultBatchAndDeductItFirstForTemporaryMedication() {
        Long userId = createAppUser().getUserId();
        Long memberId = createFamilyMember(userId).getMemberId();

        AddDrugCommand addCommand = new AddDrugCommand();
        addCommand.setDrugName("家庭常备感冒药");
        addCommand.setStockQuantity(new BigDecimal("10"));
        addCommand.setBatchNo(null);
        addCommand.setExpireDate(null);
        addCommand.setStockUnitId(1L);
        addCommand.setStockAlertThreshold(new BigDecimal("2"));
        addCommand.setStatus(StatusEnum.ENABLE.getValue());
        drugApplicationService.addDrug(addCommand, userId);

        DrugEntity savedDrug = healthDrugService.lambdaQuery()
            .eq(DrugEntity::getOwnerUserId, userId)
            .eq(DrugEntity::getDrugName, "家庭常备感冒药")
            .orderByDesc(DrugEntity::getDrugId)
            .last("limit 1")
            .one();
        Assertions.assertNotNull(savedDrug, "新增常备药后应能查到个人药品主档");

        DrugDTO createdDrug = drugApplicationService.getDrugInfo(savedDrug.getDrugId(), userId);
        Assertions.assertEquals(1, createdDrug.getStockBatches().size(),
            "没有批号和效期的首批库存应被并到同一个默认批次");
        Assertions.assertTrue(Boolean.TRUE.equals(createdDrug.getStockBatches().get(0).getDefaultBatch()),
            "无批号无效期录入的库存桶必须被显式标记为默认批次");
        Assertions.assertNull(createdDrug.getStockBatches().get(0).getExpireDate(),
            "默认批次不应该再被强行补出伪造效期");

        IncreaseDrugStockCommand normalBatchIncrease = new IncreaseDrugStockCommand();
        normalBatchIncrease.setIncreaseQuantity(new BigDecimal("3"));
        normalBatchIncrease.setBatchNo("COLD-202604");
        normalBatchIncrease.setExpireDate(DateUtil.parseDate("2027-12-31"));
        normalBatchIncrease.setOperationRemark("补入有明确批号的库存");
        DrugDTO increasedDrug = drugApplicationService.increaseDrugStock(savedDrug.getDrugId(), normalBatchIncrease, userId);
        Assertions.assertEquals(2, increasedDrug.getStockBatches().size(),
            "增加普通批次后，应同时保留默认批次和普通批次两条库存明细");

        TemporaryUseDrugStockCommand temporaryUseCommand = new TemporaryUseDrugStockCommand();
        temporaryUseCommand.setMemberId(memberId);
        temporaryUseCommand.setUsedQuantity(new BigDecimal("4"));
        temporaryUseCommand.setUseTime(DateUtil.parse("2026-04-27 21:35:00"));
        temporaryUseCommand.setSymptom("感冒");
        temporaryUseCommand.setRemark("睡前临时服用");
        DrugDTO afterTemporaryUse = drugApplicationService.temporaryUseDrugStock(savedDrug.getDrugId(), temporaryUseCommand,
            userId);
        com.healthtrail.domain.health.drug.dto.DrugStockBatchDTO defaultBatch = afterTemporaryUse.getStockBatches()
            .stream()
            .filter(batch -> Boolean.TRUE.equals(batch.getDefaultBatch()))
            .findFirst()
            .orElse(null);
        com.healthtrail.domain.health.drug.dto.DrugStockBatchDTO normalBatch = afterTemporaryUse.getStockBatches()
            .stream()
            .filter(batch -> !Boolean.TRUE.equals(batch.getDefaultBatch()))
            .findFirst()
            .orElse(null);

        Assertions.assertEquals(0, new BigDecimal("9").compareTo(afterTemporaryUse.getStockQuantity()),
            "临时用药成功后，药品总库存应被正确扣减");
        Assertions.assertNotNull(defaultBatch, "临时用药后的库存明细里仍应能找到默认批次");
        Assertions.assertNotNull(normalBatch, "临时用药后的库存明细里仍应保留普通批次");
        Assertions.assertEquals(0, new BigDecimal("6").compareTo(defaultBatch.getStockQuantity()),
            "临时用药应优先扣减默认批次库存");
        Assertions.assertEquals(0, new BigDecimal("3").compareTo(normalBatch.getStockQuantity()),
            "默认批次足够时，普通批次库存不应被提前扣减");

        long temporaryRecordCount = temporaryMedicationRecordService.lambdaQuery()
            .eq(com.healthtrail.domain.health.drug.db.HealthDrugTemporaryMedicationRecordEntity::getDrugId, savedDrug.getDrugId())
            .count();
        Assertions.assertEquals(1L, temporaryRecordCount, "登记临时用药后应新增一条独立的临时用药记录");

        long temporaryStockLogCount = healthDrugStockLogService.lambdaQuery()
            .eq(com.healthtrail.domain.health.drug.db.HealthDrugStockLogEntity::getDrugId, savedDrug.getDrugId())
            .eq(com.healthtrail.domain.health.drug.db.HealthDrugStockLogEntity::getChangeType, "TEMPORARY_USE_DEDUCT")
            .count();
        Assertions.assertEquals(1L, temporaryStockLogCount, "临时用药扣库存后应新增对应的库存流水");
    }

    @Test
    void shouldImportSystemDrugAndAutoDeductStockAfterTakingReminder() {
        Long userId = createAppUser().getUserId();
        Long memberId = createFamilyMember(userId).getMemberId();
        createActiveDevice(userId);

        ImportSystemDrugCommand importCommand = new ImportSystemDrugCommand();
        importCommand.setTargetDrugName("阿司匹林(家庭常备)");
        importCommand.setStockQuantity(new BigDecimal("2"));
        importCommand.setBatchNo(null);
        importCommand.setExpireDate(DateUtil.parseDate("2026-12-31"));
        importCommand.setStockUnitId(1L);
        importCommand.setStockAlertThreshold(new BigDecimal("3"));
        importCommand.setRemark("库存闭环测试数据");

        DrugDTO importedDrug = drugApplicationService.importSystemDrug(1L, importCommand, userId);
        Assertions.assertNotNull(importedDrug.getDrugId(), "引入系统药品后应返回个人药品ID");
        Assertions.assertEquals(1L, importedDrug.getSourceDrugId(), "个人药品应记录来源系统药品ID");
        Assertions.assertTrue(Boolean.TRUE.equals(importedDrug.getImportedFromSystem()), "应标记为系统药品引入");
        Assertions.assertEquals(1L, importedDrug.getStockUnitId(), "通过标准单位ID引入时应正确回填单位ID");
        Assertions.assertEquals("片", importedDrug.getStockUnit(), "库存单位应正确回填");
        Assertions.assertEquals("/images/drug-default.svg", importedDrug.getImageUrl(),
            "未上传药品图片时应统一返回默认图片地址");
        Assertions.assertEquals(0, new BigDecimal("2").compareTo(importedDrug.getStockQuantity()),
            "首批库存应实时汇总到药品详情中");
        Assertions.assertEquals(1, importedDrug.getStockBatches().size(),
            "引入系统药品并录入首批库存后，应返回一条批次库存明细");
        Assertions.assertNull(importedDrug.getStockBatches().get(0).getBatchNo(),
            "批号允许为空，前端应能直接回显空批号批次");

        IncreaseDrugStockCommand increaseCommand = new IncreaseDrugStockCommand();
        increaseCommand.setIncreaseQuantity(new BigDecimal("3"));
        increaseCommand.setBatchNo("ASP-202604");
        increaseCommand.setExpireDate(DateUtil.parseDate("2026-10-31"));
        increaseCommand.setOperationRemark("测试补库存");
        DrugDTO increasedDrug = drugApplicationService.increaseDrugStock(importedDrug.getDrugId(), increaseCommand, userId);
        Assertions.assertEquals(0, new BigDecimal("5").compareTo(increasedDrug.getStockQuantity()),
            "补库存后药品总库存应由批次实时汇总得到");
        Assertions.assertEquals(2, increasedDrug.getStockBatches().size(),
            "补库存后应存在两条批次库存明细");
        Assertions.assertEquals("ASP-202604", increasedDrug.getStockBatches().get(0).getBatchNo(),
            "库存明细应按最早效期优先排序，较早到期批次应排在前面");
        Assertions.assertEquals(0, new BigDecimal("3").compareTo(increasedDrug.getStockBatches().get(0).getStockQuantity()),
            "补入的新批次库存数量应正确记录");

        AddMedicationPlanCommand addPlanCommand = new AddMedicationPlanCommand();
        Date tomorrow = DateUtil.beginOfDay(DateUtil.offsetDay(new Date(), 1));
        addPlanCommand.setMemberId(memberId);
        addPlanCommand.setDrugId(importedDrug.getDrugId());
        addPlanCommand.setStartDate(tomorrow);
        addPlanCommand.setEndDate(tomorrow);
        addPlanCommand.setReminderTimes(Collections.singletonList("08:00"));
        addPlanCommand.setDoseAmount(new BigDecimal("2"));
        addPlanCommand.setDoseUnit("片");
        addPlanCommand.setFrequencyType("DAILY");
        addPlanCommand.setStatus(StatusEnum.ENABLE.getValue());
        medicationApplicationService.addMedicationPlan(addPlanCommand, userId);

        HealthMedicationReminderEntity reminderEntity = medicationReminderService.lambdaQuery()
            .eq(HealthMedicationReminderEntity::getOwnerUserId, userId)
            .eq(HealthMedicationReminderEntity::getDrugNameSnapshot, "阿司匹林(家庭常备)")
            .orderByDesc(HealthMedicationReminderEntity::getReminderId)
            .one();
        Assertions.assertNotNull(reminderEntity, "创建用药计划后应生成至少一条提醒记录");

        medicationApplicationService.markReminderTaken(reminderEntity.getReminderId(), userId);

        DrugDTO deductedDrug = drugApplicationService.getDrugInfo(importedDrug.getDrugId(), userId);
        Assertions.assertEquals(0, new BigDecimal("3").compareTo(deductedDrug.getStockQuantity()),
            "标记已服药后应按剂量从批次库存实时汇总得到新库存");
        Assertions.assertEquals(2, deductedDrug.getStockBatches().size(),
            "扣减后仍应保留有库存的两条批次明细");
        Assertions.assertEquals("ASP-202604", deductedDrug.getStockBatches().get(0).getBatchNo(),
            "自动扣减应优先消费最早效期批次");
        Assertions.assertEquals(0, new BigDecimal("1").compareTo(deductedDrug.getStockBatches().get(0).getStockQuantity()),
            "较早到期批次应先被扣减到剩余1片");
        Assertions.assertEquals(0, new BigDecimal("2").compareTo(deductedDrug.getStockBatches().get(1).getStockQuantity()),
            "较晚到期批次库存应保持不变");

        DrugEntity deductedDrugEntity = healthDrugService.getById(importedDrug.getDrugId());
        Assertions.assertTrue(Boolean.TRUE.equals(deductedDrugEntity.getLowStockNotified()),
            "库存达到预警值后应把当前周期标记为已提醒");

        java.util.List<HealthAppMessageEntity> lowStockMessages = healthAppMessageService.lambdaQuery()
            .eq(HealthAppMessageEntity::getOwnerUserId, userId)
            .eq(HealthAppMessageEntity::getBusinessScene, HealthAppMessageSceneEnum.LOW_STOCK_ALERT.getValue())
            .eq(HealthAppMessageEntity::getBusinessId, importedDrug.getDrugId())
            .orderByDesc(HealthAppMessageEntity::getMessageId)
            .list();
        Assertions.assertFalse(lowStockMessages.isEmpty(), "库存进入预警区间后应生成低库存消息");
        Assertions.assertEquals(HealthAppMessageSendStatusEnum.SUCCESS.getValue(), lowStockMessages.get(0).getSendStatus(),
            "测试设备存在时，低库存消息应被成功派发");

        IncreaseDrugStockCommand recoverCommand = new IncreaseDrugStockCommand();
        recoverCommand.setIncreaseQuantity(new BigDecimal("5"));
        recoverCommand.setBatchNo("ASP-RECOVER");
        recoverCommand.setExpireDate(DateUtil.parseDate("2027-01-31"));
        recoverCommand.setOperationRemark("测试补库存");
        DrugDTO recoveredDrugDTO = drugApplicationService.increaseDrugStock(importedDrug.getDrugId(), recoverCommand, userId);
        Assertions.assertEquals(0, new BigDecimal("8").compareTo(recoveredDrugDTO.getStockQuantity()),
            "补库存后应正确累加库存数量");

        DrugEntity recoveredDrug = healthDrugService.getById(importedDrug.getDrugId());
        Assertions.assertFalse(Boolean.TRUE.equals(recoveredDrug.getLowStockNotified()),
            "库存恢复到安全区后应重置预警周期标记");
        Assertions.assertNull(recoveredDrug.getLowStockNotifyTime(),
            "库存恢复到安全区后应清空最近一次低库存提醒时间");

        long stockLogCount = healthDrugStockLogService.lambdaQuery()
            .eq(com.healthtrail.domain.health.drug.db.HealthDrugStockLogEntity::getDrugId, importedDrug.getDrugId())
            .count();
        Assertions.assertTrue(stockLogCount >= 3, "应至少留下引入库存、服药扣减、补库存三条库存流水");
    }

    @Test
    void shouldClearEndDateWhenUpdatingMedicationPlanToLongTerm() {
        Long userId = createAppUser().getUserId();
        Long memberId = createFamilyMember(userId).getMemberId();

        AddMedicationPlanCommand addCommand = new AddMedicationPlanCommand();
        Date today = DateUtil.beginOfDay(new Date());
        Date sevenDaysLater = DateUtil.offsetDay(today, 7);
        addCommand.setMemberId(memberId);
        addCommand.setCustomDrugName("长期服用维生素D");
        addCommand.setStartDate(today);
        addCommand.setEndDate(sevenDaysLater);
        addCommand.setReminderTimes(Collections.singletonList("09:00"));
        addCommand.setDoseAmount(new BigDecimal("1"));
        addCommand.setDoseUnit("粒");
        addCommand.setFrequencyType("DAILY");
        addCommand.setStatus(StatusEnum.ENABLE.getValue());
        medicationApplicationService.addMedicationPlan(addCommand, userId);

        HealthMedicationPlanEntity savedPlan = healthMedicationPlanService.lambdaQuery()
            .eq(HealthMedicationPlanEntity::getOwnerUserId, userId)
            .eq(HealthMedicationPlanEntity::getMemberId, memberId)
            .eq(HealthMedicationPlanEntity::getCustomDrugName, "长期服用维生素D")
            .orderByDesc(HealthMedicationPlanEntity::getPlanId)
            .one();
        Assertions.assertNotNull(savedPlan, "新增计划后应能查到刚保存的记录");
        Assertions.assertNotNull(savedPlan.getEndDate(), "测试前置数据必须先带有结束日期，才能覆盖“清空结束日期”的回归场景");

        UpdateMedicationPlanCommand updateCommand = new UpdateMedicationPlanCommand();
        updateCommand.setPlanId(savedPlan.getPlanId());
        updateCommand.setMemberId(memberId);
        updateCommand.setCustomDrugName("长期服用维生素D");
        updateCommand.setStartDate(today);
        updateCommand.setEndDate(null);
        updateCommand.setReminderTimes(Collections.singletonList("09:00"));
        updateCommand.setDoseAmount(new BigDecimal("1"));
        updateCommand.setDoseUnit("粒");
        updateCommand.setFrequencyType("DAILY");
        updateCommand.setStatus(StatusEnum.ENABLE.getValue());
        medicationApplicationService.updateMedicationPlan(updateCommand, userId);

        HealthMedicationPlanEntity updatedPlan = healthMedicationPlanService.getById(savedPlan.getPlanId());
        Assertions.assertNotNull(updatedPlan, "更新计划后原记录应继续存在");
        Assertions.assertNull(updatedPlan.getEndDate(),
            "当编辑计划时显式把结束日期清空，数据库里的 end_date 也必须被真正更新为 null，而不是保留旧值");
    }

    @Test
    void shouldCreateInAppNearExpiryAlertWithoutDevicePush() {
        Long userId = createAppUser().getUserId();

        AddDrugCommand addCommand = new AddDrugCommand();
        addCommand.setDrugName("近效期测试药");
        addCommand.setStockQuantity(new BigDecimal("6"));
        addCommand.setBatchNo("EXP-20260427");
        addCommand.setExpireDate(DateUtil.offsetDay(DateUtil.beginOfDay(new Date()), 5));
        addCommand.setStockUnitId(1L);
        addCommand.setStatus(StatusEnum.ENABLE.getValue());
        drugApplicationService.addDrug(addCommand, userId);

        DrugEntity savedDrug = healthDrugService.lambdaQuery()
            .eq(DrugEntity::getOwnerUserId, userId)
            .eq(DrugEntity::getDrugName, "近效期测试药")
            .orderByDesc(DrugEntity::getDrugId)
            .last("limit 1")
            .one();
        Assertions.assertNotNull(savedDrug, "近效期测试药应成功落库");

        int createdAlertCount = drugApplicationService.inspectAndDispatchNearExpiryAlerts(7);
        Assertions.assertEquals(1, createdAlertCount, "首次巡检进入近效期窗口的批次时，应创建一条提醒消息");

        HealthDrugStockBatchEntity batchEntity = healthDrugStockBatchService.lambdaQuery()
            .eq(HealthDrugStockBatchEntity::getOwnerUserId, userId)
            .eq(HealthDrugStockBatchEntity::getDrugId, savedDrug.getDrugId())
            .orderByDesc(HealthDrugStockBatchEntity::getBatchId)
            .last("limit 1")
            .one();
        Assertions.assertNotNull(batchEntity, "应能查到刚创建的库存批次");
        Assertions.assertTrue(Boolean.TRUE.equals(batchEntity.getNearExpireNotified()),
            "近效期巡检命中后，应把该批次标记为当前周期已提醒");
        Assertions.assertNotNull(batchEntity.getNearExpireNotifyTime(),
            "近效期巡检命中后，应记录最近一次提醒时间");

        java.util.List<HealthAppMessageEntity> nearExpiryMessages = healthAppMessageService.lambdaQuery()
            .eq(HealthAppMessageEntity::getOwnerUserId, userId)
            .eq(HealthAppMessageEntity::getBusinessScene, HealthAppMessageSceneEnum.NEAR_EXPIRY_ALERT.getValue())
            .eq(HealthAppMessageEntity::getBusinessId, savedDrug.getDrugId())
            .orderByDesc(HealthAppMessageEntity::getMessageId)
            .list();
        Assertions.assertEquals(1, nearExpiryMessages.size(), "同一批次近效期首轮只应创建一条消息");
        Assertions.assertEquals(HealthAppMessageSendStatusEnum.SUCCESS.getValue(), nearExpiryMessages.get(0).getSendStatus(),
            "近效期提醒虽然不走设备推送，但站内消息应被直接标记为发送成功");
        Assertions.assertEquals("IN_APP", nearExpiryMessages.get(0).getSendChannel(),
            "近效期提醒应明确记录为站内消息渠道，而不是设备 Push 渠道");

        int secondRunCreatedAlertCount = drugApplicationService.inspectAndDispatchNearExpiryAlerts(7);
        Assertions.assertEquals(0, secondRunCreatedAlertCount, "同一批次在同一轮近效期窗口内不应重复提醒");
    }

    /**
     * 创建测试 App 用户。
     */
    private HealthAppUserEntity createAppUser() {
        HealthAppUserEntity userEntity = new HealthAppUserEntity();
        userEntity.setMobile(String.format("13%09d", Math.abs(System.nanoTime()) % 1_000_000_000L));
        userEntity.setNickname("库存测试用户");
        userEntity.setPassword("test-password");
        userEntity.setStatus(StatusEnum.ENABLE.getValue());
        userEntity.setRegisterSource("TEST");
        userEntity.setDeleted(0);
        healthAppUserService.save(userEntity);
        return userEntity;
    }

    /**
     * 创建测试家庭成员。
     */
    private HealthFamilyMemberEntity createFamilyMember(Long ownerUserId) {
        HealthFamilyMemberEntity memberEntity = new HealthFamilyMemberEntity();
        memberEntity.setOwnerUserId(ownerUserId);
        memberEntity.setMemberName("库存测试成员");
        memberEntity.setGender(1);
        memberEntity.setRelationType("SELF");
        memberEntity.setStatus(StatusEnum.ENABLE.getValue());
        memberEntity.setDeleted(0);
        healthFamilyMemberService.save(memberEntity);
        return memberEntity;
    }

    /**
     * 创建一个可用于消息推送的活跃测试设备。
     *
     * <p>当前默认 Push 网关未接入时，项目会走模拟发送分支，
     * 但前提仍然是账号下至少存在一台启用中的设备。
     */
    private void createActiveDevice(Long ownerUserId) {
        HealthAppDeviceEntity deviceEntity = new HealthAppDeviceEntity();
        deviceEntity.setOwnerUserId(ownerUserId);
        deviceEntity.setDeviceCode("device-" + System.nanoTime());
        deviceEntity.setPushPlatform("ANDROID");
        deviceEntity.setDeviceToken("token-" + System.nanoTime());
        deviceEntity.setStatus(StatusEnum.ENABLE.getValue());
        deviceEntity.setDeleted(0);
        healthAppDeviceService.save(deviceEntity);
    }
}
