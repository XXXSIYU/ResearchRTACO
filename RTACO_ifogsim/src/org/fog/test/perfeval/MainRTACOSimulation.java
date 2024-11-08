package org.fog.test.perfeval;

import java.util.List;
import java.util.ArrayList;
import java.util.Random;
import org.fog.entities.RTACOFogDevice;
import org.fog.entities.Task;
import org.fog.placement.RTACOPlacement;
import org.fog.utils.VMGenerator;
import org.fog.utils.VMSelection;
import org.fog.utils.TaskGenerator;
import org.fog.utils.VMClustering;
import org.fog.utils.FormulaUtils;
import org.fog.utils.Pair;

public class MainRTACOSimulation {

    public static void main(String[] args) {
        // Step 1: Generate VMs and Tasks
        int numVMs = 200; // 虛擬機數量
        int numTasks = 100; // 任務數量

        // 生成虛擬機
        List<RTACOFogDevice> vms = VMGenerator.generateVMs200(numVMs);

        // 生成任務
        List<Task> tasks = TaskGenerator.generateTasks(numTasks);

        // Step 2: VM Selection
        List<RTACOFogDevice> selectedVMs = VMSelection.selectVMs(vms);
        selectedVMs.sort((vm1, vm2) -> Integer.compare(vm1.getId(), vm2.getId()));

        System.out.println("\nSelected VMs:");
        for (RTACOFogDevice vm : selectedVMs) {
            System.out.println(vm + ", Trust: " + vm.getTrustValue());
        }

        // Clustering the VMs using VMClustering
        List<Integer> vmClusters = VMClustering.cluster(vms);
        System.out.println("\nVM Clustering:");
        for (int i = 0; i < vmClusters.size(); i++) {
            System.out.println("VM ID: " + vms.get(i).getId() + ", Cluster: " + vmClusters.get(i));
        }

        // Step 3: Task Allocation Methods
        RTACOPlacement placement = new RTACOPlacement(vms); // 使用新的建構子

        // FA (Firefly Algorithm)
        List<Double> taskCompletionTimeFA = new ArrayList<>();
        List<Double> energyConsumptionFA = new ArrayList<>();
        Double[] aruFA = new Double[]{0.0}, lbFA = new Double[]{0.0};
        List<RTACOFogDevice> remainingVMsFA = new ArrayList<>(selectedVMs);
        List<Pair<Integer, Integer>> allocatedTasksFA = placement.allocateTaskFA(
                tasks, selectedVMs, taskCompletionTimeFA, remainingVMsFA,
                energyConsumptionFA, aruFA, lbFA);

        System.out.println("\nFA (Firefly Algorithm) Results:");
        displayResults(allocatedTasksFA, taskCompletionTimeFA, energyConsumptionFA);

        // APSO (Ant Colony Optimization)
        List<Double> taskCompletionTimeAPSO = new ArrayList<>();
        List<Double> energyConsumptionAPSO = new ArrayList<>();
        Double[] aruAPSO = new Double[]{0.0}, lbAPSO = new Double[]{0.0};
        List<RTACOFogDevice> remainingVMsAPSO = new ArrayList<>(selectedVMs);
        List<Pair<Integer, Integer>> allocatedTasksAPSO = placement.allocateTaskAPSO(
                tasks, selectedVMs, taskCompletionTimeAPSO, remainingVMsAPSO,
                energyConsumptionAPSO, aruAPSO, lbAPSO, new ArrayList<>());

        System.out.println("\nAPSO Results:");
        displayResults(allocatedTasksAPSO, taskCompletionTimeAPSO, energyConsumptionAPSO);

        // MinFun Method
        List<Double> taskCompletionTimeMinFun = new ArrayList<>();
        List<Double> energyConsumptionMinFun = new ArrayList<>();
        Double[] aruMinFun = new Double[]{0.0}, lbMinFun = new Double[]{0.0};
        List<RTACOFogDevice> remainingVMsMinFun = new ArrayList<>(vms);
        List<Pair<Integer, Integer>> allocatedTasksMinFun = placement.assignTasksToVmsMinFun(
                tasks, vms, taskCompletionTimeMinFun, remainingVMsMinFun,
                energyConsumptionMinFun, vmClusters, aruMinFun, lbMinFun, new ArrayList<>());

        System.out.println("\nMinFun Method Results:");
        displayResults(allocatedTasksMinFun, taskCompletionTimeMinFun, energyConsumptionMinFun);

        // Clustering Method
        List<Double> taskCompletionTimeCluster = new ArrayList<>();
        List<Double> energyConsumptionCluster = new ArrayList<>();
        Double[] aruCluster = new Double[]{0.0}, lbCluster = new Double[]{0.0};
        List<RTACOFogDevice> remainingVMsCluster = new ArrayList<>(vms);
        List<Pair<Integer, Integer>> allocatedTasksCluster = placement.assignTasksWithClustering(
                tasks, vms, taskCompletionTimeCluster, remainingVMsCluster,
                energyConsumptionCluster, aruCluster, lbCluster, new ArrayList<>());

        System.out.println("\nClustering Method Results:");
        displayResults(allocatedTasksCluster, taskCompletionTimeCluster, energyConsumptionCluster);
    }

    // 用於顯示結果的輔助方法
    private static void displayResults(
            List<Pair<Integer, Integer>> allocatedTasks,
            List<Double> taskCompletionTime,
            List<Double> energyConsumption) {
        System.out.println("Allocated Tasks:");
        for (Pair<Integer, Integer> task : allocatedTasks) {
            System.out.println("Task ID: " + task.getKey() + " -> VM ID: " + task.getValue());
        }

        double averageCompletionTime = taskCompletionTime.stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);
        double averageEnergyConsumption = energyConsumption.stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);

        System.out.println("Average Completion Time: " + averageCompletionTime);
        System.out.println("Average Energy Consumption: " + averageEnergyConsumption);
    }
}
