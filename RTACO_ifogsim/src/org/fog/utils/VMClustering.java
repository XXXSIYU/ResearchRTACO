package org.fog.utils;

import org.fog.entities.RTACOFogDevice;
import org.apache.commons.math3.ml.distance.EuclideanDistance;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class VMClustering {

    // 進行分群的方法
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

    // 標準化數據
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

            // 標準化
            for (int i = 0; i < data.length; i++) {
                normalizedData[i][j] = (data[i][j] - min) / range;
            }
        }

        return normalizedData;
    }

    // 計算距離矩陣
    private static double[][] calculateDistances(double[][] data) {
        int n = data.length;
        double[][] distances = new double[n][n];
        EuclideanDistance euclideanDistance = new EuclideanDistance();

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                distances[i][j] = euclideanDistance.compute(data[i], data[j]);
            }
        }

        return distances;
    }

    // 計算每個 VM 的平均距離
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

    // 計算初始 epsilon 值
    private static double calculateEpsilon(double[] avgDistances) {
        DescriptiveStatistics stats = new DescriptiveStatistics(avgDistances);
        return stats.getMean() * 0.6;
    }

    // 計算動態調整的 minSamples 值
    private static int calculateMinSamples(int clusterCount) {
        Random random = new Random();
        double b = 1.0 + (2.0 * random.nextDouble());
        return (int) ((clusterCount / b) + 0.5);
    }

    // DBSCAN 分群算法
    private static List<Integer> DBSCAN(double[][] data, double eps, int minSamples) {
        // 使用簡單的 DBSCAN 實現，返回聚類標籤
        // 這裡可以調整成更複雜的 DBSCAN 算法實現
        List<Integer> labels = new ArrayList<>();
        // 初始化所有 VM 為未分類
        for (int i = 0; i < data.length; i++) {
            labels.add(-1);
        }

        // 簡單版本的 DBSCAN，可以根據需求改進
        // 注意這裡省略了具體的實現細節，可以使用開源庫（例如 ELKI）來完成
        return labels;
    }
}
