package com.ming.mymcpserver.converter;

import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.stereotype.Component;

@Component
public class MyJwtAuthenticationConverter extends JwtAuthenticationConverter {

    public static final String CHI_NAME = "chiName";

    public MyJwtAuthenticationConverter(MyJwtGrantedAuthenticationConverter myJwtGrantedAuthenticationConverter) {
        super();
        // oauth2 token 抓到就知道
        setPrincipalClaimName(CHI_NAME);
        setJwtGrantedAuthoritiesConverter(myJwtGrantedAuthenticationConverter);
    }
}
