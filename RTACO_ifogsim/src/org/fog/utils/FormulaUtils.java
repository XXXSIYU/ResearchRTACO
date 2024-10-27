package org.fog.utils;

import org.fog.entities.RTACOFogDevice;
import org.fog.entities.Task;
import java.util.List;

public class FormulaUtils {

    // 計算上行傳輸時間
    public static double uplinkTransmissionTime(Task task, RTACOFogDevice vm) {
        return task.getTaskSize() / vm.getTransmissionRate();
    }

    // 計算處理時間
    public static double processingTime(Task task, RTACOFogDevice vm) {
        return task.getLength() / vm.getProcessingSpeed();
    }

    // 計算下行傳輸時間
    public static double downlinkTime(Task task, RTACOFogDevice vm) {
        return task.getResultSize() / vm.getTransmissionRate();
    }

    // 計算總完成時間
    public static double completionTime(Task task, RTACOFogDevice vm) {
        return uplinkTransmissionTime(task, vm) + processingTime(task, vm) + downlinkTime(task, vm);
    }

    // 計算處理能量消耗
    public static double energyConsumptionProcessing(Task task, RTACOFogDevice vm) {
        return Constant.ENERGY_CONSUMPTION_PROCESSING * task.getLength() / vm.getProcessingSpeed();
    }

    // 計算傳輸能量消耗
    public static double energyConsumptionTransmission(Task task, RTACOFogDevice vm) {
        return (task.getTaskSize() + task.getResultSize()) * Constant.ENERGY_CONSUMPTION_TRANSMISSION;
    }

    // 計算總能量消耗
    public static double energyConsumption(Task task, RTACOFogDevice vm) {
        return energyConsumptionProcessing(task, vm) + energyConsumptionTransmission(task, vm);
    }

    // 計算負載平衡
    public static double loadBalance(List<RTACOFogDevice> vms) {
        double[] vmLoads = new double[vms.size()];
        double totalLoad = 0;

        for (int i = 0; i < vms.size(); i++) {
            RTACOFogDevice vm = vms.get(i);
            double cpuLoad = (vm.getTotalCpu() - vm.getAvailableCpu()) / vm.getTotalCpu();
            double memoryLoad = (vm.getTotalMemory() - vm.getAvailableMemory()) / vm.getTotalMemory();
            vmLoads[i] = cpuLoad + memoryLoad;
            totalLoad += vmLoads[i];
        }

        double averageLoad = totalLoad / vms.size();
        double sumOfSquaredDifferences = 0;

        for (double load : vmLoads) {
            sumOfSquaredDifferences += Math.pow(load - averageLoad, 2);
        }

        return Math.sqrt(sumOfSquaredDifferences / vms.size());
    }

    // 計算資源利用率
    public static double resourceUtilization(List<RTACOFogDevice> vms) {
        double totalUtilization = 0;

        for (RTACOFogDevice vm : vms) {
            double cpuUtilization = (vm.getTotalCpu() - vm.getAvailableCpu()) / vm.getTotalCpu() * 100;
            double memoryUtilization = (vm.getTotalMemory() - vm.getAvailableMemory()) / vm.getTotalMemory() * 100;
            totalUtilization += (cpuUtilization + memoryUtilization) / 2;
        }

        return totalUtilization / vms.size();
    }
}
