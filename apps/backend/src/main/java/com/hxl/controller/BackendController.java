package com.hxl.controller;

import com.hxl.feign.MarketingClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/backend")
public class BackendController {

    @Autowired
    private MarketingClient marketingClient;

    @GetMapping("/do-action")
    public String doAction() {
        // 就像调用本地方法一样，发起微服务间远程 RPC 调用！
        // LoadBalancer 会自动去 Nacos 查 marketing-activity 的 IP，并执行轮询负载均衡
        String result = marketingClient.issueCoupon("U_8888");
        return "Backend 执行完毕。下游响应: " + result;
    }
}
