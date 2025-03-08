package io.github.kamarias.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.kamarias.bean.LoginObject;
import io.github.kamarias.exception.SecurityException;
import io.github.kamarias.properties.TokenProperties;
import io.jsonwebtoken.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

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
     * redis操作模板
     */
    private final StringRedisTemplate stringRedisTemplate;

    /**
     * json序列化
     */
    private final ObjectMapper objectMapper;

    /**
     * token 配置
     */
    private final TokenProperties tokenProperties;

    public TokenUtils(StringRedisTemplate stringRedisTemplate, ObjectMapper objectMapper, TokenProperties tokenProperties) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.objectMapper = objectMapper;
        this.tokenProperties = tokenProperties;
    }

    /**
     * 创建token
     *
     * @param o   登录对象实体
     * @param <T> 继承LoginObject对象
     * @return 返回值
     */
    public <T extends LoginObject> String createToken(T o) {
        if (tokenProperties.isEnableRedis()) {
//            return createJwtToken(o);
        }

        if (tokenProperties.isSinglePoint()) {
            Assert.notNull(o.getId(), "登录对象Id不能为空");
            return createSingleRedisToken(o);
        }
        return createRedisToken(o);
    }

    /**
     * 移除token
     *
     * @return 移除结果
     */
    public boolean deleteToken() {
        if (tokenProperties.isSinglePoint()) {
            return removeSingleRedisToken();
        }
        return removeRedisToken();
    }


    /**
     * 移除token
     *
     * @param str 移除的令牌
     * @return 移除结果
     */
    public boolean deleteToken(String str) {
        if (tokenProperties.isSinglePoint()) {
            return removeSingleRedisToken(str);
        }
        return removeRedisToken(str);
    }

    /**
     * 解析token
     *
     * @param tClass 需要序列化的类
     * @param <T>    继承 LoginObject 的类
     * @return 解析结果
     */
    public <T extends LoginObject> T analyzeToken(Class<T> tClass) {
        if (tokenProperties.isSinglePoint()) {
            return analyzeSingleRedisToken(tClass);
        }
        return analyzeRedisToken(tClass);
    }

    /**
     * 解析token
     *
     * @param str    令牌
     * @param tClass 需要序列化的类
     * @param <T>    继承 LoginObject 的类
     * @return 解析结果
     */
    public <T extends LoginObject> T analyzeToken(String str, Class<T> tClass) {
        if (tokenProperties.isSinglePoint()) {
            return analyzeSingleRedisToken(str, tClass);
        }
        return analyzeRedisToken(str, tClass);
    }


    /**
     * 创建单点token
     *
     * @param o   生成的对象
     * @param <T> 继承UuidObject 的类
     * @return 返回生成key
     */
    private <T extends LoginObject> String createSingleRedisToken(T o) {
        // 生成 jwt 密钥
        JwtBuilder jwtBuilder = Jwts.builder();
        String jwtPassword = jwtBuilder
                .setHeaderParam("typ", "JWT")
                .setHeaderParam("alg", "HS256")
                .setId(o.getUuid())
                .signWith(SignatureAlgorithm.HS256, tokenProperties.getSecret())
                .compact();
        // 存入缓存中
        ValueOperations<String, String> opsForValue = stringRedisTemplate.opsForValue();

        opsForValue.set(loginKeyGenerator(o.getUuid()), toJsonString(o), tokenProperties.getExpireTime(), tokenProperties.getUnit());
        // 设置登录绑定的uuid
        stringRedisTemplate.opsForValue().set(singleKeyGenerator(o.getId()), o.getUuid(), tokenProperties.getExpireTime(), tokenProperties.getUnit());
        return jwtPassword;
    }

    /**
     * 删除token令牌
     *
     * @return 移除令牌结果
     */
    private boolean removeSingleRedisToken() {
        LoginObject o = analyzeSingleRedisToken(LoginObject.class);
        return stringRedisTemplate.delete(loginKeyGenerator(o.getUuid())) && stringRedisTemplate.delete(singleKeyGenerator(o.getId()));
    }

    /**
     * 删除token令牌
     *
     * @param str 令牌
     * @return 移除令牌结果
     */
    private boolean removeSingleRedisToken(String str) {
        LoginObject o = analyzeSingleRedisToken(str, LoginObject.class);
        return stringRedisTemplate.delete(loginKeyGenerator(o.getUuid())) && stringRedisTemplate.delete(singleKeyGenerator(o.getId()));
    }

    /**
     * 解析 redis
     *
     * @param tClass 序列化的类
     * @param <T>    继承UuidObject的解析对象
     * @return 解析成功的对象
     */
    private <T extends LoginObject> T analyzeSingleRedisToken(Class<T> tClass) {
        T token = analyzeRedisToken(tClass);
        String uuid = stringRedisTemplate.opsForValue().get(singleKeyGenerator(token.getId()));
        if (token.getUuid().equals(uuid)) {
            return token;
        }
        LOGGER.warn("当前登录账号已在其它地方登录");
        // 移除之前登录的缓存key
        stringRedisTemplate.delete(loginKeyGenerator(token.getUuid()));
        throw new SecurityException("当前登录账号已在其它地方登录");
    }

    /**
     * 解析 redis
     *
     * @param str    密钥
     * @param tClass 序列化的类
     * @param <T>    继承UuidObject的解析对象
     * @return 解析成功的对象
     */
    private <T extends LoginObject> T analyzeSingleRedisToken(String str, Class<T> tClass) {
        T token = analyzeRedisToken(str, tClass);
        String uuid = stringRedisTemplate.opsForValue().get(singleKeyGenerator(token.getId()));
        if (token.getUuid().equals(uuid)) {
            return token;
        }
        LOGGER.warn("当前登录账号已在其它地方登录");
        // 移除之前登录的缓存key
        stringRedisTemplate.delete(loginKeyGenerator(token.getUuid()));
        throw new SecurityException("当前登录账号已在其它地方登录");
    }

    /**
     * 创建 token
     *
     * @param o   生成的对象
     * @param <T> 继承UuidObject 的类
     * @return 返回生成key
     */
    private <T extends LoginObject> String createRedisToken(T o) {
        // 生成 jwt 密钥
        JwtBuilder jwtBuilder = Jwts.builder();
        String jwtPassword = jwtBuilder
                .setHeaderParam("typ", "JWT")
                .setHeaderParam("alg", "HS256")
                .setId(o.getUuid())
                .signWith(SignatureAlgorithm.HS256, tokenProperties.getSecret())
                .compact();
        String loginInfo = toJsonString(o);
        stringRedisTemplate.opsForValue().set(loginKeyGenerator(o.getUuid()), loginInfo, tokenProperties.getExpireTime(), tokenProperties.getUnit());
        return jwtPassword;
    }

    /**
     * 删除token令牌
     *
     * @return 移除令牌结果
     */
    private boolean removeRedisToken() {
        LoginObject o = analyzeRedisToken(LoginObject.class);
        return stringRedisTemplate.delete(loginKeyGenerator(o.getUuid()));
    }

    /**
     * 删除token令牌
     *
     * @param str 令牌
     * @return 移除令牌结果
     */
    private boolean removeRedisToken(String str) {
        LoginObject o = analyzeRedisToken(str, LoginObject.class);
        return stringRedisTemplate.delete(loginKeyGenerator(o.getUuid()));
    }

    /**
     * 解析 redis
     *
     * @param tClass 序列化的类
     * @param <T>    继承UuidObject的解析对象
     * @return 解析成功的对象
     */
    private <T extends LoginObject> T analyzeRedisToken(Class<T> tClass) {
        String header = getHttpServletRequest().getHeader(tokenProperties.getAuthHeader());
        if (StringUtils.isEmpty(header)) {
            LOGGER.info("登录令牌已过期：授权请求头为空");
            throw new SecurityException("登录令牌已过期");
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
    private <T extends LoginObject> T analyzeRedisToken(String str, Class<T> tClass) {
        String redisUuid;
        try {
            redisUuid = Jwts.parser()
                    .setSigningKey(tokenProperties.getSecret())
                    .parseClaimsJws(removePrefix(str))
                    .getBody().getId();
        } catch (Exception e) {
            // 移除redis 的缓存
            LOGGER.info("登录令牌已过期：登录令牌解析错误");
            throw new SecurityException("登录令牌错误或已失效");
        }
        redisUuid = loginKeyGenerator(redisUuid);
        RedisOperations<String, String> operations =
                stringRedisTemplate.opsForValue().getOperations();
        Long expireTime = operations.getExpire(redisUuid);

        // redis key 过期返回值
        long expiredValue = -2;
        if (Objects.nonNull(expireTime) && expireTime == expiredValue) {
            LOGGER.info("登录令牌已过期：令牌过期");
            throw new SecurityException("登录令牌已过期");
        }
        String loginObjectJson = stringRedisTemplate.opsForValue().get(redisUuid);
        T t = toObject(loginObjectJson, tClass);
        if (Objects.isNull(t)) {
            LOGGER.info("登录令牌已过期：令牌过期");
            throw new SecurityException("登录令牌已过期");
        }
        if (tokenProperties.getRefreshDate() >= expireTime) {
            // 续期token
            stringRedisTemplate.delete(redisUuid);
            stringRedisTemplate.opsForValue().set(redisUuid, toJsonString(t), tokenProperties.getExpireTime(), tokenProperties.getUnit());
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
    private <T extends LoginObject> String createJwtToken(T o) {
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
    private <T extends LoginObject> T analyzeJwtToken(Class<T> tClass) {
        String header = getHttpServletRequest().getHeader(tokenProperties.getAuthHeader());
        if (StringUtils.isEmpty(header)) {
            LOGGER.info("登录令牌已过期：授权请求头为空");
            throw new SecurityException("登录令牌已过期");
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
    private <T extends LoginObject> T analyzeJwtToken(String str, Class<T> tClass) {
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
        T t = BeanUtils.instantiateClass(tClass);
        BeanUtils.copyProperties(o, t);
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
        // 响应头中添加新的请求头
        getHttpServletResponse().addHeader("refresh_token", token);
    }

    /**
     * 过期令牌处理
     */
    private void removeToken() {
        // 响应头中添加新的请求头
        getHttpServletResponse().addHeader("refresh_token", "");
        throw new SecurityException("登录令牌错误或已失效");
    }

    /**
     * 生成登录key
     *
     * @param uuid 登录uuid
     * @return 返回结果
     */
    private String loginKeyGenerator(String uuid) {
        return this.LOGIN_KEY + uuid;
    }

    /**
     * 生成单一key
     *
     * @param singleKey 单一key
     * @return 返回结果
     */
    private String singleKeyGenerator(String singleKey) {
        return this.LOGIN_KEY + SINGLE_KEY + singleKey;
    }


    /**
     * 将对象转换为JSON字符串
     *
     * @param o   登录对象
     * @param <T> 继承LoginObject的解析对象
     * @return 返回转换后的json对象
     */
    private <T extends LoginObject> String toJsonString(T o) {
        try {
            return objectMapper.writeValueAsString(o);
        } catch (JsonProcessingException e) {
            LOGGER.error("Failed to convert the login object to JSON", e);
            throw new SecurityException(e);
        }
    }


    /**
     * 将JSON字符串转换为对象
     *
     * @param json   登录对象Json
     * @param tClass 继承LoginObject的解析对象
     * @param <T>    继承LoginObject的解析对象
     * @return 返回泛型对象
     */
    private <T extends LoginObject> T toObject(String json, Class<T> tClass) {
        try {
            return objectMapper.readValue(json, tClass);
        } catch (JsonProcessingException e) {
            LOGGER.error("Failed to convert the JSON to login object", e);
            throw new SecurityException(e);
        }
    }


    /**
     * 获取http servlet request 请求对象
     *
     * @return 获取请求对象
     */
    private HttpServletRequest getHttpServletRequest() {
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return requestAttributes.getRequest();
    }

    /**
     * 获取http servlet response 响应对象
     *
     * @return 获取请求响应对象
     */
    private HttpServletResponse getHttpServletResponse() {
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return requestAttributes.getResponse();
    }

}
