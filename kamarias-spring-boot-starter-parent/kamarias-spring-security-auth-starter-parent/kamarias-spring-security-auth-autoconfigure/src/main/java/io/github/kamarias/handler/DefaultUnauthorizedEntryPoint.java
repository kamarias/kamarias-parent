package io.github.kamarias.handler;

import com.alibaba.fastjson.JSON;
import io.github.kamarias.dto.ResultDTO;
import io.github.kamarias.utils.http.ServletUtils;
import io.github.kamarias.utils.string.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
        ServletUtils.renderString(response,
                JSON.toJSONString(new ResultDTO<>(HttpStatus.UNAUTHORIZED.value(),
                        StringUtils.format("请求路径：{}，认证失败", request.getRequestURI()))));
    }

}
