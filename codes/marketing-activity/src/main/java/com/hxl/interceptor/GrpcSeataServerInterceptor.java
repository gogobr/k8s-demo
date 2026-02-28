package com.hxl.interceptor;

import io.grpc.*;
import io.micrometer.common.util.StringUtils;
import io.seata.core.context.RootContext;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.interceptor.GrpcGlobalServerInterceptor;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@GrpcGlobalServerInterceptor
public class GrpcSeataServerInterceptor implements ServerInterceptor {

    // 必须与客户端透传时的 Key 保持完全一致 (RootContext.KEY_XID = "TX_XID")
    private static final Metadata.Key<String> XID_KEY =
            Metadata.Key.of(RootContext.KEY_XID, Metadata.ASCII_STRING_MARSHALLER);

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> serverCall, Metadata headers, ServerCallHandler<ReqT, RespT> serverCallHandler) {

        // 1. 从 gRPC 请求头中提取客户端传过来的 XID
        String xid = headers.get(XID_KEY);

        // 2. 将后续流程交给真实的业务逻辑处理器
        ServerCall.Listener<ReqT> listener = serverCallHandler.startCall(serverCall, headers);

        // 3. 如果没有 XID，说明不是一个分布式事务请求，直接放行
        if (StringUtils.isBlank(xid)) {
            return listener;
        }

        // 4. 如果有 XID，采用装饰者模式包装 Listener，确保在正确的业务线程中进行 bind 和 unbind
        return new ForwardingServerCallListener.SimpleForwardingServerCallListener<>(listener) {
            @Override
            public void onMessage(ReqT message) {
                try {
                    // 绑定到当前执行业务的线程上下文中
                    RootContext.bind(xid);
                    log.debug("成功绑定 Seata XID 到当前 gRPC 线程: {}", xid);
                    super.onMessage(message);
                } finally {
                    // ⭐️ 极其重要：务必解除绑定，防止线程池复用导致的事务污染
                    RootContext.unbind();
                }
            }

            @Override
            public void onHalfClose() {
                try {
                    RootContext.bind(xid);
                    super.onHalfClose();
                } finally {
                    RootContext.unbind();
                }
            }

            @Override
            public void onCancel() {
                try {
                    RootContext.bind(xid);
                    super.onCancel();
                } finally {
                    RootContext.unbind();
                }
            }

            @Override
            public void onComplete() {
                try {
                    RootContext.bind(xid);
                    super.onComplete();
                } finally {
                    RootContext.unbind();
                }
            }
        };
    }
}
