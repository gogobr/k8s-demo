package com.hxl.controller;

import com.hxl.feign.MarketingClient;
import com.hxl.grpc.marketing.*;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.apache.rocketmq.client.producer.TransactionSendResult;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/backend")
public class BackendController {

    @Autowired
    private MarketingClient marketingClient;

    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    // 直接向 Nacos 里的 marketing-activity 寻址，走 gRPC 协议！
    @GrpcClient("marketing-activity")
    private MarketingServiceGrpc.MarketingServiceBlockingStub marketingGrpcStub;

    // 直接向 Nacos 里的 marketing-activity 寻址，走 gRPC 协议！
    @GrpcClient("marketing-activity")
    private MarketingTccServiceGrpc.MarketingTccServiceBlockingStub marketingTccGrpcStub;

    @Autowired
    private JdbcTemplate jdbcTemplate; // 随便注入一个 DB 操作工具模拟本地落库

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

    @GetMapping("/do-grpc-tx/error")
    @GlobalTransactional(name = "tax-finance-create-tx", rollbackFor = Exception.class)
    public String doGrpcTxActionError() {

        // 1. 先执行远端 gRPC 调用 (扣库存 / 发券)
        IssueCouponRequest request = IssueCouponRequest.newBuilder().setUserId("U_TX_111").build();
        marketingGrpcStub.issueCoupon(request); // 这步会成功写入下游数据库

        // 2. 模拟本地数据库操作
        jdbcTemplate.update("INSERT INTO local_tax_record (user_id, amount) VALUES (?, ?)", "U_TX_111", 100);

        // 3. 💥 致命一击：模拟本地代码突发宕机或空指针异常！
        log.info("准备抛出异常，测试 Seata 全局回滚...");
        int error = 1 / 0; // 引发 ArithmeticException

        return "不会执行到这里";
    }

    @GetMapping("/do-grpc-tx")
    @GlobalTransactional(name = "tax-finance-create-tx", rollbackFor = Exception.class)
    public String doGrpcTxAction() {

        // 1. 先执行远端 gRPC 调用 (扣库存 / 发券)
        IssueCouponRequest request = IssueCouponRequest.newBuilder().setUserId("U_TX_111").build();
        marketingGrpcStub.issueCoupon(request); // 这步会成功写入下游数据库

        // 2. 模拟本地数据库操作
        jdbcTemplate.update("INSERT INTO local_tax_record (user_id, amount) VALUES (?, ?)", "U_TX_111", 100);

        return "执行正确";
    }

    @GetMapping("/do-grpc-tx-tcc/error")
    @GlobalTransactional(name = "tax-finance-create-tx", rollbackFor = Exception.class)
    public String doGrpcTxTccActionError() {

        // 1. 先执行远端 gRPC 调用 (扣库存 / 发券)
        IssueCouponTccRequest request = IssueCouponTccRequest.newBuilder().setUserId("U_TX_111").build();
        marketingTccGrpcStub.issueCoupon(request); // 这步会成功写入下游数据库

        // 2. 模拟本地数据库操作
        jdbcTemplate.update("INSERT INTO local_tax_record (user_id, amount) VALUES (?, ?)", "U_TX_111", 100);

        // 3. 💥 致命一击：模拟本地代码突发宕机或空指针异常！
        log.info("准备抛出异常，测试 Seata 全局回滚...");
        int error = 1 / 0; // 引发 ArithmeticException

        return "不会执行到这里";
    }


    @GetMapping("/do-mq-action")
    public String doMqAction() {
        String userId = "U_MQ_888";
        log.info("============== [前端请求到达] ==============");
        log.info("1. 核心业务：订单落库成功 (极速完成)...");

        // 🌟 2. 异步解耦：发送 MQ 消息通知下游发券
        // 参数 1：Topic 名称；参数 2：消息体内容
        rocketMQTemplate.convertAndSend("MARKETING_COUPON_TOPIC", userId);

        log.info("2. 消息已投递到 RocketMQ，核心线程立即释放！");

        return "操作成功！优惠券将在稍后发放至您的账户。";
    }

    @GetMapping("/do-tx-mq")
    public String doTxMqAction() {
        String userId = "U_TX_MQ_001";
        // 1. 生成一个全局唯一的事务ID (极其关键，用于后续对账回查)
        String transactionId = UUID.randomUUID().toString();

        log.info("============== [前端请求到达] ==============");
        log.info("准备发送 RocketMQ 半消息，事务ID: {}", transactionId);

        // 2. 构造 Spring Messaging 的标准 Message 对象
        Message<String> message = MessageBuilder.withPayload(userId)
                // 将事务ID塞入消息头
                .setHeader("TRANSACTION_ID", transactionId)
                .build();

        // 3. 致命杀招：发送事务半消息！
        // 参数：Topic名称, 消息体, 传给本地事务的额外参数(此处传userId方便业务处理)
        TransactionSendResult sendResult = rocketMQTemplate.sendMessageInTransaction(
                "MARKETING_COUPON_TOPIC",
                message,
                userId
        );

        log.info("半消息发送结果: {}", sendResult.getSendStatus());
        return "操作已受理！系统正在极速处理中...";
    }
}
