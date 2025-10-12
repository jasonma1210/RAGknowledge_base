#!/bin/bash

# RAG知识库系统JVM性能调优配置
# 适用于生产环境的JVM参数优化

# 基础JVM参数
JAVA_OPTS="-server"

# 内存配置（根据服务器配置调整）
# 建议：总内存的70-80%分配给JVM
JAVA_OPTS="$JAVA_OPTS -Xms2g -Xmx4g"

# 垃圾收集器配置（使用G1GC）
JAVA_OPTS="$JAVA_OPTS -XX:+UseG1GC"
JAVA_OPTS="$JAVA_OPTS -XX:MaxGCPauseMillis=200"
JAVA_OPTS="$JAVA_OPTS -XX:G1HeapRegionSize=16m"
JAVA_OPTS="$JAVA_OPTS -XX:G1NewSizePercent=30"
JAVA_OPTS="$JAVA_OPTS -XX:G1MaxNewSizePercent=40"
JAVA_OPTS="$JAVA_OPTS -XX:G1MixedGCCountTarget=8"
JAVA_OPTS="$JAVA_OPTS -XX:InitiatingHeapOccupancyPercent=45"

# 字符串优化
JAVA_OPTS="$JAVA_OPTS -XX:+UseStringDeduplication"
JAVA_OPTS="$JAVA_OPTS -XX:+OptimizeStringConcat"

# 编译优化
JAVA_OPTS="$JAVA_OPTS -XX:+TieredCompilation"
JAVA_OPTS="$JAVA_OPTS -XX:TieredStopAtLevel=1"

# 安全配置
JAVA_OPTS="$JAVA_OPTS -Djava.security.egd=file:/dev/./urandom"

# 网络优化
JAVA_OPTS="$JAVA_OPTS -Djava.net.preferIPv4Stack=true"

# 时区配置
JAVA_OPTS="$JAVA_OPTS -Duser.timezone=Asia/Shanghai"

# GC日志配置（生产环境建议开启）
JAVA_OPTS="$JAVA_OPTS -XX:+PrintGC"
JAVA_OPTS="$JAVA_OPTS -XX:+PrintGCDetails"
JAVA_OPTS="$JAVA_OPTS -XX:+PrintGCTimeStamps"
JAVA_OPTS="$JAVA_OPTS -XX:+PrintGCApplicationStoppedTime"
JAVA_OPTS="$JAVA_OPTS -Xloggc:logs/gc.log"
JAVA_OPTS="$JAVA_OPTS -XX:+UseGCLogFileRotation"
JAVA_OPTS="$JAVA_OPTS -XX:NumberOfGCLogFiles=5"
JAVA_OPTS="$JAVA_OPTS -XX:GCLogFileSize=10M"

# JFR性能分析（可选）
# JAVA_OPTS="$JAVA_OPTS -XX:+FlightRecorder"
# JAVA_OPTS="$JAVA_OPTS -XX:StartFlightRecording=duration=60s,filename=logs/app.jfr"

# 内存溢出时生成堆转储
JAVA_OPTS="$JAVA_OPTS -XX:+HeapDumpOnOutOfMemoryError"
JAVA_OPTS="$JAVA_OPTS -XX:HeapDumpPath=logs/heapdump.hprof"

# Spring Boot配置
JAVA_OPTS="$JAVA_OPTS -Dspring.profiles.active=prod"

# 应用配置
JAVA_OPTS="$JAVA_OPTS -Dfile.encoding=UTF-8"
JAVA_OPTS="$JAVA_OPTS -Djava.awt.headless=true"

# 线程配置
JAVA_OPTS="$JAVA_OPTS -XX:CICompilerCount=4"

# 导出环境变量
export JAVA_OPTS

# 启动应用
echo "启动RAG知识库系统..."
echo "JVM参数: $JAVA_OPTS"
echo "内存配置: 初始堆内存2GB, 最大堆内存4GB"
echo "垃圾收集器: G1GC"
echo "GC暂停目标: 200ms"

# 使用nohup在后台运行
nohup java $JAVA_OPTS -jar rag-knowledge-base.jar > logs/app.log 2>&1 &

# 获取进程ID
PID=$!
echo "应用已启动，进程ID: $PID"
echo "日志文件: logs/app.log"
echo "GC日志: logs/gc.log"

# 保存进程ID到文件
echo $PID > rag-knowledge-base.pid

echo "启动完成！"
echo "使用 'tail -f logs/app.log' 查看应用日志"
echo "使用 'kill $PID' 停止应用"
