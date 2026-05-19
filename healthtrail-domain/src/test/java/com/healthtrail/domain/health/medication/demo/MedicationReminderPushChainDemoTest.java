package com.healthtrail.domain.health.medication.demo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

/**
 * 用药提醒后端 Push 链路学习 Demo。
 *
 * <p>这个测试不是为了验证某个生产 Bean，而是专门把真实项目里的链路拆成一套“可单步阅读”的最小闭环：
 * 1. 定时任务扫描到点提醒
 * 2. 应用服务筛选待发送提醒并逐条派发
 * 3. 通知器按用户活跃设备查询 deviceToken
 * 4. Push 网关把消息发到设备
 * 5. 发送成功/失败结果回写到提醒记录和消息记录
 *
 * <p>之所以写成一个独立测试，而不是直接让你跟读生产代码，是因为生产代码里还混着计划补齐、成员权限、
 * 首页任务透传载荷、消息中心复用等配套逻辑。对于学习“后端如何发起提醒 Push”这件事来说，
 * 先跑通一个足够像真实链路、又不会被业务细节淹没的 demo，会更容易建立整体认知。
 */
class MedicationReminderPushChainDemoTest {

    @Test
    void shouldRunCompleteBackendPushChainWithRetry() {
        DemoClock clock = new DemoClock(LocalDateTime.of(2026, 4, 28, 9, 30));
        InMemoryMedicationReminderRepository reminderRepository = new InMemoryMedicationReminderRepository();
        InMemoryAppDeviceRepository deviceRepository = new InMemoryAppDeviceRepository();
        InMemoryAppMessageRepository messageRepository = new InMemoryAppMessageRepository();
        FakePushGatewayClient pushGatewayClient = new FakePushGatewayClient();

        AppDeviceApplicationServiceDemo appDeviceApplicationService =
            new AppDeviceApplicationServiceDemo(deviceRepository, clock);
        AppPushMedicationReminderNotifierDemo notifier =
            new AppPushMedicationReminderNotifierDemo(deviceRepository, pushGatewayClient);
        MedicationApplicationServiceDemo medicationApplicationService =
            new MedicationApplicationServiceDemo(reminderRepository, messageRepository, notifier, clock);
        MedicationReminderScheduleJobDemo scheduleJob =
            new MedicationReminderScheduleJobDemo(medicationApplicationService, 10, 3);

        // 先给用户 1001 注册两台活跃设备，用来演示“一个提醒会同时派发到多个设备”。
        appDeviceApplicationService.registerDevice(new RegisterDeviceRequest(
            "device-a-1", 1001L, "ANDROID", "token-a-1"));
        appDeviceApplicationService.registerDevice(new RegisterDeviceRequest(
            "device-a-2", 1001L, "IOS", "token-a-2"));

        // 给用户 2002 先注册一个设备再停用，用来演示“用户存在设备记录，但没有活跃 Token”时的失败重试。
        appDeviceApplicationService.registerDevice(new RegisterDeviceRequest(
            "device-b-1", 2002L, "ANDROID", "token-b-old"));
        appDeviceApplicationService.unregisterDevice("device-b-1", 2002L);

        reminderRepository.saveAll(List.of(
            DemoMedicationReminder.createPending(1L, 1001L, 501L, "父亲", "阿司匹林", clock.now().minusMinutes(5)),
            DemoMedicationReminder.createPending(2L, 2002L, 601L, "母亲", "维生素D", clock.now().minusMinutes(3)),
            // 这条提醒时间还没到，所以本轮调度不会被扫出来。
            DemoMedicationReminder.createPending(3L, 3003L, 701L, "自己", "辅酶Q10", clock.now().plusMinutes(30)),
            // 这条提醒已经达到最大重试次数，调度层会跳过，避免无限重试。
            DemoMedicationReminder.createFailed(4L, 4004L, 801L, "孩子", "赖氨酸", clock.now().minusMinutes(10), 3)
        ));

        int firstDispatchCount = scheduleJob.dispatchDueReminders();

        DemoMedicationReminder reminder1 = reminderRepository.getById(1L);
        DemoMedicationReminder reminder2 = reminderRepository.getById(2L);
        DemoMedicationReminder reminder3 = reminderRepository.getById(3L);
        DemoMedicationReminder reminder4 = reminderRepository.getById(4L);

        assertEquals(1, firstDispatchCount, "第一轮只有用户 1001 的提醒成功发出");
        assertEquals(ReminderNotifyStatus.SUCCESS, reminder1.getNotifyStatus());
        assertEquals(ReminderNotifyStatus.FAILED, reminder2.getNotifyStatus());
        assertEquals("当前用户没有可用的活跃设备Token", reminder2.getNotifyFailReason());
        assertEquals(1, reminder2.getNotifyRetryCount());
        assertEquals(ReminderNotifyStatus.PENDING, reminder3.getNotifyStatus());
        assertEquals(3, reminder4.getNotifyRetryCount(), "达到最大重试次数的提醒不应再被本轮调度处理");

        assertEquals(2, pushGatewayClient.getSentRecords().size(), "同一条提醒应派发到用户的两台活跃设备");
        assertTrue(messageRepository.getByDedupKey("MEDICATION_REMINDER:1").isSendSuccess());
        assertFalse(messageRepository.getByDedupKey("MEDICATION_REMINDER:2").isSendSuccess());

        // 给用户 2002 重新注册一个有效设备，再次触发调度，观察失败提醒如何被重试成功。
        appDeviceApplicationService.registerDevice(new RegisterDeviceRequest(
            "device-b-2", 2002L, "ANDROID", "token-b-new"));

        int secondDispatchCount = scheduleJob.dispatchDueReminders();
        DemoMedicationReminder retriedReminder = reminderRepository.getById(2L);
        DemoAppMessage retriedMessage = messageRepository.getByDedupKey("MEDICATION_REMINDER:2");

        assertEquals(1, secondDispatchCount, "第二轮应把上一轮失败的提醒重试成功");
        assertEquals(ReminderNotifyStatus.SUCCESS, retriedReminder.getNotifyStatus());
        assertEquals(1, retriedReminder.getNotifyRetryCount(), "重试成功后保留历史失败次数，方便审计");
        assertNull(retriedReminder.getNotifyFailReason(), "重试成功后应清空失败原因");
        assertTrue(retriedMessage.isSendSuccess(), "消息中心里的同一条业务消息应被复用并更新为成功");
        assertEquals(3, pushGatewayClient.getSentRecords().size(), "第二轮只会新增一次成功派发记录");
    }

