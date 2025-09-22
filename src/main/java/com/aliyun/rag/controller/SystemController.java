package com.aliyun.rag.controller;

import com.aliyun.rag.model.R;
import com.aliyun.rag.model.SystemInfo;
import com.aliyun.rag.model.User;
import com.aliyun.rag.model.dto.UserDTO;
import com.aliyun.rag.service.RAGService;
import com.aliyun.rag.service.VectorStoreService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GlobalMemory;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.software.os.OperatingSystem;
import oshi.util.Util;

import java.util.HashMap;
import java.util.Map;

/**
 * 系统监控控制器
 * <p>
 * 提供系统资源监控信息
 * </p>
 * 
 * @author Jason Ma
 * @version 1.0.0
 * @since 2025-09-15
 */
@RestController
@RequestMapping("/system")

public class SystemController {
    
    private static final Logger log = LoggerFactory.getLogger(SystemController.class);
    
    private final RAGService ragService;
    private final VectorStoreService vectorStoreService;
    
    // 系统信息获取工具
    private final oshi.SystemInfo oshiSystemInfo = new oshi.SystemInfo();
    private final HardwareAbstractionLayer hardware = oshiSystemInfo.getHardware();
    private final OperatingSystem operatingSystem = oshiSystemInfo.getOperatingSystem();

    public SystemController(RAGService ragService, VectorStoreService vectorStoreService) {
        this.ragService = ragService;
        this.vectorStoreService = vectorStoreService;
    }
    
    /**
     * 获取系统信息和用户统计信息
     */
    @GetMapping("/dashboard")
    public ResponseEntity<R<Map<String, Object>>> getDashboardInfo(HttpServletRequest httpRequest) {
        try {
            // 获取当前用户
            User currentUser = (User) httpRequest.getAttribute("currentUser");
            
            // 转换为UserDTO以避免敏感信息泄露
            UserDTO currentUserDTO = UserDTO.fromUser(currentUser);
            
            // 获取系统信息
            SystemInfo systemInfo = getSystemInfo();
            
            // 获取用户文件统计信息
            Map<String, Object> userStats = getUserStats(currentUserDTO);
            
            // 封装返回结果
            Map<String, Object> result = new HashMap<>();
            result.put("systemInfo", systemInfo);
            result.put("userStats", userStats);
            
            return ResponseEntity.ok(R.success(result));
        } catch (Exception e) {
            log.error("获取仪表板信息失败", e);
            return ResponseEntity.internalServerError().body(R.error(500, "获取仪表板信息失败"));
        }
    }
    
    /**
     * 获取系统信息
     */
    private SystemInfo getSystemInfo() {
        try {
            // 获取CPU使用率
            CentralProcessor processor = hardware.getProcessor();
            long[] prevTicks = processor.getSystemCpuLoadTicks();
            Util.sleep(1000); // 等待1秒
            long[] ticks = processor.getSystemCpuLoadTicks();
            double cpuUsage = processor.getSystemCpuLoadBetweenTicks(prevTicks) * 100;

            // 获取内存信息
            GlobalMemory memory = hardware.getMemory();
            long totalMemory = memory.getTotal();
            long availableMemory = memory.getAvailable();
            long usedMemory = totalMemory - availableMemory;
            double memoryUsage = (double) usedMemory / totalMemory * 100;

            // 获取磁盘信息
            long totalDiskSpace = 0;
            long usedDiskSpace = 0;

            // 获取系统根分区磁盘使用情况
            try {
                java.io.File[] roots = java.io.File.listRoots();
                for (java.io.File root : roots) {
                    totalDiskSpace += root.getTotalSpace();
                    usedDiskSpace += root.getTotalSpace() - root.getFreeSpace();
                }
            } catch (Exception e) {
                log.warn("获取磁盘信息失败", e);
            }

            double diskUsage = totalDiskSpace > 0 ? (double) usedDiskSpace / totalDiskSpace * 100 : 0;

            // 获取系统运行时间
            long uptimeSeconds = operatingSystem.getSystemUptime();
            String uptime = formatUptime(uptimeSeconds);

            // 获取进程数
            int processCount = operatingSystem.getProcessCount();

            // 获取系统负载
            double loadAverage = 0.0; // operatingSystem.getSystemLoadAverage();

            // 封装系统信息
            SystemInfo info = new SystemInfo();
            info.setCpuUsage(Math.round(cpuUsage * 100.0) / 100.0); // 保留两位小数
            info.setMemoryUsage(Math.round(memoryUsage * 100.0) / 100.0);
            info.setDiskUsage(Math.round(diskUsage * 100.0) / 100.0);
            info.setTotalMemory(totalMemory);
            info.setUsedMemory(usedMemory);
            info.setTotalDiskSpace(totalDiskSpace);
            info.setUsedDiskSpace(usedDiskSpace);
            info.setUptime(uptime);
            info.setProcessCount((long) processCount);
            info.setLoadAverage(loadAverage);

            return info;
        } catch (Exception e) {
            log.error("获取系统信息失败", e);
            throw new RuntimeException("获取系统信息失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 获取用户统计信息
     */
    private Map<String, Object> getUserStats(UserDTO userDTO) {
        Map<String, Object> stats = new HashMap<>();
        
        try {
            // 创建一个User实体用于兼容现有服务方法（仅包含必要信息）
            User user = new User();
            user.setId(userDTO.getId());
            user.setUsername(userDTO.getUsername());
            user.setUsedStorage(userDTO.getUsedStorage());
            user.setStorageQuota(userDTO.getStorageQuota());
            
            // 获取用户文件数量
            long fileCount = ragService.getDocumentCount(user);
            stats.put("fileCount", fileCount);
            
            // 获取用户向量文件数量
            long vectorCount = vectorStoreService.getVectorCount(user);
            stats.put("vectorCount", vectorCount);
            
            // 获取存储使用情况（不直接暴露敏感信息）
            stats.put("usedStorage", userDTO.getUsedStorage());
            stats.put("storageQuota", userDTO.getStorageQuota());
            
            // 计算存储占比（不直接使用用户敏感信息）
            double storageUsage = userDTO.getStorageQuota() > 0 ? 
                (double) userDTO.getUsedStorage() / userDTO.getStorageQuota() * 100 : 0;
            stats.put("storageUsage", Math.round(storageUsage * 100.0) / 100.0);
        } catch (Exception e) {
            log.error("获取用户统计信息失败", e);
        }
        
        return stats;
    }

    /**
     * 格式化系统运行时间
     */
    private String formatUptime(long uptimeSeconds) {
        long days = uptimeSeconds / (24 * 3600);
        long hours = (uptimeSeconds % (24 * 3600)) / 3600;
        long minutes = (uptimeSeconds % 3600) / 60;
        long seconds = uptimeSeconds % 60;

        StringBuilder sb = new StringBuilder();
        if (days > 0) {
            sb.append(days).append("天 ");
        }
        if (hours > 0) {
            sb.append(hours).append("小时 ");
        }
        if (minutes > 0) {
            sb.append(minutes).append("分钟 ");
        }
        sb.append(seconds).append("秒");

        return sb.toString().trim();
    }
}