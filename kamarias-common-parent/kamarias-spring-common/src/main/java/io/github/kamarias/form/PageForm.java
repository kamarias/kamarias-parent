package io.github.kamarias.form;

import com.alibaba.fastjson.JSON;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 分页参数实体
 *
 * @author wangyuxing@gogpay.cn
 * @date 2023/3/24 16:49
 */
public class PageForm implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 当前页码
     */
    @NotNull(message = "pageNum不能为空")
    private Integer pageNum;


    /**
     * 每页展示行数
     */
    @NotNull(message = "pageSize不能为空")
    private Integer pageSize;


    public Integer getPageNum() {
        return pageNum;
    }

    public void setPageNum(Integer pageNum) {
        this.pageNum = pageNum;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }

    /**
     * 计算偏移量（适用于queryDsl）
     *
     * @return 获取偏移量
     */
    public long getOffset() {
        return (pageNum - 1) * pageSize;
    }

}
