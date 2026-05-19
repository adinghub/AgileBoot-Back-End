package com.healthtrail.domain.health.report.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.healthtrail.domain.health.report.config.HealthReportAiStructuredParseProperties;
import com.healthtrail.domain.health.report.llm.HealthReportExternalLlmClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/**
 * `HealthReportAiStructuredParser` 单元测试。
 *
 * <p>这组测试专门覆盖本次修复的两个高风险场景：
 * 1. 模型把思考内容包在 `<think>` 标签里再输出 JSON；
 * 2. 模型返回了 items，但字段名是常见别名而不是我们最初约定的标准 schema。
 *
 * <p>之所以把测试直接压在解析器层，而不是更外层的应用服务，是为了让失败定位更直接：
 * 一旦这里不过，开发人员可以立刻确认问题出在“JSON 收口 / 字段映射”本身，
 * 不需要再绕一圈排查落库和后续摘要逻辑。
 */
class HealthReportAiStructuredParserTest {

    private HealthReportExternalLlmClient externalLlmClient;

    private HealthReportAiStructuredParser parser;

    @BeforeEach
    void setUp() {
        externalLlmClient = Mockito.mock(HealthReportExternalLlmClient.class);
        when(externalLlmClient.isAvailable()).thenReturn(true);

        HealthReportAiStructuredParseProperties properties = new HealthReportAiStructuredParseProperties();
        properties.setEnabled(true);
        properties.setPreferVisionOnImage(true);

        parser = new HealthReportAiStructuredParser(
            externalLlmClient,
            properties,
            new HealthReportStandardIndicatorResolver());
    }

    @Test
    void shouldParseThinkWrappedJsonWithAliasFields() {
        when(externalLlmClient.generateJsonText(anyString(), anyString())).thenReturn("""
            <think>
            先阅读报告，再输出结构化结果。
            </think>
            {
              "reportDate": "2023-07-12",
              "recognizedReportType": "血常规",
              "items": [
                {
                  "name": "嗜酸细胞绝对值",
                  "value": "0.44",
                  "unit": "10^9/L",
                  "referenceRange": "0.02-0.52",
                  "order": 1
                }
              ]
            }
            """);

        ParsedReportResult result = parser.parse("血常规报告文本");

        assertTrue(result.hasItems());
        assertEquals(1, result.getItems().size());
        assertEquals("嗜酸细胞绝对值", result.getItems().get(0).getItemName());
        assertEquals("0.44", result.getItems().get(0).getResultValue());
        assertEquals("10^9/L", result.getItems().get(0).getResultUnit());
        assertEquals("0.02-0.52", result.getItems().get(0).getReferenceText());
        assertEquals(1, result.getItems().get(0).getSort());
        assertNotNull(result.getReportDate());
        assertEquals("血常规", result.getRecognizedReportType());
    }

    @Test
    void shouldParseItemsFromNestedDataObject() {
        when(externalLlmClient.generateJsonText(anyString(), anyString())).thenReturn("""
            {
              "data": {
                "items": [
                  {
                    "itemName": "超敏C反应蛋白",
                    "result": "1.20",
                    "itemUnit": "mg/L",
                    "range": "0-10"
                  }
                ]
              }
            }
            """);

        ParsedReportResult result = parser.parse("超敏CRP报告文本");

        assertTrue(result.hasItems());
        assertEquals("超敏C反应蛋白", result.getItems().get(0).getItemName());
        assertEquals("1.20", result.getItems().get(0).getResultValue());
        assertEquals("mg/L", result.getItems().get(0).getResultUnit());
        assertEquals("0-10", result.getItems().get(0).getReferenceText());
    }

    @Test
    void shouldIgnoreItemsThatStillMissCoreFields() {
        when(externalLlmClient.generateJsonText(anyString(), anyString())).thenReturn("""
            {
              "items": [
                {
                  "unit": "mg/L",
                  "referenceRange": "0-10"
                }
              ]
            }
            """);

        ParsedReportResult result = parser.parse("无效报告文本");

        assertNotNull(result);
        assertTrue(!result.hasItems());
        assertNull(result.getReportDate());
    }
}
