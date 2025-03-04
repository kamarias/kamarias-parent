package io.github.kamarias.exception;

/**
 * @author YuXing Wang
 * @date 2025/3/4
 * @since
 */
public class SecurityException extends RuntimeException {

    private final Integer code = 401;

    public SecurityException(String msg) {
        super(msg);
    }

    public Integer getCode() {
        return code;
    }


}
