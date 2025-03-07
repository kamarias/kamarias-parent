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
public class TokenUtilsTwo {

    private static final Logger LOGGER = LoggerFactory.getLogger(TokenUtilsTwo.class);

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

    public TokenUtilsTwo(StringRedisTemplate stringRedisTemplate, ObjectMapper objectMapper, TokenProperties tokenProperties) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.objectMapper = objectMapper;
        this.tokenProperties = tokenProperties;
    }

    public <T extends LoginObject> String createToken(T o) {
        if (tokenProperties.isEnableRedis()) {
            return createJwtToken(o);
        }
        return createRedisToken(o);
    }

    /**
     * 移除token (仅支持 redis 存储时可以移除)
     *
     * @return 移除结果
     */
    public boolean deleteToken() {

        return false;
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

        }
    }

    /**
     * 解析token
     *
     * @param token  令牌
     * @param tClass 需要序列化的类
     * @param <T>    继承 LoginObject 的类
     * @return 解析结果
     */
    public <T extends LoginObject> T analyzeToken(String token, Class<T> tClass) {
        Jws<Claims> claimsJws = Jwts.parser()
                .setSigningKey(tokenProperties.getSecret())
                .parseClaimsJws(token.replace(tokenProperties.getAuthHeaderPrefix(), ""));
        return null;

    }

    /**
     * 创建redis存储的token
     *
     * @param o   需要登录的对象
     * @param <T> 继承登录对象的类
     * @return 返回创建的 token
     */
    private <T extends LoginObject> String createRedisToken(T o) {
        if (tokenProperties.isSinglePoint()) {
            // 清空之前登录的密钥
            stringRedisTemplate.delete(loginKeyGenerator(o.getId()));
        }
        String jwtToken = createJwtToken(o);
        stringRedisTemplate.opsForValue().set(loginKeyGenerator(o.getId()), jwtToken, tokenProperties.getExpireTime(), tokenProperties.getUnit());
        return jwtToken;
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
     * 生成登录key
     *
     * @param uuid 登录uuid
     * @return 返回结果
     */
    private String loginKeyGenerator(String uuid) {
        return this.LOGIN_KEY + uuid;
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
