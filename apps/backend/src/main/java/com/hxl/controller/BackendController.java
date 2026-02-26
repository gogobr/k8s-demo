package com.hxl.controller;

import com.hxl.feign.MarketingClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/backend")
public class BackendController {

    @Autowired
    private MarketingClient marketingClient;

    @GetMapping("/do-action")
    public String doAction() {
        // 就像调用本地方法一样，发起微服务间远程 RPC 调用！
        // LoadBalancer 会自动去 Nacos 查 marketing-activity 的 IP，并执行轮询负载均衡

        log.info("开始执行业务逻辑do-action...");
        String result = marketingClient.issueCoupon("U_8888");
        return "Backend 执行完毕。下游响应: " + result;
    }
}
