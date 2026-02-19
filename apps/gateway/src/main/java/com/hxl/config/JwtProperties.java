package com.hxl.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    // 对应 YAML 里的 jwt.secret
    private String secret;

    // 对应 YAML 里的 jwt.skip-urls
    // 初始化为一个空列表，防止 Nacos 没配时报空指针异常 (防御性编程)
    private List<String> skipUrls = new ArrayList<>();

    // ⚠️ 必须要有 Getter 和 Setter 方法，Spring 底层是通过 Setter 来注入值的！
    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public List<String> getSkipUrls() {
        return skipUrls;
    }

    public void setSkipUrls(List<String> skipUrls) {
        this.skipUrls = skipUrls;
    }
}
