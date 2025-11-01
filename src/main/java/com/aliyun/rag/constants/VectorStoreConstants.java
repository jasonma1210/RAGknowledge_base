package com.aliyun.rag.constants;

/**
 * 向量存储服务常量
 * <p>
 * 定义向量检索相关的常量，避免Magic Number
 * </p>
 *
 * @author Claude Code
 * @version 1.0.0
 * @since 2025-10-31
 */
public class VectorStoreConstants {

    private VectorStoreConstants() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    // ========== 搜索相关常量 ==========

    /**
     * 搜索结果最大倍数（用于扩大搜索范围后过滤）
     */
    public static final int MAX_SEARCH_MULTIPLIER = 10;

    /**
     * 绝对最大搜索结果数
     */
    public static final int ABSOLUTE_MAX_RESULTS = 1000;

    /**
     * 默认Top-K数量
     */
    public static final int DEFAULT_TOP_K = 10;

    /**
     * 最大Top-K数量
     */
    public static final int MAX_TOP_K = 100;

    /**
     * 最小Top-K数量
     */
    public static final int MIN_TOP_K = 1;

    // ========== 相似度阈值 ==========

    /**
     * 默认相似度阈值
     */
    public static final double DEFAULT_SIMILARITY_THRESHOLD = 0.7;

    /**
     * 最小相似度阈值
     */
    public static final double MIN_SIMILARITY_THRESHOLD = 0.5;

    /**
     * 最大相似度阈值
     */
    public static final double MAX_SIMILARITY_THRESHOLD = 1.0;

    /**
     * 高质量结果阈值（更严格）
     */
    public static final double HIGH_QUALITY_THRESHOLD = 0.85;

    // ========== 缓存相关常量 ==========

    /**
     * 缓存TTL（秒）
     */
    public static final int CACHE_TTL_SECONDS = 300; // 5分钟

    /**
     * 最大缓存大小
     */
    public static final int MAX_CACHE_SIZE = 1000;

    /**
     * 缓存键前缀
     */
    public static final String CACHE_KEY_PREFIX = "vector:search:";

    // ========== 向量维度 ==========

    /**
     * text-embedding-v4的向量维度
     */
    public static final int VECTOR_DIMENSION_V4 = 1536;

    /**
     * text-embedding-v3的向量维度（备用）
     */
    public static final int VECTOR_DIMENSION_V3 = 1024;

    // ========== Collection命名 ==========

    /**
     * Collection名称分隔符
     */
    public static final String COLLECTION_NAME_SEPARATOR = "_";

    /**
     * Collection名称最大长度
     */
    public static final int MAX_COLLECTION_NAME_LENGTH = 255;

    // ========== 批量操作 ==========

    /**
     * 批量插入的批次大小
     */
    public static final int BATCH_INSERT_SIZE = 100;

    /**
     * 批量删除的批次大小
     */
    public static final int BATCH_DELETE_SIZE = 50;

    // ========== 度量类型 ==========

    /**
     * L2距离（欧氏距离）
     */
    public static final String METRIC_TYPE_L2 = "L2";

    /**
     * 内积（Inner Product）
     */
    public static final String METRIC_TYPE_IP = "IP";

    /**
     * 余弦相似度
     */
    public static final String METRIC_TYPE_COSINE = "COSINE";

    // ========== 索引类型 ==========

    /**
     * IVF_FLAT索引（适合中小规模）
     */
    public static final String INDEX_TYPE_IVF_FLAT = "IVF_FLAT";

    /**
     * IVF_SQ8索引（量化，节省内存）
     */
    public static final String INDEX_TYPE_IVF_SQ8 = "IVF_SQ8";

    /**
     * HNSW索引（高性能，适合大规模）
     */
    public static final String INDEX_TYPE_HNSW = "HNSW";

    // ========== 超时设置 ==========

    /**
     * 搜索超时（毫秒）
     */
    public static final long SEARCH_TIMEOUT_MS = 5000L; // 5秒

    /**
     * 插入超时（毫秒）
     */
    public static final long INSERT_TIMEOUT_MS = 10000L; // 10秒

    /**
     * 删除超时（毫秒）
     */
    public static final long DELETE_TIMEOUT_MS = 3000L; // 3秒
}
