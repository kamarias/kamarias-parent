package io.github.kamarias.exception;

/**
 * @author YuXing Wang
 * @date 2025/3/4
 * @since
 */
public class SecurityException extends RuntimeException {


    private Integer code;

    public SecurityException(Throwable cause) {
        super(cause);
    }

    public SecurityException(String msg) {
        super(msg);
    }

    public SecurityException(Integer code, String msg) {
        super(msg);
        this.code = code;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

}
