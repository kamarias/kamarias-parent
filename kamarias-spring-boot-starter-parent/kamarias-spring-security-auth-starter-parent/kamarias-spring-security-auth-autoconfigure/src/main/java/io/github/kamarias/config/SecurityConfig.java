package io.github.kamarias.config;

import io.github.kamarias.filter.DefaultJwtAuthTokenFilter;
import io.github.kamarias.properties.SecurityProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.util.Assert;
import org.springframework.web.filter.CorsFilter;

/**
 * @author wangyuxing@gogpay.cn
 * @date 2023/5/22 10:30
 */
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true)
@Import({SecurityProperties.class})
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private final AuthenticationEntryPoint authenticationEntryPoint;

    private final LogoutSuccessHandler logoutSuccessHandler;

    private final DefaultJwtAuthTokenFilter defaultJwtAuthTokenFilter;

    private final SecurityProperties securityProperties;

    private final CorsFilter corsFilter;

    public SecurityConfig(AuthenticationEntryPoint authenticationEntryPoint,
                          LogoutSuccessHandler logoutSuccessHandler, DefaultJwtAuthTokenFilter defaultJwtAuthTokenFilter, SecurityProperties securityProperties, CorsFilter corsFilter) {
        this.securityProperties = securityProperties;
        this.corsFilter = corsFilter;
        Assert.notNull(authenticationEntryPoint, "authenticationEntryPoint must not be null");
        Assert.notNull(logoutSuccessHandler, "logoutSuccessHandler must not be null");
        Assert.notNull(defaultJwtAuthTokenFilter, "defaultJwtAuthTokenFilter must not be null");
        this.authenticationEntryPoint = authenticationEntryPoint;
        this.logoutSuccessHandler = logoutSuccessHandler;
        this.defaultJwtAuthTokenFilter = defaultJwtAuthTokenFilter;
    }

    /**
     * 请求过滤器
     *
     * @param http the {@link HttpSecurity} to modify
     * @throws Exception
     */
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // 白名单配置
        http.authorizeRequests()
                .antMatchers(securityProperties.getAnonymous().toArray(new String[securityProperties.getAnonymous().size()]))
                .anonymous();
        // 所有请求都访问
        http.authorizeRequests()
                .antMatchers(HttpMethod.GET, securityProperties.getStaticResPath().toArray(new String[securityProperties.getStaticResPath().size()]))
                .permitAll();
        // 所有请求都需要授权（除以上配置的请求外）
        http.authorizeRequests().anyRequest().authenticated();
        http.headers().frameOptions().disable();
        // 移除session授权
        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
        // CSRF禁用，因为不使用session
        http.csrf().disable();
        // 未认证处理类
        http.exceptionHandling().authenticationEntryPoint(authenticationEntryPoint);
        // 退出登录成功处理器
        http.logout().logoutSuccessHandler(logoutSuccessHandler);
        // 授权过滤器
        http.addFilterAfter(defaultJwtAuthTokenFilter, UsernamePasswordAuthenticationFilter.class);
        http.addFilterAfter(corsFilter, DefaultJwtAuthTokenFilter.class);
    }

    /**
     * 强散列哈希加密实现
     */
    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

}
