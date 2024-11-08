package org.fog.utils;

import org.fog.entities.RTACOFogDevice;
import org.fog.entities.Task;
import java.util.List;

/**
 * FormulaUtils 類包含了在 Fog 計算環境中計算任務執行時間、能量消耗、
 * 負載平衡和資源利用率的靜態方法。
 */
public class FormulaUtils {

    /**
     * 計算上行傳輸時間。
     * 
     * @param task 要執行的任務
     * @param vm 執行任務的虛擬機（Fog 設備）
     * @return 上行傳輸時間（單位：秒）
     */
    public static double uplinkTransmissionTime(Task task, RTACOFogDevice vm) {
        if (vm.getTransmissionRate() > 0) {
            return task.getTaskSize() / vm.getTransmissionRate();
        } else {
            // 若傳輸速率為零，返回極大值或處理方式
            return Double.MAX_VALUE;
        }
    }

    /**
     * 計算處理時間。
     * 
     * @param task 要執行的任務
     * @param vm 執行任務的虛擬機（Fog 設備）
     * @return 處理時間（單位：秒）
     */
    public static double processingTime(Task task, RTACOFogDevice vm) {
        if (vm.getProcessingSpeed() > 0) {
            return task.getLength() / vm.getProcessingSpeed();
        } else {
            // 若處理速度為零，返回極大值或處理方式
            return Double.MAX_VALUE;
        }
    }

    /**
     * 計算下行傳輸時間。
     * 
     * @param task 要執行的任務
     * @param vm 執行任務的虛擬機（Fog 設備）
     * @return 下行傳輸時間（單位：秒）
     */
    public static double downlinkTransmissionTime(Task task, RTACOFogDevice vm) {
        if (vm.getTransmissionRate() > 0) {
            return task.getResultSize() / vm.getTransmissionRate();
        } else {
            // 若傳輸速率為零，返回極大值或處理方式
            return Double.MAX_VALUE;
        }
    }

    /**
     * 計算任務的總完成時間。
     * 
     * @param task 要執行的任務
     * @param vm 執行任務的虛擬機（Fog 設備）
     * @return 總完成時間（單位：秒）
     */
    public static double completionTime(Task task, RTACOFogDevice vm) {
        double uplinkTime = uplinkTransmissionTime(task, vm);
        double procTime = processingTime(task, vm);
        double downlinkTime = downlinkTransmissionTime(task, vm);
        return uplinkTime + procTime + downlinkTime;
    }

    /**
     * 計算處理能量消耗。
     * 
     * @param task 要執行的任務
     * @param vm 執行任務的虛擬機（Fog 設備）
     * @return 處理能量消耗（單位：焦耳）
     */
    public static double energyConsumptionProcessing(Task task, RTACOFogDevice vm) {
        if (vm.getProcessingSpeed() > 0) {
            return Constant.ENERGY_CONSUMPTION_PROCESSING * (task.getLength() / vm.getProcessingSpeed());
        } else {
            // 若處理速度為零，返回極大值或處理方式
            return Double.MAX_VALUE;
        }
    }

    /**
     * 計算傳輸能量消耗。
     * 
     * @param task 要執行的任務
     * @param vm 執行任務的虛擬機（Fog 設備）
     * @return 傳輸能量消耗（單位：焦耳）
     */
    public static double energyConsumptionTransmission(Task task, RTACOFogDevice vm) {
        return (task.getTaskSize() + task.getResultSize()) * Constant.ENERGY_CONSUMPTION_TRANSMISSION;
    }

    /**
     * 計算總能量消耗。
     * 
     * @param task 要執行的任務
     * @param vm 執行任務的虛擬機（Fog 設備）
     * @return 總能量消耗（單位：焦耳）
     */
    public static double energyConsumption(Task task, RTACOFogDevice vm) {
        double processingEnergy = energyConsumptionProcessing(task, vm);
        double transmissionEnergy = energyConsumptionTransmission(task, vm);
        return processingEnergy + transmissionEnergy;
    }

    /**
     * 計算負載平衡指標（標準差）。
     * 
     * @param vms 虛擬機列表
     * @return 負載平衡指標
     */
    public static double loadBalance(List<RTACOFogDevice> vms) {
        double[] vmLoads = new double[vms.size()];
        double totalLoad = 0;

        for (int i = 0; i < vms.size(); i++) {
            RTACOFogDevice vm = vms.get(i);
            double cpuLoad = (vm.getTotalCpu() - vm.getAvailableCpu()) / vm.getTotalCpu();
            double memoryLoad = (vm.getTotalMemory() - vm.getAvailableMemory()) / vm.getTotalMemory();
            vmLoads[i] = (cpuLoad + memoryLoad) / 2; // 平均負載
            totalLoad += vmLoads[i];
        }

        double averageLoad = totalLoad / vms.size();
        double sumOfSquaredDifferences = 0;

        for (double load : vmLoads) {
            sumOfSquaredDifferences += Math.pow(load - averageLoad, 2);
        }

        return Math.sqrt(sumOfSquaredDifferences / vms.size());
    }

    /**
     * 計算資源利用率。
     * 
     * @param vms 虛擬機列表
     * @return 平均資源利用率（百分比）
     */
    public static double resourceUtilization(List<RTACOFogDevice> vms) {
        double totalUtilization = 0;

        for (RTACOFogDevice vm : vms) {
            double cpuUtilization = ((vm.getTotalCpu() - vm.getAvailableCpu()) / vm.getTotalCpu()) * 100;
            double memoryUtilization = ((vm.getTotalMemory() - vm.getAvailableMemory()) / vm.getTotalMemory()) * 100;
            totalUtilization += (cpuUtilization + memoryUtilization) / 2;
        }

        return totalUtilization / vms.size();
    }
}
