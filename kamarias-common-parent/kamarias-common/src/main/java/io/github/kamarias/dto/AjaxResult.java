package io.github.kamarias.dto;


import io.github.kamarias.utils.string.StringUtils;
import org.springframework.http.HttpStatus;

import java.io.Serializable;
import java.util.HashMap;

/**
 * Ajax请求响应实体
 *
 * @author wangyuxing@gogpay.cn
 * @date 2023/1/30 18:16
 */
public class AjaxResult<E> extends HashMap<String, Object> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 状态码
     */
    public static final String CODE_TAG = "code";

    /**
     * 返回内容
     */
    public static final String MSG_TAG = "msg";

    /**
     * 数据对象
     */
    public static final String DATA_TAG = "data";

    /**
     * 初始化一个新创建的 AjaxResult 对象，使其表示一个空消息。
     */
    public AjaxResult() {
    }

    /**
     * 初始化一个新创建的 AjaxResult 对象
     *
     * @param code 状态码
     * @param msg  返回内容
     */
    public AjaxResult(int code, String msg) {
        super.put(CODE_TAG, code);
        super.put(MSG_TAG, msg);
    }

    /**
     * 初始化一个新创建的 AjaxResult 对象
     *
     * @param code 状态码
     * @param msg  返回内容
     * @param data 数据对象
     */
    public AjaxResult(int code, String msg, E data) {
        super.put(CODE_TAG, code);
        super.put(MSG_TAG, msg);
        if (StringUtils.isNotNull(data)) {
            super.put(DATA_TAG, data);
        }
    }

    /**
     * 返回成功消息
     *
     * @return 成功消息
     */
    public static AjaxResult<Object> success() {
        return AjaxResult.success("操作成功");
    }

    /**
     * 返回成功数据
     *
     * @return 成功消息
     */
    public static <E> AjaxResult<E> success(E data) {
        return AjaxResult.success("操作成功", data);
    }

    /**
     * 返回成功消息
     *
     * @param msg 返回内容
     * @return 成功消息
     */
    public static AjaxResult<Object> success(String msg) {
        return AjaxResult.success(msg, null);
    }

    /**
     * 返回成功消息
     *
     * @param msg  返回内容
     * @param data 数据对象
     * @return 成功消息
     */
    public static <E> AjaxResult<E> success(String msg, E data) {
        return new AjaxResult<>(HttpStatus.OK.value(), msg, data);
    }

    /**
     * 返回错误消息
     *
     * @return 操作结果
     */
    public static AjaxResult<Object> error() {
        return AjaxResult.error("操作失败");
    }

    /**
     * 返回错误消息
     *
     * @return 操作结果
     */
    public static AjaxResult<Object> errorOfAuth() {
        return new AjaxResult<>(HttpStatus.UNAUTHORIZED.value(), "认证异常", null);
    }

    /**
     * 返回错误消息
     *
     * @return 操作结果
     */
    public static AjaxResult<Object> errorOfIpError(String msg) {
        return new AjaxResult<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), msg, null);
    }

    /**
     * 返回错误消息
     *
     * @param msg 返回内容
     * @return 警告消息
     */
    public static AjaxResult<Object> error(String msg) {
        return AjaxResult.error(msg, null);
    }

    /**
     * 返回错误消息
     *
     * @param msg  返回内容
     * @param data 数据对象
     * @return 警告消息
     */
    public static <E> AjaxResult<E> error(String msg, E data) {
        return new AjaxResult<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), msg, data);
    }

    /**
     * 返回错误消息
     *
     * @param code 状态码
     * @param msg  返回内容
     * @return 警告消息
     */
    public static AjaxResult<Object> error(int code, String msg) {
        return new AjaxResult<>(code, msg, null);
    }

    /**
     * 判断是否请求成功
     *
     * @param result 请求结果
     * @return 是否成功
     */
    public static boolean isSuccess(AjaxResult<Object> result) {
        if (result == null || result.get(CODE_TAG) == null) {
            return false;
        }
        return HttpStatus.OK.value() == (int) result.get(CODE_TAG);
    }

    /**
     * 获取返回消息
     *
     * @return 返回消息
     */
    public String getMsg() {
        return String.valueOf(this.get(MSG_TAG));
    }

    /**
     * 获取返回数据
     *
     * @return 返回数据
     */
    public E getData() {
        return (E) this.get(DATA_TAG);
    }

}
