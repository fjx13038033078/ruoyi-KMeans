package com.ruoyi.common.kmeans;

import java.util.*;

/**
 * K-Means聚类算法核心实现
 *
 * @Author 范佳兴
 * @Date 2024/12/18
 */
public class KMeansAlgorithm {

    /**
     * 执行K-Means聚类
     *
     * @param dataPoints 数据点列表
     * @param config     算法配置
     * @return 聚类结果
     */
    public static ClusterResult cluster(List<DataPoint> dataPoints, KMeansConfig config) {
        if (dataPoints == null || dataPoints.isEmpty()) {
            throw new IllegalArgumentException("数据点列表不能为空");
        }
        if (dataPoints.size() < config.getK()) {
            throw new IllegalArgumentException("数据点数量必须大于等于K值");
        }

        int k = config.getK();
        int maxIterations = config.getMaxIterations();
        double convergenceThreshold = config.getConvergenceThreshold();

        // 标准化特征
        if (config.isNormalize()) {
            normalizeFeatures(dataPoints);
        }

        int featureDim = dataPoints.get(0).getFeatures().length;

        // 初始化聚类中心（K-Means++）
        double[][] centroids = initializeCentroidsKMeansPlusPlus(dataPoints, k, config.getRandomSeed());

        ClusterResult result = new ClusterResult(k);
        boolean converged = false;
        int iteration = 0;

        for (iteration = 0; iteration < maxIterations; iteration++) {
            // 清空簇
            List<List<DataPoint>> clusters = new ArrayList<>();
            for (int i = 0; i < k; i++) {
                clusters.add(new ArrayList<>());
            }

            // 分配数据点到最近的聚类中心
            for (DataPoint point : dataPoints) {
                int nearestCluster = findNearestCluster(point, centroids, config.getFeatureWeights());
                point.setClusterId(nearestCluster);
                point.setDistanceToCenter(calculateDistance(point.getFeatures(), centroids[nearestCluster], config.getFeatureWeights()));
                clusters.get(nearestCluster).add(point);
            }

            // 计算新的聚类中心
            double[][] newCentroids = new double[k][featureDim];
            double maxCentroidShift = 0;

            for (int i = 0; i < k; i++) {
                if (clusters.get(i).isEmpty()) {
                    // 如果簇为空，保持原中心或重新随机选择
                    newCentroids[i] = centroids[i];
                } else {
                    newCentroids[i] = calculateCentroid(clusters.get(i), featureDim);
                    double shift = calculateDistance(centroids[i], newCentroids[i], null);
                    maxCentroidShift = Math.max(maxCentroidShift, shift);
                }
            }

            centroids = newCentroids;

            // 检查是否收敛
            if (maxCentroidShift < convergenceThreshold) {
                converged = true;
                break;
            }

            result.setClusters(clusters);
        }

        // 设置结果
        result.setCentroids(centroids);
        result.setIterations(iteration + 1);
        result.setConverged(converged);

        // 计算SSE
        double sse = calculateSSE(dataPoints, centroids);
        result.setSse(sse);

        // 计算轮廓系数
        double silhouette = calculateSilhouetteScore(dataPoints, result.getClusters());
        result.setSilhouetteScore(silhouette);

        // 根据平均用水量对簇进行排序并分配标签
        assignClusterLabels(result, config.getClusterLabels());

        // 计算簇统计信息
        calculateClusterStatistics(result);

        return result;
    }