    /**
     * 调度任务 Demo。
     *
     * <p>它对应生产代码里的 `MedicationReminderScheduleJob.dispatchDueReminders()`：
     * 调度层不关心设备、Token、Push SDK 这些细节，只负责按固定频率把“到点提醒派发”这件事交给应用服务。
     */
    private static final class MedicationReminderScheduleJobDemo {

        private final MedicationApplicationServiceDemo medicationApplicationService;

        private final int dispatchBatchSize;

        private final int notifyMaxRetryCount;

        private MedicationReminderScheduleJobDemo(MedicationApplicationServiceDemo medicationApplicationService,
            int dispatchBatchSize, int notifyMaxRetryCount) {
            this.medicationApplicationService = medicationApplicationService;
            this.dispatchBatchSize = dispatchBatchSize;
            this.notifyMaxRetryCount = notifyMaxRetryCount;
        }

        private int dispatchDueReminders() {
            return medicationApplicationService.dispatchDueReminders(dispatchBatchSize, notifyMaxRetryCount);
        }
    }

    /**
     * 用药提醒应用服务 Demo。
     *
     * <p>它对应生产代码里的 `MedicationApplicationService.dispatchDueReminders()` 和
     * `dispatchSingleReminder()` 两段核心逻辑，职责是：
     * 1. 从提醒仓库里找出“已到点、待发送、未超过最大重试次数”的记录
     * 2. 把数据库记录转换成通知标准载荷
     * 3. 调用发送器发送
     * 4. 回写提醒状态和消息发送结果
     */
    private static final class MedicationApplicationServiceDemo {

        private final InMemoryMedicationReminderRepository reminderRepository;

        private final InMemoryAppMessageRepository messageRepository;

        private final MedicationReminderNotifierDemo notifier;

        private final DemoClock clock;

        private MedicationApplicationServiceDemo(InMemoryMedicationReminderRepository reminderRepository,
            InMemoryAppMessageRepository messageRepository, MedicationReminderNotifierDemo notifier, DemoClock clock) {
            this.reminderRepository = reminderRepository;
            this.messageRepository = messageRepository;
            this.notifier = notifier;
            this.clock = clock;
        }

