package com.ming.mymcpserver.converter;

import lombok.val;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

// 針對 keycloak 來做對應實作
@Component
public class MyJwtGrantedAuthenticationConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

    @Override
    public Collection<GrantedAuthority> convert(Jwt source) {
        val authorities = new ArrayList<GrantedAuthority>();
        extractScopes(source, authorities);
        extractRealmRoles(source, authorities);
        extractClientRoles(source, authorities);
        return Collections.unmodifiableList(authorities);
    }

    /**
     * 從 JWT 的 scope claim 中提取權限，格式為 SCOPE_{scopeName}
     */
    private void extractScopes(Jwt jwt, List<GrantedAuthority> authorities) {
        String scope = jwt.getClaimAsString("scope");
        if (scope == null || scope.isBlank()) {
            return;
        }
        for (String s : scope.split("\\s+")) {
            if (!s.isBlank()) {
                authorities.add(new SimpleGrantedAuthority("SCOPE_" + s));
            }
        }
    }

    /**
     * 從 JWT 的 realm_access.roles 中提取權限，格式為 ROLE_{roleName}
     */
    private void extractRealmRoles(Jwt jwt, List<GrantedAuthority> authorities) {
        final Map<String, Object> realmAccess = jwt.getClaim("realm_access");
        if (realmAccess == null) {
            return;
        }
        val rolesObj = realmAccess.get("roles");
        if (rolesObj instanceof List<?> roles) {
            for (Object role : roles) {
                authorities.add(new SimpleGrantedAuthority("ROLE_" + role));
            }
        }
    }

    /**
     * 從 JWT 的 resource_access.{clientId}.roles 中提取權限，格式為 ROLE_{clientId}_{roleName}
     */
    private void extractClientRoles(Jwt jwt, List<GrantedAuthority> authorities) {
        final Map<String, Object> resourceAccess = jwt.getClaim("resource_access");
        if (resourceAccess == null) {
            return;
        }
        resourceAccess.forEach((clientId, value) -> {
            if (value instanceof Map<?, ?> clientResource) {
                val rolesObj = clientResource.get("roles");
                if (rolesObj instanceof List<?> roles) {
                    for (Object role : roles) {
                        authorities.add(new SimpleGrantedAuthority("ROLE_" + clientId + "_" + role));
                    }
                }
            }
        });
    }
}
