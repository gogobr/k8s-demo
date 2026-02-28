package com.hxl.controller;

import com.hxl.context.UserContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

// 在 marketing-activity 服务的 Controller 中
@Slf4j
@RestController
@RequestMapping("/api/marketing")
public class ActivityController {

    @PostMapping("/issue-coupon")
    public String issueCoupon(@RequestParam("userId") String userId) {
        log.info("模拟处理，用户 {} 请求发放优惠券", userId);
        log.info("用户 ID: {}", UserContextHolder.getUserId());
        // 模拟业务处理
        return "成功为用户 " + userId + " 发放了一张 100 元满减券！";
    }
}
