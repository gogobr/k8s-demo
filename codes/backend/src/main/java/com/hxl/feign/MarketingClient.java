package com.hxl.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

// name 必须严格等于 marketing-activity 在 Nacos 里的 application.name
@FeignClient(name = "marketing-activity")
public interface MarketingClient {

    // 路径和参数必须和提供者的 Controller 严丝合缝
    @PostMapping("/api/marketing/issue-coupon")
    String issueCoupon(@RequestParam("userId") String userId);
}
