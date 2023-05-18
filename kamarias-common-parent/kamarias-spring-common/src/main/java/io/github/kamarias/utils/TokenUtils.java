package io.github.kamarias.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import io.github.kamarias.cache.RedisCache;
import io.github.kamarias.exception.CustomException;
import io.github.kamarias.properties.TokenProperties;
import io.github.kamarias.utils.http.ServletUtils;
import io.github.kamarias.uuid.UuidObject;
import io.jsonwebtoken.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;

/**
 * token工具类
 *
 * @author wangyuxing@gogpay.cn
 * @date 2023/2/21 14:53
 */
@Configuration(proxyBeanMethods = false)
@Import({TokenProperties.class})
public class TokenUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(TokenUtils.class);

    /**
     * redis 操作对象
     */
    private final RedisCache redisCache;

    /**
     * token 配置
     */
    private final TokenProperties tokenProperties;

    public TokenUtils(RedisCache redisCache, TokenProperties tokenProperties) {
        this.redisCache = redisCache;
        this.tokenProperties = tokenProperties;
    }


    public <T extends UuidObject> String createRedisToken(T o) {
        // 生成 jwt 密钥
        JwtBuilder jwtBuilder = Jwts.builder();
        String jwtPassword = jwtBuilder
                .setHeaderParam("typ", "JWT")
                .setHeaderParam("alg", "HS256")
                .setId(o.getUuid())
                .signWith(SignatureAlgorithm.HS256, tokenProperties.getSecret())
                .compact();
        o.setExpireTime(tokenProperties.getExpireTime());
        // 存入缓存中
        redisCache.setCacheObject(o.getUuid(), o, tokenProperties.getExpireTime(), tokenProperties.getUnit());
        return jwtPassword;
    }

    public <T extends UuidObject> T analyzeRedisToken(String str, Class<T> tClass) {
        String redisUuid = null;
        try {
            redisUuid = Jwts.parser()
                    .setSigningKey(tokenProperties.getSecret())
                    .parseClaimsJws(removePrefix(str))
                    .getBody().getId();
        }catch (Exception e){
            // 移除redis 的缓存
            //redisCache.deleteObject(redisUuid);
            // 返回错误信息
        }
        T t = redisCache.getCacheObject(redisUuid);

        return t;
    }


    private void postponeToken(String redisKey){

    }


    /**
     * 生成token
     *
     * @param o   继承UuidObject 的实体
     * @param <T> 继承UuidObject的泛型
     * @return 生成加密的加密字符串
     */
    public <T extends UuidObject> String createJwtToken(T o) {
        JwtBuilder jwtBuilder = Jwts.builder();
        String jwtPassword = jwtBuilder
                .setHeaderParam("typ", "JWT")
                .setHeaderParam("alg", "HS256")
                .claim("user", o)
                .setExpiration(new Date(System.currentTimeMillis() + tokenProperties.getExpiredMilliseconds()))
                .setId(o.getUuid())
                .signWith(SignatureAlgorithm.HS256, tokenProperties.getSecret())
                .compact();
        return jwtPassword;
    }

    /**
     * 解析token
     *
     * @param tClass 需要解析的对象
     * @param <T>    继承 UuidObject 的泛型
     * @return 解析成功的对象
     */
    public <T extends UuidObject> T analyzeJwtToken(Class<T> tClass) {
        HttpServletRequest request = ServletUtils.getRequest();
        String header = request.getHeader(tokenProperties.getAuthHeader());
        if (StringUtils.isEmpty(header)) {
            throw new RuntimeException("获取请求头异常");
        }
        return analyzeJwtToken(header, tClass);
    }

    /**
     * 解析token
     *
     * @param str    token值
     * @param tClass 需要解析的对象
     * @param <T>    继承 UuidObject 的泛型
     * @return 解析成功的对象
     */
    public <T extends UuidObject> T analyzeJwtToken(String str, Class<T> tClass) {
        Jws<Claims> claimsJws = null;
        try {
            claimsJws = Jwts.parser()
                    .setSigningKey(tokenProperties.getSecret())
                    .parseClaimsJws(removePrefix(str));
        } catch (Exception e) {
            removeToken();
        }
        Claims body = claimsJws.getBody();
        Date expiration = body.getExpiration();
        Object o = body.get("user");
        T t = JSONObject.parseObject(JSON.toJSONString(o), tClass);
        if (expiredCheck(expiration)) {
            renewalToken(t);
        }
        return t;
    }

    /**
     * 校验请求前缀并移除 token 请求前缀
     *
     * @param token 带前缀的 token
     * @return 返回不带前缀的 token
     */
    private String removePrefix(String token) {
        if (token.indexOf(tokenProperties.getAuthHeaderPrefix()) != -1) {
            return token.replace(tokenProperties.getAuthHeaderPrefix(), "");
        }
        throw new IllegalArgumentException("请求前缀异常");
    }

    /**
     * 令牌是否需要续期
     */
    private boolean expiredCheck(Date expireDate) {
        Date date = new Date(System.currentTimeMillis() + tokenProperties.getRefreshMilliseconds());
        if (date.before(expireDate)) {
            return false;
        }
        return true;
    }

    /**
     * 续期令牌
     */
    private <T extends UuidObject> void renewalToken(T t) {
        // 新token
        String token = createJwtToken(t);
        HttpServletResponse response = ServletUtils.getResponse();
        // 响应头中添加新的请求头
        response.addHeader("refresh_token", token);
    }

    /**
     * 过期令牌处理
     */
    private void removeToken() {
        HttpServletResponse response = ServletUtils.getResponse();
        // 响应头中添加新的请求头
        response.addHeader("refresh_token", "");
        throw new CustomException("登录令牌错误或已失效");
    }

}
