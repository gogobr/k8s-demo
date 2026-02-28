package com.hxl.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

//@Component
public class AuthFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        // 获取当前请求的 path
        String path = exchange.getRequest().getURI().getPath();

        // 放行登录接口和公开接口
        if (path.contains("/login") || path.contains("/public")) {
            return chain.filter(exchange);
        }

        // 获取请求头中的 token
        String token = exchange.getRequest().getHeaders().getFirst("token");
        if ("123".equals(token)) {
            return chain.filter(exchange);
        }

        // 拦截
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
    }

    @Override
    public int getOrder() {
        return -1;
    }
}
