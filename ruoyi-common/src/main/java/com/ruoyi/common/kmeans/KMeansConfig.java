package com.ruoyi.common.kmeans;

import java.io.Serializable;

/**
 * K-Means聚类算法配置类
 *
 * @Author 范佳兴
 * @Date 2024/12/18
 */
public class KMeansConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 聚类数量（K值）
     */
    private int k = 3;

    /**
     * 最大迭代次数
     */
    private int maxIterations = 100;

    /**
     * 收敛阈值（当质心变化小于此值时停止迭代）
     */
    private double convergenceThreshold = 0.0001;

    /**
     * 随机种子（用于初始化质心，设置固定值可保证结果可重复）
     */
    private Long randomSeed = 42L;

    /**
     * 是否对特征进行标准化（Z-score标准化）
     */
    private boolean normalize = true;

    /**
     * 特征权重数组（可为不同特征设置不同权重）
     */
    private double[] featureWeights;

    /**
     * 聚类标签名称数组
     */
    private String[] clusterLabels = {"节约型", "正常型", "高耗型"};

    public KMeansConfig() {
    }

    public KMeansConfig(int k) {
        this.k = k;
    }

    public KMeansConfig(int k, int maxIterations) {
        this.k = k;
        this.maxIterations = maxIterations;
    }

    // Getters and Setters

    public int getK() {
        return k;
    }

    public void setK(int k) {
        if (k < 1) {
            throw new IllegalArgumentException("K值必须大于0");
        }
        this.k = k;
    }

    public int getMaxIterations() {
        return maxIterations;
    }

    public void setMaxIterations(int maxIterations) {
        if (maxIterations < 1) {
            throw new IllegalArgumentException("最大迭代次数必须大于0");
        }
        this.maxIterations = maxIterations;
    }

    public double getConvergenceThreshold() {
        return convergenceThreshold;
    }

    public void setConvergenceThreshold(double convergenceThreshold) {
        this.convergenceThreshold = convergenceThreshold;
    }

    public Long getRandomSeed() {
        return randomSeed;
    }

    public void setRandomSeed(Long randomSeed) {
        this.randomSeed = randomSeed;
    }

    public boolean isNormalize() {
        return normalize;
    }

    public void setNormalize(boolean normalize) {
        this.normalize = normalize;
    }

    public double[] getFeatureWeights() {
        return featureWeights;
    }

    public void setFeatureWeights(double[] featureWeights) {
        this.featureWeights = featureWeights;
    }

    public String[] getClusterLabels() {
        return clusterLabels;
    }

    public void setClusterLabels(String[] clusterLabels) {
        this.clusterLabels = clusterLabels;
    }

    @Override
    public String toString() {
        return "KMeansConfig{" +
                "k=" + k +
                ", maxIterations=" + maxIterations +
                ", convergenceThreshold=" + convergenceThreshold +
                ", normalize=" + normalize +
                '}';
    }
}

