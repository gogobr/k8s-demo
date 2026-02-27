package com.hxl.tcc;

import io.seata.rm.tcc.api.BusinessActionContext;
import io.seata.rm.tcc.api.BusinessActionContextParameter;
import io.seata.rm.tcc.api.LocalTCC;
import io.seata.rm.tcc.api.TwoPhaseBusinessAction;

/**
 * 营销发券的 TCC 核心接口
 * @LocalTCC 注解告诉 Seata，这是一个 TCC 参与者
 */
@LocalTCC
public interface CouponTccAction {

    /**
     * 一阶段：Try (资源预留/冻结)
     * @TwoPhaseBusinessAction 定义了二阶段的确认和回滚方法名
     * @BusinessActionContextParameter 将参数塞入上下文中，方便二阶段获取
     */
    @TwoPhaseBusinessAction(name = "issueCouponTcc", commitMethod = "confirm", rollbackMethod = "cancel")
    boolean tryIssue(@BusinessActionContextParameter(paramName = "userId") String userId);

    /**
     * 二阶段：Confirm (确认执行)
     * 只有所有微服务的 Try 都成功，TC 才会调这个方法
     */
    boolean confirm(BusinessActionContext actionContext);

    /**
     * 二阶段：Cancel (回滚补偿)
     * 只要有任何一个微服务报错，TC 就会调这个方法来解冻资源
     */
    boolean cancel(BusinessActionContext actionContext);
}