        private int dispatchDueReminders(int batchSize, int maxRetryCount) {
            List<DemoMedicationReminder> dueReminders = reminderRepository.listDueReminders(clock.now(), batchSize,
                maxRetryCount);

            int successCount = 0;
            for (DemoMedicationReminder reminder : dueReminders) {
                if (dispatchSingleReminder(reminder)) {
                    successCount++;
                }
            }
            return successCount;
        }

        private boolean dispatchSingleReminder(DemoMedicationReminder reminder) {
            MedicationReminderNoticeDemo notice = buildReminderNotice(reminder);
            DemoAppMessage message = messageRepository.createOrReuse(
                "MEDICATION_REMINDER:" + reminder.getReminderId(), reminder.getReminderId(), reminder.getOwnerUserId(),
                reminder.getMemberId(), notice.getTitle(), notice.getContent());

            try {
                MedicationReminderNotifyResultDemo result = notifier.notify(notice);
                if (result.isSuccess()) {
                    message.markSuccess(result.getChannel(), result.getMessage(), clock.now());
                    reminder.markNotifySuccess(clock.now());
                    return true;
                }

                message.markFailed(result.getChannel(), result.getMessage(), clock.now());
                reminder.markNotifyFailed(clock.now(), result.getMessage());
                return false;
            } catch (Exception ex) {
                message.markFailed(null, ex.getMessage(), clock.now());
                reminder.markNotifyFailed(clock.now(), ex.getMessage());
                return false;
            }
        }

        /**
         * 把提醒数据库记录收敛成发送器真正关心的字段。
         *
         * <p>生产代码也会做这一步，因为发送器不应该直接依赖整个数据库实体，否则后续改表结构时，
         * Push、短信、站内信这些通道都会被牵连。
         */
        private MedicationReminderNoticeDemo buildReminderNotice(DemoMedicationReminder reminder) {
            String title = reminder.getMemberName() + " 的用药提醒待处理";
            String content = reminder.getDrugName() + "，计划时间 "
                + reminder.getScheduledTime().toLocalTime() + "，请及时确认是否已服药。";
            return new MedicationReminderNoticeDemo(reminder.getReminderId(), reminder.getOwnerUserId(),
                reminder.getMemberId(), reminder.getMemberName(), reminder.getDrugName(), reminder.getScheduledTime(),
                title, content);
        }
    }

    /**
     * 设备注册应用服务 Demo。
     *
     * <p>它对应生产代码里的 `AppDeviceApplicationService`。这里重点演示两件事：
     * 1. App 拿到 token 后，要把“用户、设备、平台、token”上报到后端
     * 2. 用户退出登录或关闭通知时，不删除记录，而是把设备改成停用状态
     */
    private static final class AppDeviceApplicationServiceDemo {

        private final InMemoryAppDeviceRepository deviceRepository;

        private final DemoClock clock;

        private AppDeviceApplicationServiceDemo(InMemoryAppDeviceRepository deviceRepository, DemoClock clock) {
            this.deviceRepository = deviceRepository;
            this.clock = clock;
        }

        private void registerDevice(RegisterDeviceRequest request) {
            DemoAppDevice existingDevice = deviceRepository.getByDeviceCode(request.getDeviceCode());
            if (existingDevice == null) {
                deviceRepository.save(new DemoAppDevice(request.getDeviceCode(), request.getOwnerUserId(),
                    request.getPushPlatform(), request.getDeviceToken(), DeviceStatus.ENABLE, clock.now()));
                return;
            }

            existingDevice.refreshBinding(request.getOwnerUserId(), request.getPushPlatform(),
                request.getDeviceToken(), clock.now());
        }

        private void unregisterDevice(String deviceCode, Long ownerUserId) {
            DemoAppDevice existingDevice = deviceRepository.getByDeviceCode(deviceCode);
            if (existingDevice != null && Objects.equals(existingDevice.getOwnerUserId(), ownerUserId)) {
                existingDevice.disable(clock.now());
            }
        }
    }

