package com.hxl.grpc;

import com.hxl.grpc.marketing.*;
import com.hxl.tcc.CouponTccAction;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

@Slf4j
@GrpcService
public class MarketingTccGrpcServiceImpl extends MarketingTccServiceGrpc.MarketingTccServiceImplBase {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private CouponTccAction couponTccAction;

    @Override
    public void issueCoupon(IssueCouponTccRequest request, StreamObserver<IssueCouponTccResponse> responseObserver) {
        String userId = request.getUserId();
        log.info("收到 gRPC 请求，准备走 TCC 模式发券，用户ID: {}", userId);

        // 核心点：这里只调用 tryIssue！
        // 如果 try 成功，Seata 会在全局事务结束时，自动通过 RPC 回调 confirm 方法。
        // 如果上游抛了异常，Seata 会自动通过 RPC 回调 cancel 方法。
        boolean trySuccess = couponTccAction.tryIssue(userId);

        if (trySuccess) {
            IssueCouponTccResponse response = IssueCouponTccResponse.newBuilder()
                    .setCode(200)
                    .setMessage("TCC Try 冻结成功！")
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } else {
            responseObserver.onError(new RuntimeException("TCC Try 阶段冻结资源失败！"));
        }
    }
}
