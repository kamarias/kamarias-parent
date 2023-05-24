package io.github.kamarias.filter;

import io.github.kamarias.utils.TokenUtils;
import io.github.kamarias.uuid.UuidObject;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Objects;

/**
 * 每次请求过滤器，一般用于注入上下文信息
 *
 * @author wangyuxing@gogpay.cn
 * @date 2023/5/23 15:47
 */
public class DefaultJwtAuthTokenFilter extends OncePerRequestFilter {

    private final TokenUtils tokenUtils;

    public DefaultJwtAuthTokenFilter(TokenUtils tokenUtils) {
        this.tokenUtils = tokenUtils;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        UuidObject token = null;
        try {
            token = tokenUtils.analyzeRedisToken(UuidObject.class);
        } catch (Exception e) {
        }
        if (Objects.isNull(token)) {
            // 不给授权，直接放行（会被授权拦截器拦截）
            chain.doFilter(request, response);
            return;
        }
        authorize(token);
        chain.doFilter(request, response);
    }

    /**
     * 授权访问
     *
     * @param o   继承UuidObject的对象实体
     * @param <T> 继承UuidObject的泛型
     */
    private <T extends UuidObject> void authorize(T o) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(o, null, null));
    }

}
