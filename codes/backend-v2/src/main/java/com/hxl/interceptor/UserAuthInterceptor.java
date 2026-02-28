package com.hxl.interceptor;

import com.hxl.context.UserContextHolder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class UserAuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 1. 获取网关透传过来的 X-User-Id
        String userId = request.getHeader("X-User-Id");

        // 2. 如果有值，就存入当前线程上下文
        if (userId != null && !userId.isEmpty()) {
            UserContextHolder.setUserId(userId);
        }

        // 3. 返回 true 放行请求
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        // 🔥 【致命重点】请求结束后，必须清空 ThreadLocal！
        // Tomcat 处理请求使用的是线程池。如果不清理，下一个复用该线程的请求就会读到上一个人的数据（串号/越权漏洞）！
        UserContextHolder.clear();
    }
}
