package com.hxl.tcc;

import io.seata.rm.tcc.api.BusinessActionContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Slf4j
@Component
public class CouponTccActionImpl implements CouponTccAction{

    @Override
    public boolean tryIssue(String userId) {
        // 在真实大厂中，这里执行：UPDATE coupon_stock SET frozen = frozen + 1, available = available - 1 WHERE ...
        log.info("============== [TCC 一阶段 - Try] ==============");
        log.info("1. 检查库存...");
        log.info("2. 冻结用户 [{}] 的发券资源，此时券还没真正到账！", userId);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean confirm(BusinessActionContext actionContext) {
        // 修复参数丢失：从 ActionContext 的 ActionContext 参数 Map 中安全提取
        Map<String, Object> contextMap = actionContext.getActionContext();
        String userId = contextMap != null ? String.valueOf(contextMap.get("userId")) : "未知用户";
        String xid = actionContext.getXid();

        // 真实大厂中：UPDATE coupon_stock SET frozen = frozen - 1, used = used + 1 WHERE ...
        log.info("============== [TCC 二阶段 - Confirm] ==============");
        log.info("全局事务 XID: [{}] 全线成功！", xid);
        log.info("真正执行发券落库，扣除用户 [{}] 刚才冻结的资源。", userId);
        return true;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean cancel(BusinessActionContext actionContext) {
        Map<String, Object> contextMap = actionContext.getActionContext();
        String userId = contextMap != null ? String.valueOf(contextMap.get("userId")) : "未知用户";
        String xid = actionContext.getXid();

        // 真实大厂中：UPDATE coupon_stock SET frozen = frozen - 1, available = available + 1 WHERE ...
        log.info("============== [TCC 二阶段 - Cancel] ==============");
        log.info("全局事务 XID: [{}] 发生异常！", xid);
        log.info("执行逆向回滚：解冻用户 [{}] 的资源，把名额还给库存！", userId);
        return true;
    }
}
