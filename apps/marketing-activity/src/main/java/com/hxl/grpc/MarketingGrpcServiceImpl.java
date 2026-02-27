package com.hxl.grpc;

import com.hxl.grpc.marketing.IssueCouponRequest;
import com.hxl.grpc.marketing.IssueCouponResponse;
import com.hxl.grpc.marketing.MarketingServiceGrpc;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;

@GrpcService
public class MarketingGrpcServiceImpl extends MarketingServiceGrpc.MarketingServiceImplBase {

    @Override
    public void issueCoupon(IssueCouponRequest request, StreamObserver<IssueCouponResponse> responseObserver) {
        String userId = request.getUserId();
        System.out.println("收到 gRPC 请求，用户ID: " + userId);

        // 构造响应对象 (使用 Builder 模式)
        IssueCouponResponse response = IssueCouponResponse.newBuilder()
                .setCode(200)
                .setMessage("gRPC 极速发券成功！券已发至用户: " + userId)
                .build();

        // 1. 把响应发给客户端
        responseObserver.onNext(response);
        // 2. 告诉客户端：本次调用结束
        responseObserver.onCompleted();
    }
}
