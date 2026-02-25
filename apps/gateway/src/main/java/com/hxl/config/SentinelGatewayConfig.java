package com.hxl.config;

import com.alibaba.csp.sentinel.adapter.gateway.sc.callback.BlockRequestHandler;
import com.alibaba.csp.sentinel.adapter.gateway.sc.callback.GatewayCallbackManager;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerResponse;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class SentinelGatewayConfig {

    @PostConstruct
    public void initBlockHandler() {
        // 自定义网关流控被触发时的响应
        BlockRequestHandler blockRequestHandler = (exchange, t) -> {
            Map<String, Object> map = new HashMap<>();
            map.put("code", 429);
            map.put("message", "哎呀，人太多了！请稍后再试 (Sentinel 触发流控)");

            return ServerResponse.status(HttpStatus.TOO_MANY_REQUESTS)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(BodyInserters.fromValue(map));
        };

        GatewayCallbackManager.setBlockHandler(blockRequestHandler);
    }
}
