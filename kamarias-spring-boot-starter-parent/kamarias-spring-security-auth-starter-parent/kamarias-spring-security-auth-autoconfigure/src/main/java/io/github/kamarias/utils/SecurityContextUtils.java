package io.github.kamarias.utils;

import io.github.kamarias.uuid.UuidObject;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * springSecurity 安全工具类
 * @author wangyuxing@gogpay.cn
 * @date 2023/5/24 15:21
 */
public class SecurityContextUtils {

    public static <T extends UuidObject> T getLoginUser() {
        return (T) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

}
