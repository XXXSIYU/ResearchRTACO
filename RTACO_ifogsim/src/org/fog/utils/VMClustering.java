package org.fog.utils;

import org.fog.entities.RTACOFogDevice;
import org.apache.commons.math3.ml.distance.EuclideanDistance;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;
import java.util.Set;
import java.util.Random;

public class VMClustering {

    /**
     * 對虛擬機進行分群，使用完整的 DBSCAN 算法
     *
     * @param vms 虛擬機列表
     * @return 每個虛擬機的群組標籤
     */
    public static List<Integer> cluster(List<RTACOFogDevice> vms) {
        // 建立特徵向量
        double[][] data = new double[vms.size()][5];
        for (int i = 0; i < vms.size(); i++) {
            RTACOFogDevice vm = vms.get(i);
            data[i][0] = vm.getAvailableCpu();
            data[i][1] = vm.getAvailableMemory();
            data[i][2] = vm.getTrustValue();
            data[i][3] = vm.getX();
            data[i][4] = vm.getY();
        }

        // 對特徵向量進行標準化
        double[][] normalizedData = normalizeData(data);

        // 計算歐幾里德距離
        double[][] distances = calculateDistances(normalizedData);

        // 計算平均距離
        double[] avgDistances = calculateAverageDistances(distances);

        // 設置初始 epsilon（鄰域半徑）值
        double eps = calculateEpsilon(avgDistances);

        // 執行初次 DBSCAN 分群
        List<Integer> labels = DBSCAN(normalizedData, eps, 3);

        // 根據初步結果動態調整 minSamples
        int clustersCount = (int) labels.stream().filter(label -> label != -1).distinct().count();
        int minSamples = calculateMinSamples(clustersCount);

        // 再次執行 DBSCAN 分群
        labels = DBSCAN(normalizedData, eps, minSamples);

        return labels;
    }

    /**
     * 標準化數據，使每個特徵的值範圍在 0 到 1 之間
     *
     * @param data 原始數據
     * @return 標準化後的數據
     */
    private static double[][] normalizeData(double[][] data) {
        int numFeatures = data[0].length;
        double[][] normalizedData = new double[data.length][numFeatures];

        for (int j = 0; j < numFeatures; j++) {
            double min = Double.MAX_VALUE;
            double max = Double.MIN_VALUE;

            // 找出每個特徵的最大值和最小值
            for (double[] datum : data) {
                if (datum[j] < min) min = datum[j];
                if (datum[j] > max) max = datum[j];
            }

            double range = max - min;

            // 防止除以零
            if (range == 0) {
                for (int i = 0; i < data.length; i++) {
                    normalizedData[i][j] = 0.0;
                }
            } else {
                // 標準化
                for (int i = 0; i < data.length; i++) {
                    normalizedData[i][j] = (data[i][j] - min) / range;
                }
            }
        }

        return normalizedData;
    }

    /**
     * 計算距離矩陣，使用歐幾里德距離
     *
     * @param data 標準化後的特徵數據
     * @return 距離矩陣
     */
    private static double[][] calculateDistances(double[][] data) {
        int n = data.length;
        double[][] distances = new double[n][n];
        EuclideanDistance euclideanDistance = new EuclideanDistance();

        for (int i = 0; i < n; i++) {
            for (int j = i; j < n; j++) { // 跳過已計算的距離
                if (i == j) {
                    distances[i][j] = 0.0;
                } else {
                    double dist = euclideanDistance.compute(data[i], data[j]);
                    distances[i][j] = dist;
                    distances[j][i] = dist; // 對稱距離
                }
            }
        }

        return distances;
    }

    /**
     * 計算每個 VM 的平均距離
     *
     * @param distances 距離矩陣
     * @return 每個 VM 的平均距離
     */
    private static double[] calculateAverageDistances(double[][] distances) {
        int n = distances.length;
        double[] avgDistances = new double[n];

        for (int i = 0; i < n; i++) {
            DescriptiveStatistics stats = new DescriptiveStatistics();
            for (int j = 0; j < n; j++) {
                if (i != j) {
                    stats.addValue(distances[i][j]);
                }
            }
            avgDistances[i] = stats.getMean();
        }

        return avgDistances;
    }

