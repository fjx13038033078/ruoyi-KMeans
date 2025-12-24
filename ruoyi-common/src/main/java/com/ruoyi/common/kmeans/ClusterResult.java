package com.ruoyi.common.kmeans;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * K-Means聚类结果类
 *
 * @Author 范佳兴
 * @Date 2024/12/18
 */
public class ClusterResult implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 聚类数量
     */
    private int k;

    /**
     * 实际迭代次数
     */
    private int iterations;

    /**
     * 是否收敛
     */
    private boolean converged;

    /**
     * 聚类中心点
     */
    private double[][] centroids;

    /**
     * 每个簇包含的数据点
     */
    private List<List<DataPoint>> clusters;

    /**
     * SSE（Sum of Squared Errors，误差平方和）
     */
    private double sse;

    /**
     * 轮廓系数（-1到1，越大越好）
     */
    private double silhouetteScore;

    /**
     * 聚类标签
     */
    private String[] clusterLabels;

    /**
     * 每个簇的统计信息
     */
    private List<ClusterStatistics> clusterStatistics;

    public ClusterResult() {
        this.clusters = new ArrayList<>();
        this.clusterStatistics = new ArrayList<>();
    }

    public ClusterResult(int k) {
        this.k = k;
        this.clusters = new ArrayList<>();
        this.clusterStatistics = new ArrayList<>();
        for (int i = 0; i < k; i++) {
            clusters.add(new ArrayList<>());
        }
    }

    /**
     * 获取指定簇的数据点
     */
    public List<DataPoint> getCluster(int clusterId) {
        if (clusterId < 0 || clusterId >= clusters.size()) {
            return new ArrayList<>();
        }
        return clusters.get(clusterId);
    }

    /**
     * 获取指定簇的大小
     */
    public int getClusterSize(int clusterId) {
        return getCluster(clusterId).size();
    }

    /**
     * 获取指定簇的中心点
     */
    public double[] getCentroid(int clusterId) {
        if (centroids == null || clusterId < 0 || clusterId >= centroids.length) {
            return null;
        }
        return centroids[clusterId];
    }

    /**
     * 添加数据点到指定簇
     */
    public void addToCluster(int clusterId, DataPoint point) {
        if (clusterId >= 0 && clusterId < clusters.size()) {
            clusters.get(clusterId).add(point);
        }
    }

    // Getters and Setters

    public int getK() {
        return k;
    }

    public void setK(int k) {
        this.k = k;
    }

    public int getIterations() {
        return iterations;
    }

    public void setIterations(int iterations) {
        this.iterations = iterations;
    }

    public boolean isConverged() {
        return converged;
    }

    public void setConverged(boolean converged) {
        this.converged = converged;
    }

    public double[][] getCentroids() {
        return centroids;
    }

    public void setCentroids(double[][] centroids) {
        this.centroids = centroids;
    }

    public List<List<DataPoint>> getClusters() {
        return clusters;
    }

    public void setClusters(List<List<DataPoint>> clusters) {
        this.clusters = clusters;
    }

    public double getSse() {
        return sse;
    }

    public void setSse(double sse) {
        this.sse = sse;
    }

    public double getSilhouetteScore() {
        return silhouetteScore;
    }

    public void setSilhouetteScore(double silhouetteScore) {
        this.silhouetteScore = silhouetteScore;
    }

    public String[] getClusterLabels() {
        return clusterLabels;
    }

    public void setClusterLabels(String[] clusterLabels) {
        this.clusterLabels = clusterLabels;
    }

    public List<ClusterStatistics> getClusterStatistics() {
        return clusterStatistics;
    }

    public void setClusterStatistics(List<ClusterStatistics> clusterStatistics) {
        this.clusterStatistics = clusterStatistics;
    }

    @Override
    public String toString() {
        return "ClusterResult{" +
                "k=" + k +
                ", iterations=" + iterations +
                ", converged=" + converged +
                ", sse=" + sse +
                ", silhouetteScore=" + silhouetteScore +
                ", clusterLabels=" + Arrays.toString(clusterLabels) +
                '}';
    }

    /**
     * 簇统计信息内部类
     */
    public static class ClusterStatistics implements Serializable {
        private static final long serialVersionUID = 1L;

        private int clusterId;
        private String clusterLabel;
        private int size;
        private double[] centroid;
        private double[] avgFeatures;
        private double avgDistanceToCenter;

        public ClusterStatistics() {
        }

        public int getClusterId() {
            return clusterId;
        }

        public void setClusterId(int clusterId) {
            this.clusterId = clusterId;
        }

        public String getClusterLabel() {
            return clusterLabel;
        }

        public void setClusterLabel(String clusterLabel) {
            this.clusterLabel = clusterLabel;
        }

        public int getSize() {
            return size;
        }

        public void setSize(int size) {
            this.size = size;
        }

        public double[] getCentroid() {
            return centroid;
        }

        public void setCentroid(double[] centroid) {
            this.centroid = centroid;
        }

        public double[] getAvgFeatures() {
            return avgFeatures;
        }

        public void setAvgFeatures(double[] avgFeatures) {
            this.avgFeatures = avgFeatures;
        }

        public double getAvgDistanceToCenter() {
            return avgDistanceToCenter;
        }

        public void setAvgDistanceToCenter(double avgDistanceToCenter) {
            this.avgDistanceToCenter = avgDistanceToCenter;
        }
    }
}

