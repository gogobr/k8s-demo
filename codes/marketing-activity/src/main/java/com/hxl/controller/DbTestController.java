package com.hxl.controller;

import com.hxl.entity.VisitLog;
import com.hxl.mapper.VisitLogMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

@RestController
public class DbTestController {

    @Autowired
    private VisitLogMapper visitLogMapper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    // 定义缓存 Key
    private static final String COUNT_CACHE_KEY = "visit:count";

    // 注入我们在 Nacos 里写的自定义配置
    @Value("${app.feature.enable-log:false}")
    private boolean enableLog;

    @GetMapping("/config-check")
    public String checkConfig() {
        return "Current Log Switch: " + enableLog;
    }

    @GetMapping("/db-test")
    public String dbTest(HttpServletRequest request) {
        // 1. 写入
        VisitLog log = new VisitLog(request.getRemoteAddr());
        visitLogMapper.insert(log);

        // 2. 读取
        long count = visitLogMapper.count();

        return "MyBatis Connected! Total visits: " + count;
    }

    // 写接口：没有 readOnly=true，应该走 Master
    @GetMapping("/write")
    @Transactional // 默认是读写事务 -> MASTER
    public String write(HttpServletRequest request) {
        VisitLog log = new VisitLog(request.getRemoteAddr());
        visitLogMapper.insert(log);

        // 【关键】双写一致性策略：Cache Aside Pattern
        // 先写 DB，成功后删除缓存
        redisTemplate.delete(COUNT_CACHE_KEY);

        return "Written to DB & Cache Evicted!";
    }

    // 读接口：加了 readOnly=true，应该走 Slave
    @GetMapping("/read")
    @Transactional(readOnly = true) // 只读事务 -> SLAVE
    public String read() {
        // 1. 先查 Redis
        Object cachedCount = redisTemplate.opsForValue().get(COUNT_CACHE_KEY);
        if (cachedCount != null) {
            return "Read from REDIS cache! Count: " + cachedCount;
        }

        // 2. 缓存没有，查数据库 (此时会自动路由到 Slave)
        long count = visitLogMapper.count();

        // 3. 写入 Redis，设置过期时间 (防止冷数据长期占用，例如 10 分钟)
        redisTemplate.opsForValue().set(COUNT_CACHE_KEY, count, 10, TimeUnit.MINUTES);

        return "Read from DB (Slave) & Cached! Count: " + count;
    }
}
