package com.hxl.interceptor;

import io.grpc.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.interceptor.GrpcGlobalClientInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import io.seata.core.context.RootContext;

/**
 * gRPC 全局客户端拦截器：实现 HTTP Header 向 gRPC Metadata 的维度转换与接力
 */
@Slf4j
@Configuration
@GrpcGlobalClientInterceptor // ⭐️ 自动将其注册为全局拦截器
public class GrpcContextInterceptor implements ClientInterceptor {

    // 定义你要透传的业务 Header 的 Key (比如 Authorization 或自定义的 X-User-Id)
    // Metadata.Key 必须指定类型，ASCII 字符串使用 Metadata.ASCII_STRING_MARSHALLER
    private static final Metadata.Key<String> AUTH_HEADER_KEY =
            Metadata.Key.of("Authorization", Metadata.ASCII_STRING_MARSHALLER);

    @Override
    public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(
            MethodDescriptor<ReqT, RespT> method, CallOptions callOptions, Channel next) {

        return new ForwardingClientCall.SimpleForwardingClientCall<ReqT, RespT>(next.newCall(method, callOptions)) {
            @Override
            public void start(Listener<RespT> responseListener, Metadata headers) {
                // 1. 获取当前微服务的 HTTP 请求上下文
                ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                if (attributes != null) {
                    HttpServletRequest request = attributes.getRequest();
                    String authHeader = request.getHeader("Authorization");

                    // 1.1. 将 HTTP Header 塞入 gRPC 的 Metadata 中
                    if (authHeader != null) {
                        headers.put(AUTH_HEADER_KEY, authHeader);
                    }
                }

                // 🌟 2. 获取 Seata 的全局事务 XID，并塞入 gRPC 协议头
                String xid = RootContext.getXID();
                if (xid != null) {
                    // RootContext.KEY_XID 的值其实就是 "TX_XID"
                    Metadata.Key<String> xidKey = Metadata.Key.of(RootContext.KEY_XID, Metadata.ASCII_STRING_MARSHALLER);
                    headers.put(xidKey, xid);
                    log.info("成功将 Seata XID 挂载到 gRPC 链路: " + xid);
                }

                // 3. 继续执行原有的调用链
                super.start(responseListener, headers);
            }
        };
    }
}
