package com.hxl.k8sdemo;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
@ComponentScan(basePackages = "com.hxl")
public class K8sDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(K8sDemoApplication.class, args);
    }

    @Bean
    public CommandLineRunner debugRedisConfig(
            @Value("${spring.data.redis.host:unknown}") String redisHost,
            @Value("${spring.data.redis.port:0}") int redisPort) {
        return args -> {
            System.out.println("🔥 [DEBUG] Redis Config from Nacos: " + redisHost + ":" + redisPort);
        };
    }
}
