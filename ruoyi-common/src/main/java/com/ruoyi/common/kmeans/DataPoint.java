package com.ruoyi.common.kmeans;

import java.io.Serializable;
import java.util.Arrays;

/**
 * 聚类数据点类
 *
 * @Author 范佳兴
 * @Date 2024/12/18
 */
public class DataPoint implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 数据点ID（对应用户ID或特征ID）
     */
    private Long id;

    /**
     * 特征向量
     */
    private double[] features;

    /**
     * 原始特征向量（标准化前）
     */
    private double[] originalFeatures;

    /**
     * 所属聚类簇ID
     */
    private int clusterId = -1;

    /**
     * 到所属聚类中心的距离
     */
    private double distanceToCenter;

    /**
     * 附加标签（如用户名）
     */
    private String label;

    public DataPoint() {
    }

    public DataPoint(Long id, double[] features) {
        this.id = id;
        this.features = features;
        this.originalFeatures = Arrays.copyOf(features, features.length);
    }

    public DataPoint(Long id, double[] features, String label) {
        this.id = id;
        this.features = features;
        this.originalFeatures = Arrays.copyOf(features, features.length);
        this.label = label;
    }

    /**
     * 计算与另一个数据点的欧几里得距离
     *
     * @param other 另一个数据点
     * @return 欧几里得距离
     */
    public double distanceTo(DataPoint other) {
        return distanceTo(other.getFeatures());
    }

    /**
     * 计算与指定特征向量的欧几里得距离
     *
     * @param otherFeatures 特征向量
     * @return 欧几里得距离
     */
    public double distanceTo(double[] otherFeatures) {
        if (this.features.length != otherFeatures.length) {
            throw new IllegalArgumentException("特征维度不匹配");
        }
        double sum = 0;
        for (int i = 0; i < this.features.length; i++) {
            sum += Math.pow(this.features[i] - otherFeatures[i], 2);
        }
        return Math.sqrt(sum);
    }

    /**
     * 计算带权重的欧几里得距离
     *
     * @param otherFeatures 特征向量
     * @param weights       权重数组
     * @return 加权欧几里得距离
     */
    public double weightedDistanceTo(double[] otherFeatures, double[] weights) {
        if (this.features.length != otherFeatures.length || this.features.length != weights.length) {
            throw new IllegalArgumentException("特征维度或权重维度不匹配");
        }
        double sum = 0;
        for (int i = 0; i < this.features.length; i++) {
            sum += weights[i] * Math.pow(this.features[i] - otherFeatures[i], 2);
        }
        return Math.sqrt(sum);
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public double[] getFeatures() {
        return features;
    }

    public void setFeatures(double[] features) {
        this.features = features;
    }

    public double[] getOriginalFeatures() {
        return originalFeatures;
    }

    public void setOriginalFeatures(double[] originalFeatures) {
        this.originalFeatures = originalFeatures;
    }

    public int getClusterId() {
        return clusterId;
    }

    public void setClusterId(int clusterId) {
        this.clusterId = clusterId;
    }

    public double getDistanceToCenter() {
        return distanceToCenter;
    }

    public void setDistanceToCenter(double distanceToCenter) {
        this.distanceToCenter = distanceToCenter;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return "DataPoint{" +
                "id=" + id +
                ", clusterId=" + clusterId +
                ", features=" + Arrays.toString(features) +
                ", label='" + label + '\'' +
                '}';
    }
}

