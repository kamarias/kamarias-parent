package io.github.kamarias.properties;

import com.alibaba.fastjson.JSON;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Arrays;
import java.util.List;

/**
 * @author wangyuxing@gogpay.cn
 * @date 2023/5/24 13:42
 */
@ConfigurationProperties(prefix = SecurityProperties.PREFIX)
public class SecurityProperties {

    public final static String PREFIX = "security.config";

    /**
     * 授权访问白名单
     */
    private List<String> anonymous = Arrays.asList("/login");

    /**
     * 静态资源访问白名单
     */
    private List<String> staticResPath = Arrays.asList("/*.html", "/**/*.html", "/**/*.css", "/**/*.js");


    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }

    public List<String> getAnonymous() {
        return anonymous;
    }

    public void setAnonymous(List<String> anonymous) {
        this.anonymous = anonymous;
    }

    public List<String> getStaticResPath() {
        return staticResPath;
    }

    public void setStaticResPath(List<String> staticResPath) {
        this.staticResPath = staticResPath;
    }
}
