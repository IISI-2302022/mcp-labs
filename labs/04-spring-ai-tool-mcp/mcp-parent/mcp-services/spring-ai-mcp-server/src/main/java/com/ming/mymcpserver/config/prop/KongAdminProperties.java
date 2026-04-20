package com.ming.mymcpserver.config.prop;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties("my.kong")
public class KongAdminProperties {
    private String baseUrl = "http://127.0.0.1:8001";
    private String token = "Kong@1234";
}
