package com.hxl.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

import java.util.Objects;

@Configuration
public class RateLimitConfig {

    public KeyResolver apiKeyResolver() {
        return exchange -> {
            // 获取请求方的 IP 地址
            // 注意：如果是本地 localhost 测试，IP 可能是 0:0:0:0:0:0:0:1 或 127.0.0.1
            String ip = Objects.requireNonNull(exchange.getRequest().getRemoteAddress())
                    .getAddress().getHostAddress();
            return Mono.just(ip);
        };
    }
}
