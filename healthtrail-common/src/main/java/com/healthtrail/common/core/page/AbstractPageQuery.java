package com.healthtrail.common.core.page;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import javax.validation.constraints.Max;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 分页查询抽象基类，提供分页参数校验与MyBatis-Plus分页对象转换
 *
 * @author valarchie
 */
@EqualsAndHashCode(callSuper = true)
@Data
public abstract class AbstractPageQuery<T> extends AbstractQuery<T> {

    /**
     * 最大分页页数
     */
    public static final int MAX_PAGE_NUM = 200;
    /**
     * 单页最大大小
     */
    public static final int MAX_PAGE_SIZE = 500;
    /**
     * 默认分页页数
     */
    public static final int DEFAULT_PAGE_NUM = 1;
    /**
     * 默认分页大小
     */
    public static final int DEFAULT_PAGE_SIZE = 10;

    /**
     * 当前页码
     */
    @Max(MAX_PAGE_NUM)
    protected Integer pageNum;
    /**
     * 每页大小
     */
    @Max(MAX_PAGE_SIZE)
    protected Integer pageSize;

    public Page<T> toPage() {
        pageNum = ObjectUtil.defaultIfNull(pageNum, DEFAULT_PAGE_NUM);
        pageSize = ObjectUtil.defaultIfNull(pageSize, DEFAULT_PAGE_SIZE);
        return new Page<>(pageNum, pageSize);
    }

}
