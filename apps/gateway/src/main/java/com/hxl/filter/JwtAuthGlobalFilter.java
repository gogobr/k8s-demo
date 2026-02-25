package com.hxl.filter;

import com.hxl.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
public class JwtAuthGlobalFilter implements GlobalFilter, Ordered {

    // 声明配置属性类
    private final JwtProperties jwtProperties;

    // 推荐使用构造器注入 (Spring 官方推荐做法)
    public JwtAuthGlobalFilter(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        String path = request.getURI().getPath();

        // 1. 检查是否在免密白名单中 (比如登录接口)
        if (jwtProperties.getSkipUrls().stream().anyMatch(path::startsWith)) {
            return chain.filter(exchange);
        }

        // 2. 从 HTTP Header 中获取 Authorization
        String authHeader = request.getHeaders().getFirst("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return unauthorized(exchange.getResponse(), "Missing or invalid Authorization header");
        }

        // 提取真正的 token 字符串
        String token = authHeader.substring(7);

        // 3. 校验 Token

        try {
            SecretKey key = Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes());
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            // 4. 鉴权通过：拿到用户 ID，塞入请求头，透传给后端微服务
            String userId = claims.getSubject();

            // 🔥 增加这一行：打印带有业务价值的日志
            log.info("网关鉴权成功，放行请求。当前访问用户 ID: {}", userId);

            ServerHttpRequest mutatedRequest = request.mutate()
                    .header("X-User-Id", userId)
                    .build();

            return chain.filter(exchange.mutate().request(mutatedRequest).build());
        } catch (Exception e) {
            // 解析失败、过期、签名错误等，直接打回
            return unauthorized(exchange.getResponse(), "Invalid or expired JWT token");
        }
    }

    /**
     * 统一构造 401 响应
     */
    private Mono<Void> unauthorized(ServerHttpResponse response, String message) {
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().add("Content-Type", "application/json;charset=UTF-8");
        String body = String.format("{\"code\": 401, \"message\": \"%s\"}", message);
        DataBuffer buffer = response.bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        // 设置为较高的优先级 (-100)，确保在路由转发和限流之前执行鉴权
        return -100;
    }
}
