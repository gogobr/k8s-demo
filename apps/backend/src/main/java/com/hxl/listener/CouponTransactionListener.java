package com.hxl.listener;

import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQTransactionListener;
import org.apache.rocketmq.spring.core.RocketMQLocalTransactionListener;
import org.apache.rocketmq.spring.core.RocketMQLocalTransactionState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RocketMQTransactionListener
public class CouponTransactionListener implements RocketMQLocalTransactionListener {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RocketMQLocalTransactionState executeLocalTransaction(Message msg, Object arg) {
        String transactionId = (String) msg.getHeaders().get("TRANSACTION_ID");
        String userId = (String) arg;

        log.info("========== [MQ 回调：执行本地事务] ==========");
        log.info("事务ID: {}, 业务参数: {}", transactionId, userId);

        try {
            // 1. 执行核心业务 (比如：税务流水入库)
            jdbcTemplate.update("INSERT INTO local_tax_record (user_id, amount) VALUES (?, ?)", userId, 500);

            // 2. 写入本地事务回查日志 (极其关键：和业务SQL在同一个事务里一起提交！)
            jdbcTemplate.update("INSERT INTO mq_transaction_log (transaction_id, status, create_time) VALUES (?, ?, NOW())", transactionId, 1);

            // 🌟 3. 故意制造一个宕机异常来测试极端情况！(测试完记得注掉)
             System.out.println(1 / 0);

            log.info("本地事务执行成功，通知 MQ 提交半消息！");
            return RocketMQLocalTransactionState.COMMIT; // 提交后，下游立刻能收到消息

        } catch (Exception e) {
            log.error("本地事务执行失败，通知 MQ 回滚半消息！", e);
            return RocketMQLocalTransactionState.ROLLBACK; // 回滚后，MQ 直接丢弃消息，下游永远收不到
        }
    }

    @Override
    public RocketMQLocalTransactionState checkLocalTransaction(Message msg) {
        String transactionId = (String) msg.getHeaders().get("TRANSACTION_ID");
        log.warn("========== [MQ 监工来对账了：回查本地事务状态] ==========");
        log.warn("正在核对事务ID: {}", transactionId);

        // 去本地日志表里查一下，这个事务到底成功没有？
        Integer status = null;
        try {
            status = jdbcTemplate.queryForObject(
                    "SELECT status FROM mq_transaction_log WHERE transaction_id = ?",
                    Integer.class,
                    transactionId
            );
        } catch (Exception e) {
            // 查不到说明本地事务回滚了，压根没插进去数据
            log.warn("回查结果：未找到事务记录，告诉 MQ 回滚消息！");
            return RocketMQLocalTransactionState.ROLLBACK;
        }

        if (status != null && status == 1) {
            log.info("回查结果：查到事务成功记录，告诉 MQ 提交消息！");
            return RocketMQLocalTransactionState.COMMIT;
        }

        return RocketMQLocalTransactionState.UNKNOWN;
    }
}
