package org.fog.utils;

import org.fog.entities.RTACOFogDevice;
import org.fog.entities.Task;
import java.util.Map;

public class ObjectiveFunction {

    // 修改後的 objectiveFunction 方法，增加 vmSelectedTimes 參數
    public static double objectiveFunction(Task task, RTACOFogDevice vm, Map<Integer, Integer> vmSelectedTimes) {
        // 計算 VM 被選擇的次數
        int load = vmSelectedTimes.getOrDefault(vm.getId(), 0);
        
        // 計算 BRR（比例最大資源需求）
        double brr = Math.max(
            task.getCpuRequirement() / vm.getAvailableCpu(), 
            task.getMemoryRequirement() / vm.getAvailableMemory()
        );

        // 計算 CPU 和記憶體比例
        double cpuMemoryRatio = (0.5 * task.getCpuRequirement() + 0.5 * task.getMemoryRequirement()) / 
                                (0.5 * vm.getTotalCpu() + 0.5 * vm.getTotalMemory()) * 100;
        
        // 計算完成時間和能量消耗
        double completionTime = FormulaUtils.completionTime(task, vm);
        double energyConsumption = FormulaUtils.energyConsumption(task, vm);

        // 使用更新後的公式計算目標函數
        return Constant.APSO_ALPHA * cpuMemoryRatio 
               - Constant.APSO_BETA * completionTime 
               - Constant.APSO_GAMMA * energyConsumption 
               + Constant.GAMMA * load 
               + Constant.DELTA * brr;
    }
    
    // 新增 sysCost 方法
    public static double sysCost(Task task, RTACOFogDevice vm, double minCompletionTime, double maxCompletionTime, double minEnergyConsumption, double maxEnergyConsumption) {
        // 標準化完成時間和能量消耗
        double normalizedCompletionTime = normalize(FormulaUtils.completionTime(task, vm), minCompletionTime, maxCompletionTime);
        double normalizedEnergyConsumption = normalize(FormulaUtils.energyConsumption(task, vm), minEnergyConsumption, maxEnergyConsumption);

        // 計算系統成本
        return 0.5 * normalizedCompletionTime + 0.5 * normalizedEnergyConsumption;
    }

    // 標準化方法
    private static double normalize(double value, double minValue, double maxValue) {
        return (value - minValue) / (maxValue - minValue);
    }
}
