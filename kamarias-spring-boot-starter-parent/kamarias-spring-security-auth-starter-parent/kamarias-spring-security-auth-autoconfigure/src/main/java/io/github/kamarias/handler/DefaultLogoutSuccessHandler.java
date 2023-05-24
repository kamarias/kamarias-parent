package io.github.kamarias.handler;

import com.alibaba.fastjson.JSON;
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
            tokenUtils.removeRedisToken();
        } catch (CustomException e) {
            LOGGER.error(e.getMessage());
        }
        ServletUtils.renderString(response, JSON.toJSONString(new ResultDTO<>(HttpStatus.OK.value(),
                StringUtils.format("退出成功"))));
    }

}
