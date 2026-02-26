package com.hxl.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

// 在 marketing-activity 服务的 Controller 中
@RestController
@RequestMapping("/api/marketing")
public class ActivityController {

    @PostMapping("/issue-coupon")
    public String issueCoupon(@RequestParam("userId") String userId) {
        // 模拟业务处理
        return "成功为用户 " + userId + " 发放了一张 100 元满减券！";
    }
}
