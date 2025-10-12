package com.aliyun.rag.aop;

import com.aliyun.rag.config.DynamicRoutingDataSource;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * 数据源切换切面
 * <p>
 * 根据方法名自动切换主库或从库
 * </p>
 *
 * @author Jason Ma
 * @version 1.0.0
 * @since 2025-10-12
 */
@Aspect
@Order(1)
@Component
public class DataSourceAspect {

    private static final Logger log = LoggerFactory.getLogger(DataSourceAspect.class);

    /**
     * 定义读操作的切点
     * 匹配所有以find、select、get、query开头的方法
     */
    @Pointcut("execution(* com.aliyun.rag.repository.*.find*(..)) || " +
              "execution(* com.aliyun.rag.repository.*.select*(..)) || " +
              "execution(* com.aliyun.rag.repository.*.get*(..)) || " +
              "execution(* com.aliyun.rag.repository.*.query*(..))")
    public void readOperation() {}

    /**
     * 定义写操作的切点
     * 匹配所有以save、update、delete、insert开头的方法
     */
    @Pointcut("execution(* com.aliyun.rag.repository.*.save*(..)) || " +
              "execution(* com.aliyun.rag.repository.*.update*(..)) || " +
              "execution(* com.aliyun.rag.repository.*.delete*(..)) || " +
              "execution(* com.aliyun.rag.repository.*.insert*(..))")
    public void writeOperation() {}

    /**
     * 读操作使用从库
     */
    @Around("readOperation()")
    public Object aroundReadOperation(ProceedingJoinPoint joinPoint) throws Throwable {
        DynamicRoutingDataSource.setDataSourceType(DynamicRoutingDataSource.DataSourceType.SLAVE);
        log.debug("切换到从库进行读操作: {}", joinPoint.getSignature().getName());
        
        try {
            return joinPoint.proceed();
        } finally {
            DynamicRoutingDataSource.clearDataSourceType();
            log.debug("清除了数据源类型");
        }
    }

    /**
     * 写操作使用主库
     */
    @Around("writeOperation()")
    public Object aroundWriteOperation(ProceedingJoinPoint joinPoint) throws Throwable {
        DynamicRoutingDataSource.setDataSourceType(DynamicRoutingDataSource.DataSourceType.MASTER);
        log.debug("切换到主库进行写操作: {}", joinPoint.getSignature().getName());
        
        try {
            return joinPoint.proceed();
        } finally {
            DynamicRoutingDataSource.clearDataSourceType();
            log.debug("清除了数据源类型");
        }
    }
}