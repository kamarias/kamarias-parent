package io.github.kamarias.exception;

/**
 * 自定义异常
 * @author wangyuxing@gogpay.cn
 * @date 2023/1/30 11:57
 */
public class CustomException extends RuntimeException{

    private Integer code;

    public CustomException(String msg) {
        super(msg);
    }

    public CustomException(Integer code, String msg) {
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