    /**
     * 用药提醒通知发送器 Demo。
     *
     * <p>它对应生产代码里的 `AppPushMedicationReminderNotifier`：
     * 1. 先按 ownerUserId 查活跃设备
     * 2. 再过滤出 status=ENABLE 且 deviceToken 非空的设备
     * 3. 如果没有可发设备，则返回失败并让上层决定是否重试
     * 4. 如果有设备，则交给 Push 网关逐台发送
     */
    private static final class AppPushMedicationReminderNotifierDemo implements MedicationReminderNotifierDemo {

        private static final String NO_ACTIVE_DEVICE_TOKEN_MESSAGE = "当前用户没有可用的活跃设备Token";

        private final InMemoryAppDeviceRepository deviceRepository;

        private final FakePushGatewayClient pushGatewayClient;

        private AppPushMedicationReminderNotifierDemo(InMemoryAppDeviceRepository deviceRepository,
            FakePushGatewayClient pushGatewayClient) {
            this.deviceRepository = deviceRepository;
            this.pushGatewayClient = pushGatewayClient;
        }

        @Override
        public MedicationReminderNotifyResultDemo notify(MedicationReminderNoticeDemo notice) {
            List<DemoAppDevice> activeDevices = deviceRepository.listActiveDevices(notice.getOwnerUserId()).stream()
                .filter(device -> device.getStatus() == DeviceStatus.ENABLE)
                .filter(device -> isNotBlank(device.getDeviceToken()))
                .collect(Collectors.toList());

            if (activeDevices.isEmpty()) {
                return MedicationReminderNotifyResultDemo.failed("APP_PUSH", NO_ACTIVE_DEVICE_TOKEN_MESSAGE);
            }

            pushGatewayClient.pushBatch(activeDevices, notice);
            return MedicationReminderNotifyResultDemo.success("APP_PUSH", "已派发到 " + activeDevices.size() + " 个设备");
        }
    }

    /**
     * Push 网关客户端 Demo。
     *
     * <p>生产项目里这里会调用外部 HTTP 网关或厂商 SDK；demo 则故意只把发送记录记到内存里，
     * 这样你既能看到“最终发给了哪些 deviceToken”，又不用依赖任何第三方环境就能跑通全链路。
     */
    private static final class FakePushGatewayClient {

        private final List<PushSendRecord> sentRecords = new ArrayList<>();

        private void pushBatch(List<DemoAppDevice> devices, MedicationReminderNoticeDemo notice) {
            for (DemoAppDevice device : devices) {
                sentRecords.add(new PushSendRecord(notice.getReminderId(), notice.getOwnerUserId(),
                    device.getDeviceCode(), device.getPushPlatform(), device.getDeviceToken(), notice.getTitle(),
                    notice.getContent()));
            }
        }

        private List<PushSendRecord> getSentRecords() {
            return sentRecords;
        }
    }

    /**
     * 提醒仓库 Demo。
     *
     * <p>这里故意把“到点筛选条件”写得和生产逻辑接近一些，方便你把 demo 和真实项目对应起来：
     * 1. reminderStatus 必须还是待处理
     * 2. scheduledTime 必须已经到点
     * 3. notifyStatus 只能是待发送或发送失败
     * 4. notifyRetryCount 必须小于最大重试次数
     */
    private static final class InMemoryMedicationReminderRepository {

        private final Map<Long, DemoMedicationReminder> data = new HashMap<>();

        private void saveAll(Collection<DemoMedicationReminder> reminders) {
            reminders.forEach(reminder -> data.put(reminder.getReminderId(), reminder));
        }

        private DemoMedicationReminder getById(Long reminderId) {
            return data.get(reminderId);
        }

        private List<DemoMedicationReminder> listDueReminders(LocalDateTime now, int batchSize, int maxRetryCount) {
            return data.values().stream()
                .filter(reminder -> reminder.getReminderStatus() == ReminderBusinessStatus.PENDING)
                .filter(reminder -> !reminder.getScheduledTime().isAfter(now))
                .filter(reminder -> reminder.getNotifyRetryCount() < maxRetryCount)
                .filter(reminder -> reminder.getNotifyStatus() == ReminderNotifyStatus.PENDING
                    || reminder.getNotifyStatus() == ReminderNotifyStatus.FAILED)
                .sorted(Comparator.comparing(DemoMedicationReminder::getScheduledTime))
                .limit(batchSize)
                .collect(Collectors.toList());
        }
    }

