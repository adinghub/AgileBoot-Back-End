package com.healthtrail.api.controller;

import com.healthtrail.common.core.base.BaseController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 订单控制器，提供订单相关占位接口。
 *
 * @author valarchie
 */
@RestController
@RequestMapping("/api/order")
public class OrderController extends BaseController {

    /**
     * 访问首页，提示语
     */
    @RequestMapping("/")
    public String index() {
        return "暂无订单";
    }


}
