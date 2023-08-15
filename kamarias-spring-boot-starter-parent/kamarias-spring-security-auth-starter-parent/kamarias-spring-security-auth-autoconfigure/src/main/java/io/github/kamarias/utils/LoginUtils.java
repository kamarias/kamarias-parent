package io.github.kamarias.utils;

import io.github.kamarias.uuid.LoginObject;

/**
 * 登录工具类，用于获取登录的用户信息
 *
 * @author wangyuxing@gogpay.cn
 * @date 2023/5/24 13:52
 */
public class LoginUtils {

    private final TokenUtils tokenUtils;

    public LoginUtils(TokenUtils tokenUtils) {
        this.tokenUtils = tokenUtils;
    }

    /**
     * 获取登录的用户信息
     *
     * @param <T> 继承UuidObject的泛型
     * @return 返回登录的用户
     */
    public <T extends LoginObject> T getLoginUser() {
        return (T) tokenUtils.analyzeRedisToken(LoginObject.class);
    }

    /**
     * 获取登录的用户信息
     *
     * @param str 密钥
     * @param <T> 继承UuidObject的泛型
     * @return 返回登录的用户
     */
    public <T extends LoginObject> T getLoginUser(String str) {
        return (T) tokenUtils.analyzeRedisToken(str, LoginObject.class);
    }

}