    /**
     * 设备仓库 Demo。
     *
     * <p>真实项目里 `listActiveDevices(ownerUserId)` 会查数据库；这里用内存 Map 模拟。
     * 这样你在阅读时可以把注意力放在“为什么 ownerUserId 能查到设备”以及“为什么停用设备后就推不到”。
     */
    private static final class InMemoryAppDeviceRepository {

        private final Map<String, DemoAppDevice> data = new HashMap<>();

        private DemoAppDevice getByDeviceCode(String deviceCode) {
            return data.get(deviceCode);
        }

        private void save(DemoAppDevice device) {
            data.put(device.getDeviceCode(), device);
        }

        private List<DemoAppDevice> listActiveDevices(Long ownerUserId) {
            return data.values().stream()
                .filter(device -> Objects.equals(device.getOwnerUserId(), ownerUserId))
                .sorted(Comparator.comparing(DemoAppDevice::getLastActiveTime).reversed())
                .collect(Collectors.toList());
        }
    }

    /**
     * 消息中心仓库 Demo。
     *
     * <p>生产代码里在发 Push 之前会先落一条 App 消息，并且同一 reminderId 重试时复用同一条消息。
     * 这样做的价值是：
     * 1. 用户在消息中心里看到的是一条稳定的业务消息，而不是每次重试都多一条
     * 2. 后台审计时可以看到“同一条消息从失败到成功”的演进轨迹
     */
    private static final class InMemoryAppMessageRepository {

        private final Map<String, DemoAppMessage> data = new HashMap<>();

        private DemoAppMessage createOrReuse(String dedupKey, Long businessId, Long ownerUserId, Long memberId,
            String title, String content) {
            DemoAppMessage existingMessage = data.get(dedupKey);
            if (existingMessage != null) {
                return existingMessage;
            }

            DemoAppMessage message = new DemoAppMessage(dedupKey, businessId, ownerUserId, memberId, title, content);
            data.put(dedupKey, message);
            return message;
        }

        private DemoAppMessage getByDedupKey(String dedupKey) {
            return data.get(dedupKey);
        }
    }

    /**
     * 固定时钟。
     *
     * <p>测试里固定当前时间，是为了让“哪些提醒算到点、哪些提醒未到点”稳定可复现。
     */
    private static final class DemoClock {

        private final LocalDateTime now;

        private DemoClock(LocalDateTime now) {
            this.now = now;
        }

        private LocalDateTime now() {
            return now;
        }
    }

    /**
     * 设备注册请求。
     */
    private static final class RegisterDeviceRequest {

        private final String deviceCode;

        private final Long ownerUserId;

        private final String pushPlatform;

        private final String deviceToken;

        private RegisterDeviceRequest(String deviceCode, Long ownerUserId, String pushPlatform, String deviceToken) {
            this.deviceCode = deviceCode;
            this.ownerUserId = ownerUserId;
            this.pushPlatform = pushPlatform;
            this.deviceToken = deviceToken;
        }

        private String getDeviceCode() {
            return deviceCode;
        }

        private Long getOwnerUserId() {
            return ownerUserId;
        }

        private String getPushPlatform() {
            return pushPlatform;
        }

        private String getDeviceToken() {
            return deviceToken;
        }
    }

    /**
     * 用药提醒标准通知载荷。
     *
     * <p>这个对象对应生产代码里的 `MedicationReminderNotice`。
     * 只保留发送链路关心的字段，避免发送器直接依赖提醒数据库实体。
     */
    private static final class MedicationReminderNoticeDemo {

        private final Long reminderId;

        private final Long ownerUserId;

        private final Long memberId;

        private final String memberName;

        private final String drugName;

        private final LocalDateTime scheduledTime;

        private final String title;

        private final String content;

        private MedicationReminderNoticeDemo(Long reminderId, Long ownerUserId, Long memberId, String memberName,
            String drugName, LocalDateTime scheduledTime, String title, String content) {
            this.reminderId = reminderId;
            this.ownerUserId = ownerUserId;
            this.memberId = memberId;
            this.memberName = memberName;
            this.drugName = drugName;
            this.scheduledTime = scheduledTime;
            this.title = title;
            this.content = content;
        }

        private Long getReminderId() {
            return reminderId;
        }

        private Long getOwnerUserId() {
            return ownerUserId;
        }

        private Long getMemberId() {
            return memberId;
        }

        private String getMemberName() {
            return memberName;
        }

        private String getDrugName() {
            return drugName;
        }

