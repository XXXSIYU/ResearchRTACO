package org.fog.test.perfeval;

import java.util.List;
import java.util.ArrayList;
import java.util.Random;
import java.util.Calendar;

import org.fog.entities.RTACOFogDevice;
import org.fog.entities.Task;
import org.fog.placement.RTACOPlacement;
import org.fog.utils.VMGenerator;
import org.fog.utils.VMSelection;
import org.fog.utils.TaskGenerator;
import org.fog.utils.VMClustering;
import org.fog.utils.FormulaUtils;
import org.fog.utils.Pair;
import org.cloudbus.cloudsim.core.CloudSim;

/**
 * MainRTACOSimulation 類用於執行 RTACOPlacement 的模擬。
 */
public class MainRTACOSimulation {

    public static void main(String[] args) throws Exception {
        // 初始化 CloudSim
        int numUser = 1; // 使用者數量
        Calendar calendar = Calendar.getInstance();
        boolean traceFlag = false; // 記錄事件

        // 初始化 CloudSim 模擬環境
        CloudSim.init(numUser, calendar, traceFlag);

        // Step 1: 生成虛擬機和任務
        int numVMs = 200; // 虛擬機數量
        int numTasks = 500; // 任務數量

        // 生成虛擬機
        List<RTACOFogDevice> vms = VMGenerator.generateRTACOFogDevices(numVMs);

        // 生成任務
        List<Task> tasks = TaskGenerator.generateTasks(numTasks);

        // Step 2: VM 選擇
        List<RTACOFogDevice> selectedVMs = VMSelection.selectRTACOFogDevices(vms);
        selectedVMs.sort((vm1, vm2) -> Integer.compare(vm1.getId(), vm2.getId()));

        System.out.println("\n選擇的虛擬機：");
        for (RTACOFogDevice vm : selectedVMs) {
            System.out.println("VM ID: " + vm.getId() + ", 信任值: " + vm.getTrustValue());
        }

        // 對虛擬機進行分群
        List<Integer> vmClusters = VMClustering.cluster(vms);
        System.out.println("\n虛擬機分群結果：");
        for (int i = 0; i < vmClusters.size(); i++) {
            System.out.println("VM ID: " + vms.get(i).getId() + ", 群組: " + vmClusters.get(i));
        }

        // Step 3: 任務分配方法
        RTACOPlacement placement = new RTACOPlacement(vms); // 使用新的構造函數

        // 將任務列表傳遞給 RTACOPlacement 類
        placement.setTasks(tasks);

        // Baseline 方法
        List<Double> taskCompletionTimeBaseline = new ArrayList<>();
        List<Double> energyConsumptionBaseline = new ArrayList<>();
        double aruBaseline = 0.0, lbBaseline = 0.0;
        List<RTACOFogDevice> remainingVMsBaseline = new ArrayList<>(vms); // 或選擇 selectedVMs 根據需求
        List<Integer> untrustedVmsBaseline = new ArrayList<>(); // 如果有不可信任的 VM，請添加其 ID

        try {
            System.out.println("\n開始執行 Baseline 方法...");
            List<Pair<Integer, Integer>> allocatedTasksBaseline = placement.assignTasksWithBaseline(
                    tasks, vms, taskCompletionTimeBaseline, remainingVMsBaseline,
                    energyConsumptionBaseline, aruBaseline, lbBaseline, untrustedVmsBaseline);

            System.out.println("Baseline 方法完成，分配了 " + allocatedTasksBaseline.size() + " 個任務。");
            System.out.println("\nBaseline 方法結果：");
            displayResults("Baseline 方法", placement, allocatedTasksBaseline, taskCompletionTimeBaseline, energyConsumptionBaseline);
        } catch (Exception e) {
            System.err.println("Baseline 方法執行失敗：");
            e.printStackTrace();
        }

        // FA (Firefly Algorithm) 方法
        List<Double> taskCompletionTimeFA = new ArrayList<>();
        List<Double> energyConsumptionFA = new ArrayList<>();
        double aruFA = 0.0, lbFA = 0.0;
        List<RTACOFogDevice> remainingVMsFA = new ArrayList<>(selectedVMs);

        try {
            System.out.println("\n開始執行 FA (Firefly Algorithm) 方法...");
            List<Pair<Integer, Integer>> allocatedTasksFA = placement.allocateTaskFA(
                    tasks, selectedVMs, taskCompletionTimeFA, remainingVMsFA,
                    energyConsumptionFA, aruFA, lbFA);

            System.out.println("FA (Firefly Algorithm) 方法完成，分配了 " + allocatedTasksFA.size() + " 個任務。");
            System.out.println("\nFA (Firefly Algorithm) 結果：");
            displayResults("FA (Firefly Algorithm)", placement, allocatedTasksFA, taskCompletionTimeFA, energyConsumptionFA);
        } catch (Exception e) {
            System.err.println("FA (Firefly Algorithm) 方法執行失敗：");
            e.printStackTrace();
        }

        // APSO 方法
        List<Double> taskCompletionTimeAPSO = new ArrayList<>();
        List<Double> energyConsumptionAPSO = new ArrayList<>();
        double aruAPSO = 0.0, lbAPSO = 0.0;
        List<RTACOFogDevice> remainingVMsAPSO = new ArrayList<>(selectedVMs);

        try {
            System.out.println("\n開始執行 APSO 方法...");
            List<Pair<Integer, Integer>> allocatedTasksAPSO = placement.allocateTaskAPSO(
                    tasks, selectedVMs, taskCompletionTimeAPSO, remainingVMsAPSO,
                    energyConsumptionAPSO, aruAPSO, lbAPSO, new ArrayList<>());

            System.out.println("APSO 方法完成，分配了 " + allocatedTasksAPSO.size() + " 個任務。");
            System.out.println("\nAPSO 方法結果：");
            displayResults("APSO 方法", placement, allocatedTasksAPSO, taskCompletionTimeAPSO, energyConsumptionAPSO);
        } catch (Exception e) {
            System.err.println("APSO 方法執行失敗：");
            e.printStackTrace();
        }

        // MinFun 方法
        List<Double> taskCompletionTimeMinFun = new ArrayList<>();
        List<Double> energyConsumptionMinFun = new ArrayList<>();
        double aruMinFun = 0.0, lbMinFun = 0.0;
        List<RTACOFogDevice> remainingVMsMinFun = new ArrayList<>(vms);

        try {
            System.out.println("\n開始執行 MinFun 方法...");
            List<Pair<Integer, Integer>> allocatedTasksMinFun = placement.assignTasksToVmsMinFun(
                    tasks, vms, taskCompletionTimeMinFun, remainingVMsMinFun,
                    energyConsumptionMinFun, vmClusters, aruMinFun, lbMinFun, new ArrayList<>());

            System.out.println("MinFun 方法完成，分配了 " + allocatedTasksMinFun.size() + " 個任務。");
            System.out.println("\nMinFun 方法結果：");
            displayResults("MinFun 方法", placement, allocatedTasksMinFun, taskCompletionTimeMinFun, energyConsumptionMinFun);
        } catch (Exception e) {
            System.err.println("MinFun 方法執行失敗：");
            e.printStackTrace();
        }

        // Clustering 方法
        List<Double> taskCompletionTimeCluster = new ArrayList<>();
        List<Double> energyConsumptionCluster = new ArrayList<>();
        double aruCluster = 0.0, lbCluster = 0.0;
        List<RTACOFogDevice> remainingVMsCluster = new ArrayList<>(vms);

        try {
            System.out.println("\n開始執行 Clustering 方法...");
            List<Pair<Integer, Integer>> allocatedTasksCluster = placement.assignTasksWithClustering(
                    tasks, vms, taskCompletionTimeCluster, remainingVMsCluster,
                    energyConsumptionCluster, aruCluster, lbCluster, new ArrayList<>());

            System.out.println("Clustering 方法完成，分配了 " + allocatedTasksCluster.size() + " 個任務。");
            System.out.println("\nClustering 方法結果：");
            displayResults("Clustering 方法", placement, allocatedTasksCluster, taskCompletionTimeCluster, energyConsumptionCluster);
        } catch (Exception e) {
            System.err.println("Clustering 方法執行失敗：");
            e.printStackTrace();
        }

        // 計算並顯示 Baseline 方法的詳細指標
//        try {
//            // 確保使用正確的 taskCompletionTimeBaseline 列表
//            double makespanBaseline = placement.calculateMakespan(taskCompletionTimeBaseline);
//            double loadBalanceBaseline = FormulaUtils.loadBalance(vms);
//            double resourceUtilizationBaseline = FormulaUtils.resourceUtilization(vms);
//
//            System.out.println("\nBaseline 方法詳細指標：");
//            System.out.println("Makespan: " + makespanBaseline);
//            System.out.println("負載平衡: " + loadBalanceBaseline);
//            System.out.println("資源利用率: " + resourceUtilizationBaseline);
//        } catch (Exception e) {
//            System.err.println("計算 Baseline 方法詳細指標失敗：");
//            e.printStackTrace();
//        }

        // 可以在此處啟動模擬，如果需要
        // CloudSim.startSimulation();
        // CloudSim.stopSimulation();
    }

