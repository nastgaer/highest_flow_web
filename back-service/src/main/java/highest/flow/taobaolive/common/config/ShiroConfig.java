package highest.flow.taobaolive.common.config;

import highest.flow.taobaolive.sys.oauth2.OAuth2Filter;
import highest.flow.taobaolive.sys.oauth2.OAuth2Realm;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.servlet.Filter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@Configuration
public class ShiroConfig {

    @Bean("securityManager")
    public SecurityManager securityManager(OAuth2Realm oAuth2Realm) {
        DefaultWebSecurityManager securityManager = new DefaultWebSecurityManager();
        securityManager.setRealm(oAuth2Realm);
        securityManager.setRememberMeManager(null);
        return securityManager;
    }

    @Bean("shiroFilter")
    public ShiroFilterFactoryBean shirFilter(SecurityManager securityManager) {
        ShiroFilterFactoryBean shiroFilter = new ShiroFilterFactoryBean();
        shiroFilter.setSecurityManager(securityManager);

        // oauth过滤
        Map<String, Filter> filters = new HashMap<>();
        filters.put("oauth2", new OAuth2Filter());
        shiroFilter.setFilters(filters);

        Map<String, String> filterMap = new LinkedHashMap<>();
        filterMap.put("/v1.0/xsign", "anon");
        filterMap.put("/v1.0/sys/login", "anon");
        filterMap.put("/v1.0/login", "anon");
        filterMap.put("/v1.0/live/upload_image", "anon");
        filterMap.put("/v1.0/tbacc/upload", "anon");
        filterMap.put("/external/ranking/reg", "anon");
        filterMap.put("/**", "oauth2");
        shiroFilter.setFilterChainDefinitionMap(filterMap);

        return shiroFilter;
    }
}
