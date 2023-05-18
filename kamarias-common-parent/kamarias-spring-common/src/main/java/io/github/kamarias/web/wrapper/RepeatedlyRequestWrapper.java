package io.github.kamarias.web.wrapper;

import io.github.kamarias.utils.http.HttpHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

/**
 * 重复请求包装类
 * @author 王玉星
 * @date 2023/1/12 18:14
 */
public class RepeatedlyRequestWrapper extends HttpServletRequestWrapper {

    private static final Logger LOGGER = LoggerFactory.getLogger(RepeatedlyRequestWrapper.class);

    private final String CHARACTER_ENCODING = "UTF-8";

    private final byte[] body;

    public RepeatedlyRequestWrapper(HttpServletRequest request, ServletResponse response) {
        super(request);
        try {
            request.setCharacterEncoding(this.CHARACTER_ENCODING);
        } catch (UnsupportedEncodingException e) {
            LOGGER.error("设置编码异常");
            throw new RuntimeException(e);
        }
        response.setCharacterEncoding(this.CHARACTER_ENCODING);

        body = HttpHelper.getBodyString(request).getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public BufferedReader getReader() {
        return new BufferedReader(new InputStreamReader(getInputStream()));
    }

    @Override
    public ServletInputStream getInputStream() {

        final ByteArrayInputStream bis = new ByteArrayInputStream(body);

        return new ServletInputStream() {

            @Override
            public int read() {
                return bis.read();
            }

            @Override
            public boolean isFinished() {
                return false;
            }

            @Override
            public boolean isReady() {
                return false;
            }

            @Override
            public void setReadListener(ReadListener readListener) {

            }
        };
    }

    /**
     * 字节数组转字符串
     * @return 返回字符串
     */
    public String getBodyString(){
        return new String(this.body, StandardCharsets.UTF_8);
    }

}
