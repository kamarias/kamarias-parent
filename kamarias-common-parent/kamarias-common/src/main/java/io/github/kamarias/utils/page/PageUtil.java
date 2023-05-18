package io.github.kamarias.utils.page;

import io.github.kamarias.vo.PageVO;

import java.util.List;

/**
 * 分页结果处理工具类
 *
 * @author wangyuxing@gogpay.cn
 * @date 2023/5/4 15:40
 */
public class PageUtil {


    /**
     * 处理翻页结果
     *
     * @param list     返回的集合
     * @param total    页面中速
     * @param pageSize 页面大小
     * @param <T>      所有泛型
     * @return 返回计算的翻页结果
     */
    public static <T> PageVO<T> handlerResult(List<T> list, long total, Integer pageSize) {
        return new PageVO<T>(
                total, calcPage(total,
                pageSize), list
        );
    }

    /**
     * 计算可翻页数
     *
     * @param total    总数
     * @param pageSize 页面大小
     * @return 返回计算结果
     */
    public static long calcPage(long total, long pageSize) {
        return (long) Math.ceil((double) total / (double) pageSize);
    }

}