        private LocalDateTime getScheduledTime() {
            return scheduledTime;
        }

        private String getTitle() {
            return title;
        }

        private String getContent() {
            return content;
        }
    }

    /**
     * 发送器接口。
     *
     * <p>保留接口这一层，是为了让你看到“应用服务”和“具体发送通道”之间的边界。
     * 以后想换成短信、站内信或者企业微信，都可以替换实现，而不需要改调度和派发逻辑。
     */
    private interface MedicationReminderNotifierDemo {

        MedicationReminderNotifyResultDemo notify(MedicationReminderNoticeDemo notice);
    }

    /**
     * 发送结果。
     */
    private static final class MedicationReminderNotifyResultDemo {

        private final boolean success;

        private final String channel;

        private final String message;

        private MedicationReminderNotifyResultDemo(boolean success, String channel, String message) {
            this.success = success;
            this.channel = channel;
            this.message = message;
        }

        private static MedicationReminderNotifyResultDemo success(String channel, String message) {
            return new MedicationReminderNotifyResultDemo(true, channel, message);
        }

        private static MedicationReminderNotifyResultDemo failed(String channel, String message) {
            return new MedicationReminderNotifyResultDemo(false, channel, message);
        }

        private boolean isSuccess() {
            return success;
        }

        private String getChannel() {
            return channel;
        }

        private String getMessage() {
            return message;
        }
    }

    /**
     * 设备实体 Demo。
     */
    private static final class DemoAppDevice {

        private final String deviceCode;

        private Long ownerUserId;

        private String pushPlatform;

        private String deviceToken;

        private DeviceStatus status;

        private LocalDateTime lastActiveTime;

        private DemoAppDevice(String deviceCode, Long ownerUserId, String pushPlatform, String deviceToken,
            DeviceStatus status, LocalDateTime lastActiveTime) {
            this.deviceCode = deviceCode;
            this.ownerUserId = ownerUserId;
            this.pushPlatform = pushPlatform;
            this.deviceToken = deviceToken;
            this.status = status;
            this.lastActiveTime = lastActiveTime;
        }

        private void refreshBinding(Long ownerUserId, String pushPlatform, String deviceToken, LocalDateTime now) {
            this.ownerUserId = ownerUserId;
            this.pushPlatform = pushPlatform;
            this.deviceToken = deviceToken;
            this.status = DeviceStatus.ENABLE;
            this.lastActiveTime = now;
        }

        private void disable(LocalDateTime now) {
            this.status = DeviceStatus.DISABLE;
            this.lastActiveTime = now;
        }

        private String getDeviceCode() {
            return deviceCode;
        }

        private Long getOwnerUserId() {
            return ownerUserId;
        }

        private String getPushPlatform() {
            return pushPlatform;
        }

        private String getDeviceToken() {
            return deviceToken;
        }

        private DeviceStatus getStatus() {
            return status;
        }

        private LocalDateTime getLastActiveTime() {
            return lastActiveTime;
        }
    }

    /**
     * 提醒实体 Demo。
     *
     * <p>这里保留了两套状态，是为了和生产表结构保持一致：
     * 1. reminderStatus 表示“用户是否已处理这条提醒”
     * 2. notifyStatus 表示“系统是否已经把这条提醒发出去”
     */
    private static final class DemoMedicationReminder {

        private final Long reminderId;

        private final Long ownerUserId;

        private final Long memberId;

        private final String memberName;

        private final String drugName;

        private final LocalDateTime scheduledTime;

        private ReminderBusinessStatus reminderStatus;

        private ReminderNotifyStatus notifyStatus;

        private int notifyRetryCount;

        private String notifyFailReason;

        private LocalDateTime notifyTime;

        private DemoMedicationReminder(Long reminderId, Long ownerUserId, Long memberId, String memberName,
            String drugName, LocalDateTime scheduledTime, ReminderBusinessStatus reminderStatus,
            ReminderNotifyStatus notifyStatus, int notifyRetryCount) {
            this.reminderId = reminderId;
            this.ownerUserId = ownerUserId;
            this.memberId = memberId;
            this.memberName = memberName;
            this.drugName = drugName;
            this.scheduledTime = scheduledTime;
            this.reminderStatus = reminderStatus;
            this.notifyStatus = notifyStatus;
            this.notifyRetryCount = notifyRetryCount;
        }