    /**
     * K-Means++初始化聚类中心
     */
    private static double[][] initializeCentroidsKMeansPlusPlus(List<DataPoint> dataPoints, int k, Long seed) {
        Random random = seed != null ? new Random(seed) : new Random();
        int n = dataPoints.size();
        int featureDim = dataPoints.get(0).getFeatures().length;
        double[][] centroids = new double[k][featureDim];

        // 随机选择第一个中心
        int firstIndex = random.nextInt(n);
        centroids[0] = Arrays.copyOf(dataPoints.get(firstIndex).getFeatures(), featureDim);

        // 选择剩余的中心
        for (int i = 1; i < k; i++) {
            double[] distances = new double[n];
            double totalDistance = 0;

            for (int j = 0; j < n; j++) {
                double minDist = Double.MAX_VALUE;
                for (int c = 0; c < i; c++) {
                    double dist = calculateDistance(dataPoints.get(j).getFeatures(), centroids[c], null);
                    minDist = Math.min(minDist, dist);
                }
                distances[j] = minDist * minDist;
                totalDistance += distances[j];
            }

            // 按概率选择下一个中心
            double r = random.nextDouble() * totalDistance;
            double cumulative = 0;
            for (int j = 0; j < n; j++) {
                cumulative += distances[j];
                if (cumulative >= r) {
                    centroids[i] = Arrays.copyOf(dataPoints.get(j).getFeatures(), featureDim);
                    break;
                }
            }
        }

        return centroids;
    }

    /**
     * 找到最近的聚类中心
     */
    private static int findNearestCluster(DataPoint point, double[][] centroids, double[] weights) {
        int nearest = 0;
        double minDistance = Double.MAX_VALUE;

        for (int i = 0; i < centroids.length; i++) {
            double distance = calculateDistance(point.getFeatures(), centroids[i], weights);
            if (distance < minDistance) {
                minDistance = distance;
                nearest = i;
            }
        }

        return nearest;
    }

    /**
     * 计算距离（支持加权）
     */
    private static double calculateDistance(double[] a, double[] b, double[] weights) {
        double sum = 0;
        for (int i = 0; i < a.length; i++) {
            double weight = (weights != null && i < weights.length) ? weights[i] : 1.0;
            sum += weight * Math.pow(a[i] - b[i], 2);
        }
        return Math.sqrt(sum);
    }

    /**
     * 计算簇的中心点
     */
    private static double[] calculateCentroid(List<DataPoint> cluster, int featureDim) {
        double[] centroid = new double[featureDim];
        for (DataPoint point : cluster) {
            for (int i = 0; i < featureDim; i++) {
                centroid[i] += point.getFeatures()[i];
            }
        }
        for (int i = 0; i < featureDim; i++) {
            centroid[i] /= cluster.size();
        }
        return centroid;
    }

    /**
     * 特征标准化（Z-score）
     */
    private static void normalizeFeatures(List<DataPoint> dataPoints) {
        if (dataPoints.isEmpty()) return;

        int featureDim = dataPoints.get(0).getFeatures().length;
        int n = dataPoints.size();

        // 计算每个特征的均值和标准差
        double[] means = new double[featureDim];
        double[] stds = new double[featureDim];

        for (DataPoint point : dataPoints) {
            for (int i = 0; i < featureDim; i++) {
                means[i] += point.getFeatures()[i];
            }
        }
        for (int i = 0; i < featureDim; i++) {
            means[i] /= n;
        }

        for (DataPoint point : dataPoints) {
            for (int i = 0; i < featureDim; i++) {
                stds[i] += Math.pow(point.getFeatures()[i] - means[i], 2);
            }
        }
        for (int i = 0; i < featureDim; i++) {
            stds[i] = Math.sqrt(stds[i] / n);
        }

        // 标准化
        for (DataPoint point : dataPoints) {
            double[] features = point.getFeatures();
            for (int i = 0; i < featureDim; i++) {
                if (stds[i] > 0) {
                    features[i] = (features[i] - means[i]) / stds[i];
                }
            }
        }
    }

    /**
     * 计算SSE（误差平方和）
     */
    private static double calculateSSE(List<DataPoint> dataPoints, double[][] centroids) {
        double sse = 0;
        for (DataPoint point : dataPoints) {
            int clusterId = point.getClusterId();
            if (clusterId >= 0 && clusterId < centroids.length) {
                sse += Math.pow(calculateDistance(point.getFeatures(), centroids[clusterId], null), 2);
            }
        }
        return sse;
    }

