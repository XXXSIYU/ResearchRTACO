package org.fog.utils;

import org.fog.entities.RTACOFogDevice;
import java.util.ArrayList;
import java.util.List;

public class VMSelection {

    // 正規化特徵的方法
    private static double[][] normalizeFeatures(double[][] X) {
        int numRows = X.length;
        int numCols = X[0].length;
        double[] minValues = new double[numCols];
        double[] maxValues = new double[numCols];
        double[][] XNormalized = new double[numRows][numCols];

        // 找到最小值和最大值
        for (int j = 0; j < numCols; j++) {
            minValues[j] = Double.POSITIVE_INFINITY;
            maxValues[j] = Double.NEGATIVE_INFINITY;
            for (int i = 0; i < numRows; i++) {
                if (X[i][j] < minValues[j]) minValues[j] = X[i][j];
                if (X[i][j] > maxValues[j]) maxValues[j] = X[i][j];
            }
        }

        // 進行正規化
        for (int i = 0; i < numRows; i++) {
            for (int j = 0; j < numCols; j++) {
                double range = maxValues[j] - minValues[j];
                if (range == 0) {
                    XNormalized[i][j] = 0.0;
                } else {
                    XNormalized[i][j] = (X[i][j] - minValues[j]) / range;
                }
            }
        }

        return XNormalized;
    }

    // 選擇合適的 VM
    public static List<RTACOFogDevice> selectVMs(List<RTACOFogDevice> vms) {
        int numVms = vms.size();
        double[][] features = new double[numVms][3];

        // 獲取每個 VM 的可用 CPU、記憶體和信任值
        for (int i = 0; i < numVms; i++) {
            RTACOFogDevice vm = vms.get(i);
            features[i][0] = vm.getAvailableCpu();
            features[i][1] = vm.getAvailableMemory();
            features[i][2] = vm.getTrustValue();
        }

        // 正規化特徵
        double[][] normalizedFeatures = normalizeFeatures(features);

        // 權重
        double[] weights = {0.25, 0.25, 0.5};
        double[] scores = new double[numVms];

        // 計算每個 VM 的分數
        for (int i = 0; i < numVms; i++) {
            scores[i] = 0;
            for (int j = 0; j < 3; j++) {
                scores[i] += normalizedFeatures[i][j] * weights[j];
            }
        }

        // 計算分數閾值（20 百分位數）
        double threshold = calculatePercentile(scores, 20);

        // 選擇分數高於閾值的 VM
        List<RTACOFogDevice> selectedVMs = new ArrayList<>();
        for (int i = 0; i < numVms; i++) {
            if (scores[i] > threshold) {
                selectedVMs.add(vms.get(i));
            }
        }

        return selectedVMs;
    }

    // 計算百分位數的方法
    private static double calculatePercentile(double[] scores, double percentile) {
        double[] sortedScores = scores.clone();
        java.util.Arrays.sort(sortedScores);
        int n = sortedScores.length;
        double rank = percentile / 100.0 * (n - 1);
        int lowerIndex = (int) Math.floor(rank);
        int upperIndex = (int) Math.ceil(rank);
        if (lowerIndex == upperIndex) {
            return sortedScores[lowerIndex];
        } else {
            double weight = rank - lowerIndex;
            return sortedScores[lowerIndex] * (1 - weight) + sortedScores[upperIndex] * weight;
        }
    }
}
