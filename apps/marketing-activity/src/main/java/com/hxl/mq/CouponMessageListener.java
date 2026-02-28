package com.hxl.mq;

import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

/**
 * 营销发券 MQ 消费者
 * @RocketMQMessageListener 定义了要监听的 Topic 和消费者组
 */
@Slf4j
@Component
@RocketMQMessageListener(
        topic = "MARKETING_COUPON_TOPIC",
        consumerGroup = "marketing-activity-consumer-group"
)
public class CouponMessageListener implements RocketMQListener<String> {

    @Override
    public void onMessage(String userId) {
        log.info("============== [MQ 消费者唤醒] ==============");
        log.info("接收到来自 RocketMQ 的消息，开始为用户 [{}] 发放优惠券...", userId);

        try {
            // 模拟业务处理耗时 (这就是削峰填谷的意义，慢慢消费不着急)
            Thread.sleep(1000);

            // 模拟发券落库逻辑
            log.info("用户 [{}] 优惠券发放完毕！", userId);

        } catch (Exception e) {
            log.error("发券失败，触发 RocketMQ 自动重试机制！", e);
            // 架构师细节：抛出异常，RocketMQ 就会捕获并在稍后根据阶梯时间重新投递这条消息！
            throw new RuntimeException("发券异常，要求重试");
        }
    }
}
