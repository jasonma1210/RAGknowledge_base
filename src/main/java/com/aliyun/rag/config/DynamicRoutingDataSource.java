package com.aliyun.rag.config;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

/**
 * 动态数据源路由
 * <p>
 * 根据当前线程的数据库操作类型选择主库或从库
 * </p>
 *
 * @author Jason Ma
 * @version 1.0.0
 * @since 2025-10-12
 */
public class DynamicRoutingDataSource extends AbstractRoutingDataSource {

    /**
     * 数据源类型枚举
     */
    public enum DataSourceType {
        MASTER, SLAVE
    }

    /**
     * 使用ThreadLocal存储当前线程的数据源类型
     */
    private static final ThreadLocal<DataSourceType> CONTEXT_HOLDER = new ThreadLocal<>();

    /**
     * 确定当前查找键
     */
    @Override
    protected Object determineCurrentLookupKey() {
        DataSourceType dataSourceType = CONTEXT_HOLDER.get();
        if (dataSourceType == null) {
            // 默认使用主库
            return "master";
        }
        return dataSourceType.name().toLowerCase();
    }

    /**
     * 设置数据源类型
     */
    public static void setDataSourceType(DataSourceType dataSourceType) {
        CONTEXT_HOLDER.set(dataSourceType);
    }

    /**
     * 清除数据源类型
     */
    public static void clearDataSourceType() {
        CONTEXT_HOLDER.remove();
    }

    /**
     * 获取当前数据源类型
     */
    public static DataSourceType getDataSourceType() {
        return CONTEXT_HOLDER.get();
    }
}