    // 用於顯示結果的輔助方法（修正後，包含更多指標）
    private static void displayResults(
            String methodName,
            RTACOPlacement placement,
            List<Pair<Integer, Integer>> allocatedTasks,
            List<Double> taskCompletionTime,
            List<Double> energyConsumption) {
        System.out.println("----- " + methodName + " -----");
        if (allocatedTasks == null || allocatedTasks.isEmpty()) {
            System.out.println("沒有分配到任何任務。");
            return;
        }

        System.out.println("任務分配結果（前 10 個任務）：");
        for (int i = 0; i < Math.min(allocatedTasks.size(), 10); i++) {
            Pair<Integer, Integer> task = allocatedTasks.get(i);
            System.out.println("任務 ID: " + task.getKey() + " -> VM ID: " + task.getValue());
        }
        if (allocatedTasks.size() > 10) {
            System.out.println("... 共 " + allocatedTasks.size() + " 個任務");
        }

        double averageCompletionTime = taskCompletionTime.stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);
        double averageEnergyConsumption = energyConsumption.stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);

        double makespan = placement.calculateMakespan(taskCompletionTime);
        double loadBalance = FormulaUtils.loadBalance(placement.getVms());
        double resourceUtilization = FormulaUtils.resourceUtilization(placement.getVms());

        System.out.println("平均完成時間: " + averageCompletionTime);
        System.out.println("平均能量消耗: " + averageEnergyConsumption);
        System.out.println("Makespan: " + makespan);
        System.out.println("負載平衡: " + loadBalance);
        System.out.println("資源利用率: " + resourceUtilization);
    }
}
