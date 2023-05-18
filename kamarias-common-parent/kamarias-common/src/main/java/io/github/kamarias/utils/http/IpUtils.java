package io.github.kamarias.utils.http;

import com.wyx.common.utils.string.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;

/**
 * 用户原始请求工具类
 * @author wangyuxing@gogpay.cn
 * @date 2023/2/1 14:41
 */
public class IpUtils {
    private static final Logger logger = LoggerFactory.getLogger(IpUtils.class);

    /**
     * 获取Ip地址
     * @param request
     * @return
     */
    public static String getIpAdrress(HttpServletRequest request) {
        String Xip = request.getHeader("X-Real-IP");
        String XFor = request.getHeader("X-Forwarded-For");

        logger.info("X-Real-IP = " + Xip);
        logger.info("X-Forwarded-For = " + XFor);


        if(StringUtils.isNotEmpty(Xip) && !"unKnown".equalsIgnoreCase(Xip)){
            return Xip;
        }
        if (StringUtils.isBlank(Xip) || "unknown".equalsIgnoreCase(Xip)) {
            Xip = request.getHeader("Proxy-Client-IP");
            if(StringUtils.isNotEmpty(Xip)) {
                return Xip;
            }
        }
        if (StringUtils.isBlank(Xip) || "unknown".equalsIgnoreCase(Xip)) {
            Xip = request.getHeader("WL-Proxy-Client-IP");
            if(StringUtils.isNotEmpty(Xip)) {
                return Xip;
            }
        }
        if (StringUtils.isBlank(Xip) || "unknown".equalsIgnoreCase(Xip)) {
            Xip = request.getHeader("HTTP_CLIENT_IP");
            if(StringUtils.isNotEmpty(Xip)) {
                return Xip;
            }
        }
        if (StringUtils.isBlank(XFor) || "unknown".equalsIgnoreCase(Xip)) {
            Xip = request.getHeader("HTTP_X_FORWARDED_FOR");
            if(StringUtils.isNotEmpty(Xip)) {
                return Xip;
            }
        }
        if (StringUtils.isBlank(XFor) || "unknown".equalsIgnoreCase(Xip)) {
            Xip = request.getRemoteAddr();
            if(StringUtils.isNotEmpty(Xip)) {
                return Xip;
            }
        }

        if(StringUtils.isNotEmpty(XFor) && !"unKnown".equalsIgnoreCase(XFor)){
            //多次反向代理后会有多个ip值，第一个ip才是真实ip
            int index = XFor.indexOf(",");
            if(index != -1){
                return XFor.substring(0,index);
            }else{
                return XFor;
            }
        }
        if(StringUtils.isEmpty(XFor)){
            return request.getRemoteAddr();
        }

        return XFor;
    }
}