        private static DemoMedicationReminder createPending(Long reminderId, Long ownerUserId, Long memberId,
            String memberName, String drugName, LocalDateTime scheduledTime) {
            return new DemoMedicationReminder(reminderId, ownerUserId, memberId, memberName, drugName,
                scheduledTime, ReminderBusinessStatus.PENDING, ReminderNotifyStatus.PENDING, 0);
        }

        private static DemoMedicationReminder createFailed(Long reminderId, Long ownerUserId, Long memberId,
            String memberName, String drugName, LocalDateTime scheduledTime, int retryCount) {
            return new DemoMedicationReminder(reminderId, ownerUserId, memberId, memberName, drugName,
                scheduledTime, ReminderBusinessStatus.PENDING, ReminderNotifyStatus.FAILED, retryCount);
        }

        private void markNotifySuccess(LocalDateTime now) {
            this.notifyStatus = ReminderNotifyStatus.SUCCESS;
            this.notifyTime = now;
            this.notifyFailReason = null;
        }

        private void markNotifyFailed(LocalDateTime now, String failReason) {
            this.notifyStatus = ReminderNotifyStatus.FAILED;
            this.notifyTime = now;
            this.notifyRetryCount++;
            this.notifyFailReason = failReason;
        }

        private Long getReminderId() {
            return reminderId;
        }

        private Long getOwnerUserId() {
            return ownerUserId;
        }

        private Long getMemberId() {
            return memberId;
        }

        private String getMemberName() {
            return memberName;
        }

        private String getDrugName() {
            return drugName;
        }

        private LocalDateTime getScheduledTime() {
            return scheduledTime;
        }

        private ReminderBusinessStatus getReminderStatus() {
            return reminderStatus;
        }

        private ReminderNotifyStatus getNotifyStatus() {
            return notifyStatus;
        }

        private int getNotifyRetryCount() {
            return notifyRetryCount;
        }

        private String getNotifyFailReason() {
            return notifyFailReason;
        }
    }

    /**
     * App 消息实体 Demo。
     *
     * <p>这个对象用于观察“重试时是否复用同一条消息”。
     */
    private static final class DemoAppMessage {

        private final String dedupKey;

        private final Long businessId;

        private final Long ownerUserId;

        private final Long memberId;

        private final String title;

        private final String content;

        private boolean sendSuccess;

        private String sendChannel;

        private String sendMessage;

        private LocalDateTime sendTime;

        private DemoAppMessage(String dedupKey, Long businessId, Long ownerUserId, Long memberId, String title,
            String content) {
            this.dedupKey = dedupKey;
            this.businessId = businessId;
            this.ownerUserId = ownerUserId;
            this.memberId = memberId;
            this.title = title;
            this.content = content;
        }

        private void markSuccess(String channel, String message, LocalDateTime now) {
            this.sendSuccess = true;
            this.sendChannel = channel;
            this.sendMessage = message;
            this.sendTime = now;
        }

        private void markFailed(String channel, String message, LocalDateTime now) {
            this.sendSuccess = false;
            this.sendChannel = channel;
            this.sendMessage = message;
            this.sendTime = now;
        }

        private boolean isSendSuccess() {
            return sendSuccess;
        }
    }

    /**
     * Push 实际发送记录。
     *
     * <p>保留这份记录，是为了在断言时直观看到“哪条提醒最终发给了哪个 deviceToken”。
     */
    private static final class PushSendRecord {

        private final Long reminderId;

        private final Long ownerUserId;

        private final String deviceCode;

        private final String pushPlatform;

        private final String deviceToken;

        private final String title;

        private final String content;

        private PushSendRecord(Long reminderId, Long ownerUserId, String deviceCode, String pushPlatform,
            String deviceToken, String title, String content) {
            this.reminderId = reminderId;
            this.ownerUserId = ownerUserId;
            this.deviceCode = deviceCode;
            this.pushPlatform = pushPlatform;
            this.deviceToken = deviceToken;
            this.title = title;
            this.content = content;
        }
    }

    private enum ReminderBusinessStatus {
        PENDING
    }

    private enum ReminderNotifyStatus {
        PENDING,
        SUCCESS,
        FAILED
    }

    private enum DeviceStatus {
        ENABLE,
        DISABLE
    }

    private static boolean isNotBlank(String text) {
        return text != null && !text.trim().isEmpty();
    }
}
