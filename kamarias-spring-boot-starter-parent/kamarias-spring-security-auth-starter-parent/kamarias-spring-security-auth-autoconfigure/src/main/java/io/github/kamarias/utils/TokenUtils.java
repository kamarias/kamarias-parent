package io.github.kamarias.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.kamarias.bean.LoginObject;
import io.github.kamarias.exception.SecurityException;
import io.github.kamarias.properties.TokenProperties;
import io.jsonwebtoken.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.StringRedisTemplate;
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

    public <T extends LoginObject> String createToken(T o) {
        if (tokenProperties.isEnableRedis()) {
            return createRedisToken(o);
        }
        return createJwtToken(o);
    }

    /**
     * 移除token (仅支持 redis 存储时可以移除)
     *
     * @return 移除结果
     */
    public <T extends LoginObject> void deleteToken(Class<T> tClass) {
        Jws<Claims> claimsJws = parseToken(getAuthToken());
        Claims body = claimsJws.getBody();
        String jsonString = toJsonString(body.get("user"));
        T loginInfo = toObject(jsonString, tClass);
        // 清理登录的key
        if (tokenProperties.isEnableRedis()) {
            if (tokenProperties.isSinglePoint()) {
                stringRedisTemplate.delete(loginKeyGenerator(loginInfo.getId()));
            }
            if (!tokenProperties.isSinglePoint()) {
                // 清理登录的随机值
                stringRedisTemplate.delete(loginKeyGenerator(loginInfo.getUuid()));
            }
        }
    }

    private String getAuthToken() {
        String token = getHttpServletRequest().getHeader(tokenProperties.getAuthHeader());
        if (StringUtils.isEmpty(token)) {
            LOGGER.info("登录令牌已过期：授权请求头为空");
            throw new SecurityException(401, "请求未授权");
        }
        if (token.contains(tokenProperties.getAuthHeaderPrefix())) {
            return token.replace(tokenProperties.getAuthHeaderPrefix(), "");
        }
        LOGGER.info("请求前缀异常");
        throw new SecurityException(401, "请求未授权");
    }

    /**
     * 解析token
     *
     * @param tClass 需要序列化的类
     * @param <T>    继承 LoginObject 的类
     * @return 解析结果
     */
    public <T extends LoginObject> T analyzeToken(Class<T> tClass) {
        return analyzeToken(getAuthToken(), tClass);
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
        Jws<Claims> claimsJws = parseToken(token);
        Claims body = claimsJws.getBody();
        Object userBody = body.get("user");
        String jsonString = toJsonString(userBody);
        T user = toObject(jsonString, tClass);
        if (tokenProperties.isEnableRedis()) {
            if (tokenProperties.isSinglePoint()) {
                String redisToken = stringRedisTemplate.opsForValue().get(loginKeyGenerator(user.getId()));
                if (redisToken != null) {
                    if (!redisToken.equals(token)) {
                        LOGGER.info("账户在其它地方登录");
                        throw new SecurityException(401, "账户在其它地方登录");
                    }
                } else {
                    LOGGER.info("登录信息已过期...");
                    throw new SecurityException(401, "登录信息已过期");
                }
            } else {
                if (!stringRedisTemplate.hasKey(loginKeyGenerator(user.getUuid()))) {
                    LOGGER.info("登录信息已过期");
                    throw new SecurityException(401, "登录信息已过期");
                }
            }
        }
        renewalTokens(body, user);
        return user;

    }

    /**
     * 生成token
     *
     * @param token 继承UuidObject 的实体
     * @return 生成加密的加密字符串
     */
    private Jws<Claims> parseToken(String token) {
        Jws<Claims> claimsJws;
        try {
            claimsJws = Jwts.parser()
                    .setSigningKey(tokenProperties.getSecret())
                    .parseClaimsJws(token);
        } catch (ExpiredJwtException | UnsupportedJwtException | MalformedJwtException | SignatureException |
                 IllegalArgumentException ex) {
            LOGGER.error("Failed to parse token", ex);
            throw new SecurityException(401, "Failed to parse token");
        }
        return claimsJws;
    }

    /**
     * 续期令牌
     *
     * @param body 上一次的密钥信息
     * @param user 需要续期的用户对象
     * @param <T>  继承登录对象的类
     */
    private <T extends LoginObject> void renewalTokens(Claims body, T user) {
        Date date = new Date(System.currentTimeMillis() + tokenProperties.getRefreshMilliseconds());
        if (date.before(body.getExpiration())) {
            return;
        }
        String token = createToken(user);
        // 响应头中添加新的请求头
        getHttpServletResponse().addHeader("refresh_token", token);
    }


    /**
     * 创建redis存储的token
     *
     * @param o   需要登录的对象
     * @param <T> 继承登录对象的类
     * @return 返回创建的 token
     */
    private <T extends LoginObject> String createRedisToken(T o) {
        String jwtToken = createJwtToken(o);
        // 存储登录key
        if (tokenProperties.isSinglePoint()) {
            // 单浏览器存储用户id
            if (Objects.isNull(o.getId())) {
                LOGGER.info("The user ID cannot be empty when logging in with a single browser, so set the user ID");
                throw new SecurityException(401, "The user ID cannot be empty when logging in with a single browser, so set the user ID");
            }
            stringRedisTemplate.opsForValue().set(loginKeyGenerator(o.getId()), jwtToken, tokenProperties.getExpireTime(), tokenProperties.getUnit());
        }
        if (!tokenProperties.isSinglePoint()) {
            // 多浏览器存储随id
            stringRedisTemplate.opsForValue().set(loginKeyGenerator(o.getUuid()), jwtToken, tokenProperties.getExpireTime(), tokenProperties.getUnit());
        }
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
        return tokenProperties.getRedisStoragePath() + uuid;
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
     * 将对象转换为JSON字符串
     *
     * @param o 登录对象
     * @return 返回转换后的json对象
     */
    private String toJsonString(Object o) {
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
