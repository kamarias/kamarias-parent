package io.github.kamarias.enums;

/**
 * 性别枚举
 * @author wangyuxing@gogpay.cn
 * @date 2023/3/23 22:09
 */
public enum SexEnum {


    /**
     * 女
     */
    WOMAN(0, "女"),

    /**
     * 男
     */
    MAN(1, "男"),


    /**
     * 未知性别
     */
    UNKNOWN(0, "未知");

    /**
     * 编码
     */
    private final Integer code;

    /**
     * 中文值
     */
    private final String value;


    SexEnum(Integer code, String value) {
        this.code = code;
        this.value = value;
    }


    public Integer getCode() {
        return code;
    }


    public String getValue() {
        return value;
    }

}
