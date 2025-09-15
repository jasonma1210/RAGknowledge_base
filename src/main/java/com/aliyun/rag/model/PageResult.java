package com.aliyun.rag.model;

import java.util.List;

/**
 * 分页结果模型
 * <p>
 * 用于封装分页查询的结果，包括数据列表、总记录数、页码等信息
 * </p>
 *
 * @author Jason Ma
 * @version 1.0.0
 * @since 2025-09-14
 */
public class PageResult<T> {
    /**
     * 数据列表
     */
    private List<T> data;

    /**
     * 当前页码（从0开始）
     */
    private int page;

    /**
     * 每页大小
     */
    private int size;

    /**
     * 总记录数
     */
    private long total;

    /**
     * 总页数
     */
    private int totalPages;

    public PageResult() {
    }

    public PageResult(List<T> data, int page, int size, long total) {
        this.data = data;
        this.page = page;
        this.size = size;
        this.total = total;
        this.totalPages = (int) Math.ceil((double) total / size);
    }

    // Getters and Setters

    public List<T> getData() {
        return data;
    }

    public void setData(List<T> data) {
        this.data = data;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
        this.totalPages = (int) Math.ceil((double) total / size);
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }
}