package com.healthtrail.common.core.base;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import java.beans.PropertyEditorSupport;
import java.util.Date;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;

/**
 * Web层通用控制器基类，提供日期参数自动转换和页面跳转等通用能力
 *
 * @author valarchie
 */
@Slf4j
public class BaseController {

    /**
     *
     * 将前台传递过来的日期格式的字符串，自动转化为Date类型
     */
    @InitBinder
    public void initBinder(WebDataBinder binder) {
        // Date 类型转换
        binder.registerCustomEditor(Date.class, new PropertyEditorSupport() {
            @Override
            public void setAsText(String text) {
                // 这里对空字符串做显式兜底，原因是 multipart/form-data 和传统表单提交时，
                // 前端即使原本想表达“未填写日期”，也很容易把 null 传成 ""。
                // 如果这里直接交给 Hutool `parseDate`，它会抛出
                // “Date String must be not blank !”，从而在 Spring 参数绑定阶段提前失败。
                // 改成空串 => null 后：
                // 1. 非必填日期字段可以自然留空；
                // 2. 必填字段仍然会交给后续 `@NotNull` / 业务校验去处理；
                // 3. 其它使用 BaseController 的日期表单也能一起受益。
                if (StrUtil.isBlank(text)) {
                    setValue(null);
                    return;
                }
                setValue(DateUtil.parseDate(text));
            }
        });
    }

    /**
     * 页面跳转
     */
    public String redirect(String url) {
        return StrUtil.format("redirect:{}", url);
    }


}