    /**
     * 计算轮廓系数
     */
    private static double calculateSilhouetteScore(List<DataPoint> dataPoints, List<List<DataPoint>> clusters) {
        if (clusters.size() <= 1) return 0;

        double totalSilhouette = 0;
        int count = 0;

        for (DataPoint point : dataPoints) {
            int clusterId = point.getClusterId();
            if (clusterId < 0 || clusters.get(clusterId).size() <= 1) continue;

            // 计算a(i) - 与同簇其他点的平均距离
            double a = 0;
            for (DataPoint other : clusters.get(clusterId)) {
                if (other != point) {
                    a += point.distanceTo(other);
                }
            }
            a /= (clusters.get(clusterId).size() - 1);

            // 计算b(i) - 与最近其他簇的平均距离
            double b = Double.MAX_VALUE;
            for (int i = 0; i < clusters.size(); i++) {
                if (i != clusterId && !clusters.get(i).isEmpty()) {
                    double avgDist = 0;
                    for (DataPoint other : clusters.get(i)) {
                        avgDist += point.distanceTo(other);
                    }
                    avgDist /= clusters.get(i).size();
                    b = Math.min(b, avgDist);
                }
            }

            // 计算轮廓系数
            double s = (b - a) / Math.max(a, b);
            totalSilhouette += s;
            count++;
        }

        return count > 0 ? totalSilhouette / count : 0;
    }

    /**
     * 根据平均用水量分配聚类标签
     */
    private static void assignClusterLabels(ClusterResult result, String[] defaultLabels) {
        List<List<DataPoint>> clusters = result.getClusters();
        int k = clusters.size();

        // 计算每个簇的平均用水量（假设第一个特征是日均用水量）
        List<double[]> clusterAvgs = new ArrayList<>();
        for (int i = 0; i < k; i++) {
            double avgUsage = 0;
            if (!clusters.get(i).isEmpty()) {
                for (DataPoint p : clusters.get(i)) {
                    if (p.getOriginalFeatures() != null && p.getOriginalFeatures().length > 0) {
                        avgUsage += p.getOriginalFeatures()[0];
                    }
                }
                avgUsage /= clusters.get(i).size();
            }
            clusterAvgs.add(new double[]{i, avgUsage});
        }

        // 按平均用水量排序
        clusterAvgs.sort(Comparator.comparingDouble(a -> a[1]));

        // 分配标签
        String[] labels = new String[k];
        for (int i = 0; i < k; i++) {
            int clusterId = (int) clusterAvgs.get(i)[0];
            if (defaultLabels != null && i < defaultLabels.length) {
                labels[clusterId] = defaultLabels[i];
            } else {
                labels[clusterId] = "类型" + (i + 1);
            }
        }

        result.setClusterLabels(labels);
    }

    /**
     * 计算簇统计信息
     */
    private static void calculateClusterStatistics(ClusterResult result) {
        List<ClusterResult.ClusterStatistics> statsList = new ArrayList<>();
        List<List<DataPoint>> clusters = result.getClusters();
        String[] labels = result.getClusterLabels();

        for (int i = 0; i < clusters.size(); i++) {
            ClusterResult.ClusterStatistics stats = new ClusterResult.ClusterStatistics();
            stats.setClusterId(i);
            stats.setClusterLabel(labels != null && i < labels.length ? labels[i] : "类型" + (i + 1));
            stats.setSize(clusters.get(i).size());
            stats.setCentroid(result.getCentroid(i));

            // 计算平均特征（使用原始特征）
            if (!clusters.get(i).isEmpty()) {
                int featureDim = clusters.get(i).get(0).getOriginalFeatures().length;
                double[] avgFeatures = new double[featureDim];
                double avgDist = 0;

                for (DataPoint p : clusters.get(i)) {
                    for (int j = 0; j < featureDim; j++) {
                        avgFeatures[j] += p.getOriginalFeatures()[j];
                    }
                    avgDist += p.getDistanceToCenter();
                }

                for (int j = 0; j < featureDim; j++) {
                    avgFeatures[j] /= clusters.get(i).size();
                }
                stats.setAvgFeatures(avgFeatures);
                stats.setAvgDistanceToCenter(avgDist / clusters.get(i).size());
            }

            statsList.add(stats);
        }

        result.setClusterStatistics(statsList);
    }
}

