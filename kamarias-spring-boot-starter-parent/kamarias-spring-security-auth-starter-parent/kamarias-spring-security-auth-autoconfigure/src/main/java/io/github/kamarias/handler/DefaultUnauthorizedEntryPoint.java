package io.github.kamarias.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 默认的
 *
 * @author wangyuxing@gogpay.cn
 * @date 2023/5/23 14:44
 */
@ConditionalOnMissingBean(AuthenticationEntryPoint.class)
public class DefaultUnauthorizedEntryPoint implements AuthenticationEntryPoint {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultUnauthorizedEntryPoint.class);

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) {
        LOGGER.warn("请求路径：{}，权限不允许", request.getRequestURI());
        String error = "请求路径：" + request.getRequestURI() + "，认证失败";
        renderString(response,
                "{\"code\": 401, \"msg\": " + "\"" + error + "\"}");
    }


    /**
     * 将字符串渲染到客户端
     *
     * @param response 渲染对象
     * @param string   待渲染的字符串
     */
    private static void renderString(HttpServletResponse response, String string) {
        try {
            response.setStatus(200);
            response.setContentType("application/json");
            response.setCharacterEncoding("utf-8");
            response.getWriter().print(string);
        } catch (IOException e) {
            LOGGER.error("rewrite response request data failed", e);
        }
    }
}
