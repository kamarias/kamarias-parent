package io.github.kamarias.dto;

import com.alibaba.fastjson.JSONObject;
import org.springframework.http.HttpStatus;

import java.io.Serializable;

/**
 * 请求响应统一实体
 *
 * @author wangyuxing@gogpay.cn
 * @date 2023/1/30 14:32
 */
public class ResultDTO<E> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 返回码状态
     */
    private Integer code;

    /**
     * 提示信息
     */
    private String msg;

    /**
     * 消息体
     */
    private E data;

    /**
     * 返回成功消息
     *
     * @return 成功消息
     */
    public static ResultDTO success() {
        return ResultDTO.success("操作成功");
    }

    /**
     * 返回成功数据
     *
     * @return 成功消息
     */
    public static <E> ResultDTO<E> success(E data) {
        return ResultDTO.success("操作成功", data);
    }

    /**
     * 返回成功消息
     *
     * @param msg 返回内容
     * @return 成功消息
     */
    public static ResultDTO success(String msg) {
        return ResultDTO.success(msg, null);
    }

    /**
     * 返回成功消息
     *
     * @param msg  返回内容
     * @param data 数据对象
     * @return 成功消息
     */
    public static <E> ResultDTO<E> success(String msg, E data) {
        return new ResultDTO(HttpStatus.OK.value(), msg, data);
    }

    /**
     * 返回错误消息
     *
     * @return 操作结果
     */
    public static ResultDTO error() {
        return ResultDTO.error("操作失败");
    }

    /**
     * 返回服务器内部错误
     *
     * @param msg 错误信息
     */
    public static ResultDTO error(String msg) {
        return ResultDTO.error(msg, null);
    }

    /**
     * 返回服务器内部错误
     *
     * @param msg  错误信息
     * @param data 错误数据
     */
    public static <E> ResultDTO<E> error(String msg, E data) {
        return new ResultDTO(HttpStatus.INTERNAL_SERVER_ERROR.value(), msg, data);
    }

    /**
     * 返回未授权错误
     */
    public static ResultDTO errorOfAuth() {
        return new ResultDTO(HttpStatus.UNAUTHORIZED.value(), "权限异常", null);
    }

    /**
     * 判断是否请求成功
     *
     * @param result 请求结果
     * @return 是否成功
     */
    public static boolean isSuccess(ResultDTO result) {
        if (result == null || result.getCode() == null) {
            return false;
        }
        return HttpStatus.OK.value() == result.getCode();
    }


    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public E getData() {
        return data;
    }

    public void setData(E data) {
        this.data = data;
    }

    public ResultDTO() {
    }

    public ResultDTO(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public ResultDTO(Integer code, String msg, E data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    @Override
    public String toString() {
        return JSONObject.toJSONString(this);
    }

}
