package io.github.kamarias.properties;

import com.alibaba.fastjson2.JSONObject;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;

/**
 * Token属性配置类
 * @author wangyuxing@gogpay.cn
 * @date 2023/2/24 17:31
 */
@ConfigurationProperties(prefix = TokenProperties.PREFIX)
public class TokenProperties implements Serializable {

    public final static String PREFIX = "security.token";

    /**
     * 密钥
     */
    private String secret = "S4yHQTz2mvCi";


    /**
     * 授权请求头
     */
    private String authHeader = "Authorization";

    /**
     * 授权请求头前缀
     */
    private String authHeaderPrefix = "Bearer ";

    /**
     * 续期响应头名称
     */
    private String refreshToken = "refresh_token";

    /**
     * 过期时间
     */
    private long expireTime = 30;

    /**
     * 过期时间单位
     */
    private TimeUnit unit = TimeUnit.MINUTES;

    /**
     * 令牌剩余多少时间刷新（单位：秒）
     */
    private long refreshDate = 60;

    /**
     * 获取令牌刷新的毫秒数
     * @return
     */
    public long getRefreshMilliseconds(){
        return TimeUnit.SECONDS.toMillis(this.refreshDate);
    }

    /**
     * 获取过期的毫秒
     */
    public long getExpiredMilliseconds(){
        return this.unit.toMillis(this.expireTime);
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public long getExpireTime() {
        return expireTime;
    }

    public String getAuthHeader() {
        return authHeader;
    }

    public void setAuthHeader(String authHeader) {
        this.authHeader = authHeader;
    }

    public String getAuthHeaderPrefix() {
        return authHeaderPrefix;
    }

    public void setAuthHeaderPrefix(String authHeaderPrefix) {
        this.authHeaderPrefix = authHeaderPrefix;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public long getRefreshDate() {
        return refreshDate;
    }

    public void setRefreshDate(long refreshDate) {
        this.refreshDate = refreshDate;
    }

    public void setExpireTime(long expireTime) {
        this.expireTime = expireTime;
    }

    public TimeUnit getUnit() {
        return unit;
    }

    public void setUnit(TimeUnit unit) {
        this.unit = unit;
    }

    @Override
    public String toString() {
        return JSONObject.toJSONString(this);
    }
}
