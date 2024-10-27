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

public class MainRTACOSimulation{

    public static void main(String[] args) {
        // Step 1: Generate VMs and Tasks
        int numVMs = 200; // Number of VMs
        int numTasks = 100; // Number of tasks

        // Generate VMs using VMGenerator
        List<RTACOFogDevice> vms = VMGenerator.generateVMs200(numVMs);

        // Generate tasks using TaskGenerator
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
        RTACOPlacement placement = new RTACOPlacement(vms, new ArrayList<>()); // Initialize with empty module list for now

        // FA (Firefly Algorithm)
        List<Double> taskCompletionTimeFA = new ArrayList<>();
        List<Double> energyConsumptionFA = new ArrayList<>();
        double aruFA = 0, lbFA = 0;
        List<RTACOFogDevice> remainingVMsFA = new ArrayList<>(selectedVMs);
        List<Pair<Integer, Integer>> allocatedTasksFA = placement.allocateTaskFA(tasks, selectedVMs, taskCompletionTimeFA, remainingVMsFA, energyConsumptionFA, aruFA, lbFA);
        
        System.out.println("\nFA (Firefly Algorithm) Results:");
        displayResults(allocatedTasksFA, taskCompletionTimeFA, energyConsumptionFA);

        // APSO (Ant Colony Optimization)
        List<Double> taskCompletionTimeAPSO = new ArrayList<>();
        List<Double> energyConsumptionAPSO = new ArrayList<>();
        double aruAPSO = 0, lbAPSO = 0;
        List<RTACOFogDevice> remainingVMsAPSO = new ArrayList<>(selectedVMs);
        List<Pair<Integer, Integer>> allocatedTasksAPSO = placement.allocateTaskAPSO(tasks, selectedVMs, taskCompletionTimeAPSO, remainingVMsAPSO, energyConsumptionAPSO, aruAPSO, lbAPSO, new ArrayList<>());
        
        System.out.println("\nAPSO Results:");
        displayResults(allocatedTasksAPSO, taskCompletionTimeAPSO, energyConsumptionAPSO);

        // MinFun Method
        List<Double> taskCompletionTimeMinFun = new ArrayList<>();
        List<Double> energyConsumptionMinFun = new ArrayList<>();
        double aruMinFun = 0, lbMinFun = 0;
        List<RTACOFogDevice> remainingVMsMinFun = new ArrayList<>(vms);
        List<Pair<Integer, Integer>> allocatedTasksMinFun = placement.assignTasksToVmsMinFun(tasks, vms, taskCompletionTimeMinFun, remainingVMsMinFun, energyConsumptionMinFun, vmClusters, aruMinFun, lbMinFun, new ArrayList<>());
        
        System.out.println("\nMinFun Method Results:");
        displayResults(allocatedTasksMinFun, taskCompletionTimeMinFun, energyConsumptionMinFun);

        // Clustering Method
        List<Double> taskCompletionTimeCluster = new ArrayList<>();
        List<Double> energyConsumptionCluster = new ArrayList<>();
        double aruCluster = 0, lbCluster = 0;
        List<RTACOFogDevice> remainingVMsCluster = new ArrayList<>(vms);
        List<Pair<Integer, Integer>> allocatedTasksCluster = placement.assignTasksWithClustering(tasks, vms, taskCompletionTimeCluster, remainingVMsCluster, energyConsumptionCluster, aruCluster, lbCluster, new ArrayList<>());
        
        System.out.println("\nClustering Method Results:");
        displayResults(allocatedTasksCluster, taskCompletionTimeCluster, energyConsumptionCluster);
    }

    // Utility method to display results
    private static void displayResults(List<Pair<Integer, Integer>> allocatedTasks, List<Double> taskCompletionTime, List<Double> energyConsumption) {
        System.out.println("Allocated Tasks:");
        for (Pair<Integer, Integer> task : allocatedTasks) {
            System.out.println("Task ID: " + task.getFirst() + " -> VM ID: " + task.getSecond());
        }

        double averageCompletionTime = taskCompletionTime.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        double averageEnergyConsumption = energyConsumption.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);

        System.out.println("Average Completion Time: " + averageCompletionTime);
        System.out.println("Average Energy Consumption: " + averageEnergyConsumption);
    }
}
