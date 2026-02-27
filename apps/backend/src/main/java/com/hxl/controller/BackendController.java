package com.hxl.controller;

import com.hxl.feign.MarketingClient;
import com.hxl.grpc.marketing.IssueCouponRequest;
import com.hxl.grpc.marketing.IssueCouponResponse;
import com.hxl.grpc.marketing.MarketingServiceGrpc;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
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

    // ⭐️ 灵魂注解：直接向 Nacos 里的 marketing-activity 寻址，走 gRPC 协议！
    @GrpcClient("marketing-activity")
    private MarketingServiceGrpc.MarketingServiceBlockingStub marketingGrpcStub;

    @GetMapping("/do-action")
    public String doAction() {
        // 就像调用本地方法一样，发起微服务间远程 RPC 调用！
        // LoadBalancer 会自动去 Nacos 查 marketing-activity 的 IP，并执行轮询负载均衡

        log.info("开始执行业务逻辑do-action...");
        String result = marketingClient.issueCoupon("U_8888");
        return "Backend 执行完毕。下游响应: " + result;
    }

    @GetMapping("/do-grpc")
    public String doGrpcAction() {
        // 1. 构造强类型的请求参数
        IssueCouponRequest request = IssueCouponRequest.newBuilder()
                .setUserId("U_GRPC_999")
                .build();

        // 2. 发起跨进程的二进制调用！(BlockingStub 代表同步阻塞调用)
        IssueCouponResponse response = marketingGrpcStub.issueCoupon(request);

        return "gRPC 响应 -> Code: " + response.getCode() + ", Msg: " + response.getMessage();
    }
}
