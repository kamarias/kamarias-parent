package io.github.kamarias.utils;


import io.github.kamarias.bean.LoginObject;

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
    public <T extends LoginObject> T getLoginUser(Class<T> loginUserClass) {
        return tokenUtils.analyzeToken(loginUserClass);
    }

    /**
     * 获取登录的用户信息
     *
     * @param str 密钥
     * @param <T> 继承UuidObject的泛型
     * @return 返回登录的用户
     */
    public <T extends LoginObject> T getLoginUser(String str, Class<T> loginUserClass) {
        return (T) tokenUtils.analyzeToken(str, loginUserClass);
    }

}
