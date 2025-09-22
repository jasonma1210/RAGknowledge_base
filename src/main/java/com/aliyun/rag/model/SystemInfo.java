package com.aliyun.rag.model;

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

    // Getters and Setters
    public Double getCpuUsage() {
        return cpuUsage;
    }

    public void setCpuUsage(Double cpuUsage) {
        this.cpuUsage = cpuUsage;
    }

    public Double getMemoryUsage() {
        return memoryUsage;
    }

    public void setMemoryUsage(Double memoryUsage) {
        this.memoryUsage = memoryUsage;
    }

    public Double getDiskUsage() {
        return diskUsage;
    }

    public void setDiskUsage(Double diskUsage) {
        this.diskUsage = diskUsage;
    }

    public Long getTotalMemory() {
        return totalMemory;
    }

    public void setTotalMemory(Long totalMemory) {
        this.totalMemory = totalMemory;
    }

    public Long getUsedMemory() {
        return usedMemory;
    }

    public void setUsedMemory(Long usedMemory) {
        this.usedMemory = usedMemory;
    }

    public Long getTotalDiskSpace() {
        return totalDiskSpace;
    }

    public void setTotalDiskSpace(Long totalDiskSpace) {
        this.totalDiskSpace = totalDiskSpace;
    }

    public Long getUsedDiskSpace() {
        return usedDiskSpace;
    }

    public void setUsedDiskSpace(Long usedDiskSpace) {
        this.usedDiskSpace = usedDiskSpace;
    }

    public String getUptime() {
        return uptime;
    }

    public void setUptime(String uptime) {
        this.uptime = uptime;
    }

    public Long getProcessCount() {
        return processCount;
    }

    public void setProcessCount(Long processCount) {
        this.processCount = processCount;
    }

    public Double getLoadAverage() {
        return loadAverage;
    }

    public void setLoadAverage(Double loadAverage) {
        this.loadAverage = loadAverage;
    }
}