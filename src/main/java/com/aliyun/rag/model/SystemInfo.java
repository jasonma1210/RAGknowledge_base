package com.aliyun.rag.model;

import lombok.Data;

/**
 * 系统信息模型
 * <p>
 * 用于封装系统资源使用情况
 * </p>
 *
 * @author Jason Ma
 * @version 1.0.0
 * @since 2025-09-13
 */
@Data
public class SystemInfo {

    private Double cpuUsage;

    private Double memoryUsage;

    private Double diskUsage;

    private Long totalMemory;

    private Long usedMemory;

    private Long totalDiskSpace;

    private Long usedDiskSpace;

    private String uptime;

    private Long processCount;

    private Double loadAverage;
}