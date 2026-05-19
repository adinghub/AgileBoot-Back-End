# 体检报告移除 OCR 能力设计

## 1. 背景

历史版本里，体检报告模块保留过 `/app/reports/{reportId}/ocr` 入口和一组 OCR 相关配置、客户端实现。
但当前业务主链路已经切换为：

1. 文本型 PDF / 文本文件：服务端直接抽取文本后做规则结构化和 AI 结构化增强。
2. 图片报告：优先由外部大模型视觉能力直接解析。
3. 无法自动解析时：回到人工补录。

继续保留 OCR 会带来两个问题：

1. 接口和日志会让前端、测试、运营误以为系统仍提供真实 OCR 能力。
2. 云 OCR SDK、本地 OCR 命令配置会增加部署复杂度，但实际收益已经很低。

## 2. 目标

1. 代码层面不再保留 OCR 客户端、OCR 配置和 `/ocr` 接口。
2. 对外统一使用“重新解析报告文件”语义，而不是“触发 OCR”语义。
3. 保留现有报告解析主链路，不影响文本文件解析、AI 视觉解析和人工补录。
4. 保持数据库兼容，不在本次改动里删除 `ocr_*` 字段，避免额外数据迁移风险。

## 3. 方案

### 3.1 接口调整

移除旧入口：

`POST /app/reports/{reportId}/ocr`

新增统一入口：

`POST /app/reports/{reportId}/parse`

返回值直接复用 `HealthReportDTO`，让前端拿到最新的报告详情状态，而不是再维护一套 OCR 专用返回结构。

### 3.2 解析策略调整

1. PDF：继续优先使用 PDFBox 抽取文本。
2. 图片：不再调用云 OCR SDK，也不再执行本地 OCR 命令。
3. 图片解析仅在 AI 视觉模型可用时自动解析，否则保留为空并引导人工补录。
4. 文本增强里保留“补充文本”概念，但不再把它定义为 OCR 文本。

### 3.3 代码清理

本次移除以下内容：

1. `HealthReportOcrProperties`
2. `HealthReportOcrClient`
3. `HealthReportOcrClientFactory`
4. `AliyunHealthReportOcrClient`
5. `CommandHealthReportOcrClient`
6. `HealthReportOcrResultDTO`
7. Maven 中阿里云 OCR SDK 依赖

## 4. 兼容策略

1. `ocr_status`、`ocr_text_snapshot`、`ocr_time` 字段暂时保留，继续作为报告解析过程中的兼容状态字段使用。
2. 这样可以避免数据库迁移、历史数据修复、前端详情字段联动在同一批变更里叠加。
3. 后续如果确认前端和后台都不再依赖这些字段，再单独安排数据库清理。

## 5. 风险说明

1. 若当前环境没有启用 AI 视觉模型，图片报告将无法自动抽取结构化指标。
2. 扫描版 PDF 在没有文本层时，也不会再自动识别内容。
3. 因此本次改动的业务口径必须同步给前端和测试：图片类报告的自动解析能力现在只依赖 AI 视觉，不依赖 OCR。

## 6. 本次不做

1. 不删除数据库中的 `ocr_*` 字段。
2. 不清理所有历史设计文档中零散出现的 “ocrStatus/ocrTextSnapshot” 描述。
3. 不新增“扫描 PDF 转图片后再做 AI 视觉解析”的能力。
