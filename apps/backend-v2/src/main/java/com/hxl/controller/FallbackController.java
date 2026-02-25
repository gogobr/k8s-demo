package com.hxl.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@RestController
public class FallbackController {

    /**
     * 当后端服务超时或宕机时，网关会把请求转发到这里
     */
    @RequestMapping("/fallback")
    public Mono<Map<String, Object>> fallback() {
        Map<String, Object> response = new HashMap<>();
        response.put("code", 503);
        response.put("message", "系统开小差了，请稍后再试 (网关触发熔断降级兜底)");
        return Mono.just(response);
    }
}
