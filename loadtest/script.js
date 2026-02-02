import http from 'k6/http';
import { check, sleep } from 'k6';

// 1. 配置压测策略
export const options = {
    stages: [
        { duration: '30s', target: 10 }, // 热身：30秒内缓慢增加到 10 个并发
        { duration: '1m', target: 50 },  // 施压：1分钟内增加到 50 个并发
        { duration: '30s', target: 0 },  // 冷却：30秒内降回 0
    ],
    // 设定阈值：如果 P95 延迟超过 1s 或错误率超过 1%，则 CI 判定为失败
    thresholds: {
        http_req_duration: ['p(95)<1000'],
        http_req_failed: ['rate<0.01'],
    },
};

export default function () {
    // 2. 模拟业务请求
    // 注意：这里用 stock.local，确保你本地 hosts 已配置
    const res = http.get('http://frontend.local/db-test'); // 或者你的业务接口

    // 3. 断言检查
    check(res, {
        'status is 200': (r) => r.status === 200,
    });

    // 4. 思考时间 (Think Time)：模拟用户停顿 1秒
    sleep(1);
}