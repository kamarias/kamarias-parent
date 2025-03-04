package io.github.kamarias.handler;

import com.alibaba.fastjson2.JSON;
import io.github.kamarias.dto.ResultDTO;
import io.github.kamarias.exception.CustomException;
import io.github.kamarias.utils.TokenUtils;
import io.github.kamarias.utils.http.ServletUtils;
import io.github.kamarias.utils.string.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author wangyuxing@gogpay.cn
 * @date 2023/5/23 15:09
 */
@ConditionalOnMissingBean(LogoutSuccessHandler.class)
public class DefaultLogoutSuccessHandler implements LogoutSuccessHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultLogoutSuccessHandler.class);


    private final TokenUtils tokenUtils;

    public DefaultLogoutSuccessHandler(TokenUtils tokenUtils) {
        this.tokenUtils = tokenUtils;
    }

    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        try {
            tokenUtils.deleteToken();
        } catch (CustomException e) {

        }

        renderString(response, "{\"code\": 200, \"msg\": \"退出登录成功\"}");
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
