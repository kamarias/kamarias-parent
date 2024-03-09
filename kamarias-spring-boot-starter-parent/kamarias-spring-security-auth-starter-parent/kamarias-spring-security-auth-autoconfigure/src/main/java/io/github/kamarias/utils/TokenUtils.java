package io.github.kamarias.utils;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import io.github.kamarias.cache.RedisCache;
import io.github.kamarias.exception.CustomException;
import io.github.kamarias.properties.TokenProperties;
import io.github.kamarias.utils.http.ServletUtils;
import io.github.kamarias.uuid.LoginObject;
import io.jsonwebtoken.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.util.Assert;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.Objects;

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
     * 登录key前缀
     */
    private final String LOGIN_KEY = "login_cache::";

    /**
     * 登录用户Id唯一值
     */
    private final String SINGLE_KEY = "single_key::";

    /**
     * redis 操作对象
     */
    private final RedisCache redisCache;

    /**
     * token 配置
     */
    private final TokenProperties tokenProperties;

    /**
     * redis key 过期返回值
     */
    private final long EXPIRED_VALUE = -2;

    public TokenUtils(RedisCache redisCache, TokenProperties tokenProperties) {
        this.redisCache = redisCache;
        this.tokenProperties = tokenProperties;
    }

    /**
     * 创建token
     * @param o 登录对象实体
     * @return 返回值
     * @param <T> 继承LoginObject对象
     */
    public <T extends LoginObject> String createToken(T o) {
        if (tokenProperties.isSinglePoint()){
            Assert.notNull(o.getId(), "登录对象Id不能为空");
            return createSingleRedisToken(o);
        }
        return createRedisToken(o);
    }

    /**
     * 移除token
     * @return 移除结果
     */
    public boolean deleteToken() {
        if (tokenProperties.isSinglePoint()){
            return removeSingleRedisToken();
        }
        return removeRedisToken();
    }


    /**
     * 移除token
     * @param str 移除的令牌
     * @return 移除结果
     */
    public boolean deleteToken(String str) {
        if (tokenProperties.isSinglePoint()){
            return removeSingleRedisToken(str);
        }
        return removeRedisToken(str);
    }

    /**
     * 解析token
     * @param tClass 需要序列化的类
     * @return 解析结果
     * @param <T> 继承 LoginObject 的类
     */
    public <T extends LoginObject> T analyzeToken(Class<T> tClass) {
        if (tokenProperties.isSinglePoint()){
            return analyzeSingleRedisToken(tClass);
        }
        return analyzeRedisToken(tClass);
    }

    /**
     * 解析token
     * @param str 令牌
     * @param tClass 需要序列化的类
     * @return 解析结果
     * @param <T> 继承 LoginObject 的类
     */
    public <T extends LoginObject> T analyzeToken(String str, Class<T> tClass) {
        if (tokenProperties.isSinglePoint()){
            return analyzeSingleRedisToken(str, tClass);
        }
        return analyzeRedisToken(str, tClass);
    }


    /**
     * 创建单点token
     *
     * @param o         生成的对象
     * @param <T>       继承UuidObject 的类
     * @return 返回生成key
     */
    public <T extends LoginObject> String createSingleRedisToken(T o) {
        // 生成 jwt 密钥
        JwtBuilder jwtBuilder = Jwts.builder();
        String jwtPassword = jwtBuilder
                .setHeaderParam("typ", "JWT")
                .setHeaderParam("alg", "HS256")
                .setId(o.getUuid())
                .signWith(SignatureAlgorithm.HS256, tokenProperties.getSecret())
                .compact();
        // 存入缓存中
        redisCache.setCacheObject(loginKeyGenerator(o.getUuid()) , o, tokenProperties.getExpireTime(), tokenProperties.getUnit());
        // 设置登录绑定的uuid
        redisCache.setCacheObject(singleKeyGenerator(o.getId()), o.getUuid(), tokenProperties.getExpireTime(), tokenProperties.getUnit());
        return jwtPassword;
    }

    /**
     * 删除token令牌
     *
     * @return 移除令牌结果
     */
    public boolean removeSingleRedisToken() {
        LoginObject o = analyzeSingleRedisToken(LoginObject.class);
        return redisCache.deleteObject(loginKeyGenerator(o.getUuid())) && redisCache.deleteObject(singleKeyGenerator(o.getId()));
    }

    /**
     * 删除token令牌
     *
     * @param str       令牌
     * @return 移除令牌结果
     */
    public boolean removeSingleRedisToken(String str) {
        LoginObject o = analyzeSingleRedisToken(str, LoginObject.class);
        return redisCache.deleteObject(loginKeyGenerator(o.getUuid())) && redisCache.deleteObject(singleKeyGenerator(o.getId()));
    }

    /**
     * 解析 redis
     *
     * @param tClass    序列化的类
     * @param <T>       继承UuidObject的解析对象
     * @return 解析成功的对象
     */
    public <T extends LoginObject> T analyzeSingleRedisToken(Class<T> tClass) {
        T token = analyzeRedisToken(tClass);
        String uuid = redisCache.getCacheObject(singleKeyGenerator(token.getId()));
        if (token.getUuid().equals(uuid)) {
            return token;
        }
        LOGGER.warn("当前登录账号已在其它地方登录");
        // 移除之前登录的缓存key
        redisCache.deleteObject(loginKeyGenerator(token.getUuid()));
        throw new CustomException("当前登录账号已在其它地方登录");
    }

    /**
     * 解析 redis
     *
     * @param str       密钥
     * @param tClass    序列化的类
     * @param <T>       继承UuidObject的解析对象
     * @return 解析成功的对象
     */
    public <T extends LoginObject> T analyzeSingleRedisToken(String str, Class<T> tClass) {
        T token = analyzeRedisToken(str, tClass);
        String uuid = redisCache.getCacheObject(singleKeyGenerator(token.getId()));
        if (token.getUuid().equals(uuid)) {
            return token;
        }
        LOGGER.warn("当前登录账号已在其它地方登录");
        // 移除之前登录的缓存key
        redisCache.deleteObject(loginKeyGenerator(token.getUuid()));
        throw new CustomException("当前登录账号已在其它地方登录");
    }

    /**
     * 创建 token
     *
     * @param o   生成的对象
     * @param <T> 继承UuidObject 的类
     * @return 返回生成key
     */
    public <T extends LoginObject> String createRedisToken(T o) {
        // 生成 jwt 密钥
        JwtBuilder jwtBuilder = Jwts.builder();
        String jwtPassword = jwtBuilder
                .setHeaderParam("typ", "JWT")
                .setHeaderParam("alg", "HS256")
                .setId(o.getUuid())
                .signWith(SignatureAlgorithm.HS256, tokenProperties.getSecret())
                .compact();
        // 存入缓存中
        redisCache.setCacheObject(loginKeyGenerator(o.getUuid()), o, tokenProperties.getExpireTime(), tokenProperties.getUnit());
        return jwtPassword;
    }

    /**
     * 删除token令牌
     *
     * @return 移除令牌结果
     */
    public boolean removeRedisToken() {
        LoginObject o = analyzeRedisToken(LoginObject.class);
        return redisCache.deleteObject(loginKeyGenerator(o.getUuid()));
    }

    /**
     * 删除token令牌
     *
     * @param str 令牌
     * @return 移除令牌结果
     */
    public boolean removeRedisToken(String str) {
        LoginObject o = analyzeRedisToken(str, LoginObject.class);
        return redisCache.deleteObject(loginKeyGenerator(o.getUuid()));
    }

    /**
     * 解析 redis
     *
     * @param tClass 序列化的类
     * @param <T>    继承UuidObject的解析对象
     * @return 解析成功的对象
     */
    public <T extends LoginObject> T analyzeRedisToken(Class<T> tClass) {
        HttpServletRequest request = ServletUtils.getRequest();
        String header = request.getHeader(tokenProperties.getAuthHeader());
        if (StringUtils.isEmpty(header)) {
            LOGGER.info("登录令牌已过期：授权请求头为空");
            throw new CustomException("登录令牌已过期");
        }
        return analyzeRedisToken(header, tClass);
    }

    /**
     * 解析 redis
     *
     * @param str    密钥
     * @param tClass 序列化的类
     * @param <T>    继承UuidObject的解析对象
     * @return 解析成功的对象
     */
    public <T extends LoginObject> T analyzeRedisToken(String str, Class<T> tClass) {
        String redisUuid;
        try {
            redisUuid = Jwts.parser()
                    .setSigningKey(tokenProperties.getSecret())
                    .parseClaimsJws(removePrefix(str))
                    .getBody().getId();
        } catch (Exception e) {
            // 移除redis 的缓存
            LOGGER.info("登录令牌已过期：登录令牌解析错误");
            throw new CustomException("登录令牌错误或已失效");
        }
        redisUuid = loginKeyGenerator(redisUuid);
        long expireTime = redisCache.getExpireTime(redisUuid);
        if (expireTime == EXPIRED_VALUE) {
            LOGGER.info("登录令牌已过期：令牌过期");
            throw new CustomException("登录令牌已过期");
        }
        T t = redisCache.getCacheObject(redisUuid);
        if (Objects.isNull(t)) {
            LOGGER.info("登录令牌已过期：令牌过期");
            throw new CustomException("登录令牌已过期");
        }
        if (tokenProperties.getRefreshDate() >= expireTime) {
            // 续期token
            redisCache.deleteObject(redisUuid);
            redisCache.setCacheObject(redisUuid, t, tokenProperties.getExpireTime(), tokenProperties.getUnit());
        }
        return t;
    }

    /**
     * 生成token
     *
     * @param o   继承UuidObject 的实体
     * @param <T> 继承UuidObject的泛型
     * @return 生成加密的加密字符串
     */
    public <T extends LoginObject> String createJwtToken(T o) {
        JwtBuilder jwtBuilder = Jwts.builder();
        return jwtBuilder
                .setHeaderParam("typ", "JWT")
                .setHeaderParam("alg", "HS256")
                .claim("user", o)
                .setExpiration(new Date(System.currentTimeMillis() + tokenProperties.getExpiredMilliseconds()))
                .setId(o.getUuid())
                .signWith(SignatureAlgorithm.HS256, tokenProperties.getSecret())
                .compact();
    }

    /**
     * 解析token
     *
     * @param tClass 需要解析的对象
     * @param <T>    继承 LoginObject 的泛型
     * @return 解析成功的对象
     */
    public <T extends LoginObject> T analyzeJwtToken(Class<T> tClass) {
        HttpServletRequest request = ServletUtils.getRequest();
        String header = request.getHeader(tokenProperties.getAuthHeader());
        if (StringUtils.isEmpty(header)) {
            LOGGER.info("登录令牌已过期：授权请求头为空");
            throw new CustomException("登录令牌已过期");
        }
        return analyzeJwtToken(header, tClass);
    }

    /**
     * 解析token
     *
     * @param str    token值
     * @param tClass 需要解析的对象
     * @param <T>    继承 LoginObject 的泛型
     * @return 解析成功的对象
     */
    public <T extends LoginObject> T analyzeJwtToken(String str, Class<T> tClass) {
        Jws<Claims> claimsJws = null;
        try {
            claimsJws = Jwts.parser()
                    .setSigningKey(tokenProperties.getSecret())
                    .parseClaimsJws(removePrefix(str));
        } catch (Exception e) {
            removeToken();
        }
        assert claimsJws != null;
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
    public String removePrefix(String token) {
        if (token.contains(tokenProperties.getAuthHeaderPrefix())) {
            return token.replace(tokenProperties.getAuthHeaderPrefix(), "");
        }
        throw new IllegalArgumentException("请求前缀异常");
    }

    /**
     * 令牌是否需要续期
     */
    private boolean expiredCheck(Date expireDate) {
        Date date = new Date(System.currentTimeMillis() + tokenProperties.getRefreshMilliseconds());
        return !date.before(expireDate);
    }

    /**
     * 续期令牌
     */
    private <T extends LoginObject> void renewalToken(T t) {
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

    /**
     * 生成登录key
     * @param uuid 登录uuid
     * @return 返回结果
     */
    private String loginKeyGenerator(String uuid){
        return this.LOGIN_KEY + uuid;
    }

    /**
     * 生成单一key
     * @param singleKey 单一key
     * @return 返回结果
     */
    private String singleKeyGenerator(String singleKey){
        return this.LOGIN_KEY + SINGLE_KEY + singleKey;
    }

}