    /**
     * 計算初始 epsilon 值，設為平均距離的 60%
     *
     * @param avgDistances 每個 VM 的平均距離
     * @return 初始 epsilon 值
     */
    private static double calculateEpsilon(double[] avgDistances) {
        DescriptiveStatistics stats = new DescriptiveStatistics(avgDistances);
        return stats.getMean() * 0.6;
    }

    /**
     * 根據群組數量動態調整 minSamples 值
     *
     * @param clusterCount 群組數量
     * @return 調整後的 minSamples 值
     */
    private static int calculateMinSamples(int clusterCount) {
        if (clusterCount <= 0) {
            return 3; // 預設值
        }
        Random random = new Random();
        double b = 1.0 + (2.0 * random.nextDouble());
        return (int) ((clusterCount / b) + 0.5);
    }

    /**
     * 完整的 DBSCAN 分群算法實現
     *
     * @param data       標準化後的特徵數據
     * @param eps        鄰域半徑
     * @param minSamples 最小點數
     * @return 每個點的群組標籤
     */
    private static List<Integer> DBSCAN(double[][] data, double eps, int minSamples) {
        int n = data.length;
        List<Integer> labels = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            labels.add(-1); // 初始化為噪音點
        }

        int clusterId = 0;
        boolean[] visited = new boolean[n];

        for (int i = 0; i < n; i++) {
            if (visited[i]) continue;
            visited[i] = true;
            List<Integer> neighbors = getNeighbors(data, i, eps);
            if (neighbors.size() < minSamples) {
                labels.set(i, -1); // 噪音點
            } else {
                expandCluster(data, labels, i, neighbors, clusterId, eps, minSamples, visited);
                clusterId++;
            }
        }

        return labels;
    }

    /**
     * 擴展群組
     *
     * @param data         標準化後的特徵數據
     * @param labels       群組標籤列表
     * @param pointId      當前點的索引
     * @param neighbors    當前點的鄰居列表
     * @param clusterId    當前群組的 ID
     * @param eps          鄰域半徑
     * @param minSamples   最小點數
     * @param visited      已訪問標記陣列
     */
    private static void expandCluster(double[][] data, List<Integer> labels, int pointId, List<Integer> neighbors,
                                      int clusterId, double eps, int minSamples, boolean[] visited) {
        labels.set(pointId, clusterId);
        Set<Integer> seeds = new HashSet<>(neighbors);

        while (!seeds.isEmpty()) {
            Integer current = seeds.iterator().next();
            seeds.remove(current);

            if (!visited[current]) {
                visited[current] = true;
                List<Integer> currentNeighbors = getNeighbors(data, current, eps);
                if (currentNeighbors.size() >= minSamples) {
                    seeds.addAll(currentNeighbors);
                }
            }

            if (labels.get(current) == -1) {
                labels.set(current, clusterId);
            }
        }
    }

    /**
     * 獲取指定點的鄰居
     *
     * @param data    標準化後的特徵數據
     * @param pointId 指定點的索引
     * @param eps     鄰域半徑
     * @return 鄰居列表
     */
    private static List<Integer> getNeighbors(double[][] data, int pointId, double eps) {
        List<Integer> neighbors = new ArrayList<>();
        for (int i = 0; i < data.length; i++) {
            if (i == pointId) continue;
            double distance = euclideanDistance(data[pointId], data[i]);
            if (distance <= eps) {
                neighbors.add(i);
            }
        }
        return neighbors;
    }

    /**
     * 計算兩個點之間的歐幾里德距離
     *
     * @param a 第一個點的特徵向量
     * @param b 第二個點的特徵向量
     * @return 歐幾里德距離
     */
    private static double euclideanDistance(double[] a, double[] b) {
        double sum = 0.0;
        for (int i = 0; i < a.length; i++) {
            sum += Math.pow(a[i] - b[i], 2);
        }
        return Math.sqrt(sum);
    }
}
