package com.hxl.context;

public class UserContextHolder {

    // 使用 InheritableThreadLocal，当父线程（Web线程） new 出子线程时，数据会自动拷贝过去
    private static final ThreadLocal<String> USER_ID_CONTEXT = new InheritableThreadLocal<>();

    public static void setUserId(String userId) {
        USER_ID_CONTEXT.set(userId);
    }

    public static String getUserId() {
        return USER_ID_CONTEXT.get();
    }

    /**
     * 🔥 架构师底线：用完必须清理！
     */
    public static void clear() {
        USER_ID_CONTEXT.remove();
    }
}