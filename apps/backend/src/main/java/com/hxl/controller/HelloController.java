package com.hxl.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.net.InetAddress;
import java.nio.charset.StandardCharsets;

@Slf4j
@RestController
public class HelloController {

    @Autowired
    private RestTemplate restTemplate;

    @GetMapping(value = "/hello" , produces = "text/plain; charset=UTF-8")
    public String hello() {
        // 👇 修改点：尝试获取环境变量 GREETING_MESSAGE
        // 如果 K8s 没传这个变量，就默认显示 "Hello K8s (Default)"
        String message = System.getenv().getOrDefault("GREETING_MESSAGE", "Hello K8s (Default)");

        try {
            return new String(message.getBytes(), StandardCharsets.UTF_8) + "! I am running on: " + InetAddress.getLocalHost().getHostAddress();
        } catch (Exception e) {
            return message + "! (Unknown Host)";
        }
    }

    // 👇 2. 模拟“前端”调用“后端”的接口
    @GetMapping("/chain")
    public String chain() {
        log.info("🔗 Chain start: I am the Frontend!");

        // 这里利用 K8s 的服务发现机制！
        // 我们假设稍后会部署一个叫 k8s-backend 的服务
        String backendUrl = "http://k8s-backend/hello";

        String response = restTemplate.getForObject(backendUrl, String.class);

        return "Frontend calls Backend, result is: [" + response + "]";
    }
}
