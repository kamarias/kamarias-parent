package io.github.kamarias.vo;



import com.alibaba.fastjson2.JSON;

import java.io.Serializable;
import java.util.List;

/**
 * 分页返回实体
 *
 * @author wangyuxing@gogpay.cn
 * @date 2023/5/4 15:18
 */
public class PageVO<E> implements Serializable {

    /**
     * 总记录数
     */
    private Long total;

    /**
     * 最大页数
     */
    private Long totalPage;

    /**
     * 返回的数据集合
     */
    private List<E> list;

    public PageVO(Long total, Long totalPage, List<E> list) {
        this.total = total;
        this.totalPage = totalPage;
        this.list = list;
    }

    public Long getTotal() {
        return total;
    }

    public void setTotal(Long total) {
        this.total = total;
    }

    public Long getTotalPage() {
        return totalPage;
    }

    public void setTotalPage(Long totalPage) {
        this.totalPage = totalPage;
    }

    public List<E> getList() {
        return list;
    }

    public void setList(List<E> list) {
        this.list = list;
    }

    public String toString() {
        return JSON.toJSONString(this);
    }
}
