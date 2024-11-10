package org.fog.test.perfeval;

import java.util.List;
import java.util.ArrayList;
import java.util.Random;
import java.util.Calendar;

import org.fog.entities.RTACOFogDevice;
import org.fog.entities.Task;
import org.fog.placement.RTACOPlacement;
import org.fog.placement.PlacementStrategy;
import org.fog.utils.VMGenerator;
import org.fog.utils.VMSelection;
import org.fog.utils.TaskGenerator;
import org.fog.utils.VMClustering;
import org.fog.utils.FormulaUtils;
import org.fog.utils.Pair;
import org.cloudbus.cloudsim.core.CloudSim;

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
        int numTasks = 100; // 任務數量

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

        List<Pair<Integer, Integer>> allocatedTasksBaseline = placement.assignTasksWithBaseline(
                tasks, vms, taskCompletionTimeBaseline, remainingVMsBaseline,
                energyConsumptionBaseline, aruBaseline, lbBaseline, untrustedVmsBaseline);

        System.out.println("\nBaseline 方法結果：");
        displayResults(allocatedTasksBaseline, taskCompletionTimeBaseline, energyConsumptionBaseline);

        // FA (Firefly Algorithm) 方法
        List<Double> taskCompletionTimeFA = new ArrayList<>();
        List<Double> energyConsumptionFA = new ArrayList<>();
        double aruFA = 0.0, lbFA = 0.0;
        List<RTACOFogDevice> remainingVMsFA = new ArrayList<>(selectedVMs);
        List<Pair<Integer, Integer>> allocatedTasksFA = placement.allocateTaskFA(
                tasks, selectedVMs, taskCompletionTimeFA, remainingVMsFA,
                energyConsumptionFA, aruFA, lbFA);

        System.out.println("\nFA (Firefly Algorithm) 結果：");
        displayResults(allocatedTasksFA, taskCompletionTimeFA, energyConsumptionFA);

        // APSO 方法
        List<Double> taskCompletionTimeAPSO = new ArrayList<>();
        List<Double> energyConsumptionAPSO = new ArrayList<>();
        double aruAPSO = 0.0, lbAPSO = 0.0;
        List<RTACOFogDevice> remainingVMsAPSO = new ArrayList<>(selectedVMs);
        List<Pair<Integer, Integer>> allocatedTasksAPSO = placement.allocateTaskAPSO(
                tasks, selectedVMs, taskCompletionTimeAPSO, remainingVMsAPSO,
                energyConsumptionAPSO, aruAPSO, lbAPSO, new ArrayList<>());

        System.out.println("\nAPSO 結果：");
        displayResults(allocatedTasksAPSO, taskCompletionTimeAPSO, energyConsumptionAPSO);

        // MinFun 方法
        List<Double> taskCompletionTimeMinFun = new ArrayList<>();
        List<Double> energyConsumptionMinFun = new ArrayList<>();
        double aruMinFun = 0.0, lbMinFun = 0.0;
        List<RTACOFogDevice> remainingVMsMinFun = new ArrayList<>(vms);
        List<Pair<Integer, Integer>> allocatedTasksMinFun = placement.assignTasksToVmsMinFun(
                tasks, vms, taskCompletionTimeMinFun, remainingVMsMinFun,
                energyConsumptionMinFun, vmClusters, aruMinFun, lbMinFun, new ArrayList<>());

        System.out.println("\nMinFun 方法結果：");
        displayResults(allocatedTasksMinFun, taskCompletionTimeMinFun, energyConsumptionMinFun);

        // Clustering 方法
        List<Double> taskCompletionTimeCluster = new ArrayList<>();
        List<Double> energyConsumptionCluster = new ArrayList<>();
        double aruCluster = 0.0, lbCluster = 0.0;
        List<RTACOFogDevice> remainingVMsCluster = new ArrayList<>(vms);
        List<Pair<Integer, Integer>> allocatedTasksCluster = placement.assignTasksWithClustering(
                tasks, vms, taskCompletionTimeCluster, remainingVMsCluster,
                energyConsumptionCluster, aruCluster, lbCluster, new ArrayList<>());

        System.out.println("\nClustering 方法結果：");
        displayResults(allocatedTasksCluster, taskCompletionTimeCluster, energyConsumptionCluster);

        // 可以在此處啟動模擬，如果需要
        // CloudSim.startSimulation();
        // CloudSim.stopSimulation();
    }

    // 用於顯示結果的輔助方法
    private static void displayResults(
            List<Pair<Integer, Integer>> allocatedTasks,
            List<Double> taskCompletionTime,
            List<Double> energyConsumption) {
        System.out.println("任務分配結果：");
        for (Pair<Integer, Integer> task : allocatedTasks) {
            System.out.println("任務 ID: " + task.getKey() + " -> VM ID: " + task.getValue());
        }

        double averageCompletionTime = taskCompletionTime.stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);
        double averageEnergyConsumption = energyConsumption.stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);

        System.out.println("平均完成時間: " + averageCompletionTime);
        System.out.println("平均能量消耗: " + averageEnergyConsumption);
    }
}
