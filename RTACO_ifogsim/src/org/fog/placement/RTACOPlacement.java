package org.fog.placement;

import org.fog.entities.FogDevice;
import org.fog.application.AppModule;
import org.fog.utils.Constant;
import org.fog.utils.ObjectiveFunction;
import org.fog.utils.FormulaUtils;
import org.fog.entities.RTACOFogDevice;
import org.fog.entities.Task;
import org.fog.utils.Pair;
import org.fog.utils.VMClustering; // 引入 VMClustering 類別

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Random;

public class RTACOPlacement extends ModulePlacement {
    private List<FogDevice> fogDevices;
    private List<AppModule> modules;
    private Map<Integer, Double> trustValues; // 存放信任值

    public RTACOPlacement(List<FogDevice> fogDevices, List<AppModule> modules) {
        this.fogDevices = fogDevices;
        this.modules = modules;
        this.trustValues = new HashMap<>();  // 初始化信任值 Map
        initializeTrustValues(); // 初始化信任值
    }

    @Override
    protected void mapModules() {
        for (AppModule module : modules) {
            FogDevice selectedDevice = selectFogDeviceForModule(module);
            if (selectedDevice != null) { 
                placeModule(module, selectedDevice);
            }
        }
    }

    // 初始化信任值
    private void initializeTrustValues() {
        for (FogDevice device : fogDevices) {
            trustValues.put(device.getId(), initialTrustValue(device)); 
        }
    }

    // 計算初始信任值
    private double initialTrustValue(FogDevice device) {
        if (device instanceof RTACOFogDevice) {
            return ((RTACOFogDevice) device).initialTrustValue();
        }
        return 1.0; 
    }

    // 選擇適合的 FogDevice
    private FogDevice selectFogDeviceForModule(AppModule module) {
        double[] pheromones = new double[fogDevices.size()];
        double totalPheromone = 0.0;

        for (int i = 0; i < fogDevices.size(); i++) {
            FogDevice device = fogDevices.get(i);
            double pheromone = calculatePheromone(device, module);
            pheromones[i] = pheromone;
            totalPheromone += pheromone;
        }

        double random = Math.random() * totalPheromone;
        double cumulative = 0.0;

        for (int i = 0; i < fogDevices.size(); i++) {
            cumulative += pheromones[i];
            if (cumulative >= random) {
                return fogDevices.get(i);
            }
        }

        return fogDevices.get(0); 
    }

    // 計算信息素值
    private double calculatePheromone(FogDevice device, AppModule module) {
        double cpuRatio = (double) device.getAvailableMips() / module.getMips();
        double memoryRatio = (double) device.getAvailableRam() / module.getRam();
        double trustValue = trustValues.get(device.getId());

        return (cpuRatio + memoryRatio) * trustValue;
    }

    // 更新信任值
    public void updateTrustValue(FogDevice device, boolean success) {
        double currentTrustValue = trustValues.get(device.getId());
        if (success) {
            trustValues.put(device.getId(), Math.min(1.0, currentTrustValue + Constant.ALPHA_TRUST));
        } else {
            trustValues.put(device.getId(), Math.max(0.0, currentTrustValue - Constant.BETA_TRUST));
        }
    }

    private void placeModule(AppModule module, FogDevice selectedDevice) {
        if (createModuleInstanceOnDevice(module, selectedDevice)) {
            System.out.println("模組 " + module.getName() + " 被放置於設備 " + selectedDevice.getName());
        } else {
            System.err.println("無法將模組 " + module.getName() + " 放置於設備 " + selectedDevice.getName());
        }
    }
    
 // 新增 normalizeFeatures 方法，用於標準化特徵
    private double[][] normalizeFeatures(double[][] X) {
        int featureCount = X[0].length;
        double[] minValues = new double[featureCount];
        double[] maxValues = new double[featureCount];

        // 計算每個特徵的最小值和最大值
        for (int j = 0; j < featureCount; j++) {
            minValues[j] = Double.MAX_VALUE;
            maxValues[j] = Double.MIN_VALUE;
            for (int i = 0; i < X.length; i++) {
                minValues[j] = Math.min(minValues[j], X[i][j]);
                maxValues[j] = Math.max(maxValues[j], X[i][j]);
            }
        }

        // 標準化 X 中的特徵
        double[][] X_normalized = new double[X.length][featureCount];
        for (int i = 0; i < X.length; i++) {
            for (int j = 0; j < featureCount; j++) {
                X_normalized[i][j] = (X[i][j] - minValues[j]) / (maxValues[j] - minValues[j]);
            }
        }
        
        return X_normalized;
    }
    
    
    

    // allocateTaskAPSO 方法
    public List<Pair<Integer, Integer>> allocateTaskAPSO(List<Task> tasks, List<RTACOFogDevice> vms, List<Double> taskCompletionTime, List<RTACOFogDevice> remainingVms, List<Double> energyConsumptionAPSO, double aru, double lb, List<Integer> untrustedVms) {
        List<Pair<Integer, Integer>> allocatedTasks = new ArrayList<>();
        Map<Integer, Integer> vmSelectedTimes = new HashMap<>();

        for (Task task : tasks) {
            List<RTACOFogDevice> availableVms = new ArrayList<>();
            for (RTACOFogDevice vm : vms) {
                if (vm.getAvailableCpu() >= task.getCpuRequirement() && vm.getAvailableMemory() >= task.getMemoryRequirement()) {
                    availableVms.add(vm);
                }
            }

            if (availableVms.isEmpty()) {
                continue;
            }

            RTACOFogDevice bestVm = null;
            double bestObjectiveValue = Double.NEGATIVE_INFINITY;

            for (RTACOFogDevice vm : availableVms) {
                if (task.getDeadline() >= FormulaUtils.completionTime(task, vm)) {
                	double objValue = ObjectiveFunction.objectiveFunction(task, vm, vmSelectedTimes);
                    if (objValue > bestObjectiveValue) {
                        bestVm = vm;
                        bestObjectiveValue = objValue;
                    }
                }
            }

            if (bestVm != null) {
                allocatedTasks.add(new Pair<>(task.getId(), bestVm.getId()));

                if (untrustedVms.contains(bestVm.getId())) {
                    taskCompletionTime.add(FormulaUtils.completionTime(task, bestVm) * 1.5);
                    energyConsumptionAPSO.add(FormulaUtils.energyConsumption(task, bestVm) * 2.5);
                } else {
                    taskCompletionTime.add(FormulaUtils.completionTime(task, bestVm));
                    energyConsumptionAPSO.add(FormulaUtils.energyConsumption(task, bestVm));
                }

                bestVm.setAvailableCpu(bestVm.getAvailableCpu() - task.getCpuRequirement());
                bestVm.setAvailableMemory(bestVm.getAvailableMemory() - task.getMemoryRequirement());
                vmSelectedTimes.put(bestVm.getId(), vmSelectedTimes.getOrDefault(bestVm.getId(), 0) + 1);
            }
        }

        lb = FormulaUtils.loadBalance(vms);
        aru = FormulaUtils.resourceUtilization(vms);
        return allocatedTasks;
    }
    
    // assignTasksToVmsBaseline 方法
    public List<Pair<Integer, Integer>> assignTasksToVmsBaseline(List<Task> tasks, List<RTACOFogDevice> vms, List<Double> taskCompletionTime, List<RTACOFogDevice> remainingVms, List<Double> energyConsumptionClustered, double aru, double lb, List<Integer> untrustedVms) {
        List<Pair<Integer, Integer>> allocatedTasks = new ArrayList<>();
        Map<Integer, Integer> vmSelectedTimes = new HashMap<>();
        Random random = new Random();

        for (Task task : tasks) {
            RTACOFogDevice bestVm = vms.get(random.nextInt(vms.size()));

            if (bestVm.getAvailableCpu() >= task.getCpuRequirement() &&
                bestVm.getAvailableMemory() >= task.getMemoryRequirement() &&
                task.getDeadline() >= FormulaUtils.completionTime(task, bestVm)) {

                allocatedTasks.add(new Pair<>(task.getId(), bestVm.getId()));

                if (untrustedVms.contains(bestVm.getId())) {
                    taskCompletionTime.add(FormulaUtils.completionTime(task, bestVm) * 1.5);
                    energyConsumptionClustered.add(FormulaUtils.energyConsumption(task, bestVm) * 2.5);
                } else {
                    taskCompletionTime.add(FormulaUtils.completionTime(task, bestVm));
                    energyConsumptionClustered.add(FormulaUtils.energyConsumption(task, bestVm));
                }

                bestVm.setAvailableCpu(bestVm.getAvailableCpu() - task.getCpuRequirement());
                bestVm.setAvailableMemory(bestVm.getAvailableMemory() - task.getMemoryRequirement());
                vmSelectedTimes.put(bestVm.getId(), vmSelectedTimes.getOrDefault(bestVm.getId(), 0) + 1);
            }
        }

        lb = FormulaUtils.loadBalance(vms);
        aru = FormulaUtils.resourceUtilization(vms);

        return allocatedTasks;
    }
    
    // 新增的 Firefly Algorithm 分配方法
    public List<Pair<Integer, Integer>> allocateTaskFA(List<Task> tasks, List<RTACOFogDevice> vms, List<Double> taskCompletionTime, List<RTACOFogDevice> remainingVms, List<Double> energyConsumptionFA, double aru, double lb) {
        List<Pair<Integer, Integer>> allocatedTasks = new ArrayList<>();
        Map<Integer, Integer> vmSelectedTimes = new HashMap<>();
        double[] lightIntensities = new double[vms.size()];

        Random random = new Random();
        double alpha = random.nextDouble();
        double epsilon = random.nextDouble() * 2 - 1;
        double gamma = random.nextDouble() * 9.99 + 0.01;

        // 初始化光亮度
        for (int i = 0; i < vms.size(); i++) {
            lightIntensities[i] = 0;
        }

        for (Task task : tasks) {
            // 計算每個 VM 的光亮度
            for (int i = 0; i < vms.size(); i++) {
                RTACOFogDevice vm = vms.get(i);
                if (vm.getAvailableCpu() >= task.getCpuRequirement() && vm.getAvailableMemory() >= task.getMemoryRequirement()) {
                    lightIntensities[i] = -ObjectiveFunction.objectiveFunction(task, vm, vmSelectedTimes) + vm.getTrustValue();
                } else {
                    lightIntensities[i] = Double.NEGATIVE_INFINITY;
                }
            }

            // 螢火蟲互動，調整位置
            // 修正螢火蟲互動部分
            for (int i = 0; i < vms.size(); i++) {
                for (int j = i + 1; j < vms.size(); j++) {
                    if (lightIntensities[j] > lightIntensities[i]) {
                        double distance = Math.sqrt(Math.pow(vms.get(i).getX() - vms.get(j).getX(), 2) + Math.pow(vms.get(i).getY() - vms.get(j).getY(), 2));
                        double beta = lightIntensities[j] * Math.exp(-gamma * Math.pow(distance, 2));

                        vms.get(i).setX(vms.get(i).getX() + beta * (vms.get(j).getX() - vms.get(i).getX()) + alpha * epsilon);
                        vms.get(i).setY(vms.get(i).getY() + beta * (vms.get(j).getY() - vms.get(i).getY()) + alpha * epsilon);

                        // 交換螢火蟲位置
                        RTACOFogDevice tempVm = vms.get(i);
                        vms.set(i, vms.get(j));
                        vms.set(j, tempVm);

                        double tempIntensity = lightIntensities[i];
                        lightIntensities[i] = lightIntensities[j];
                        lightIntensities[j] = tempIntensity;
                    }
                }
            }

            // 選擇最佳 VM 來執行任務
            RTACOFogDevice bestVm = null;
            double bestObjValue = Double.POSITIVE_INFINITY;

            for (RTACOFogDevice vm : vms) {
                if (vm.getAvailableCpu() >= task.getCpuRequirement() && vm.getAvailableMemory() >= task.getMemoryRequirement() && task.getDeadline() >= FormulaUtils.completionTime(task, vm)) {
                    double objValue = ObjectiveFunction.objectiveFunction(task, vm, vmSelectedTimes);
                    if (objValue < bestObjValue) {
                        bestVm = vm;
                        bestObjValue = objValue;
                    }
                }
            }

            if (bestVm != null) {
                allocatedTasks.add(new Pair<>(task.getId(), bestVm.getId()));
                taskCompletionTime.add(FormulaUtils.completionTime(task, bestVm));
                energyConsumptionFA.add(FormulaUtils.energyConsumption(task, bestVm));

                // 更新 VM 資源
                bestVm.setAvailableCpu(bestVm.getAvailableCpu() - task.getCpuRequirement());
                bestVm.setAvailableMemory(bestVm.getAvailableMemory() - task.getMemoryRequirement());
                vmSelectedTimes.put(bestVm.getId(), vmSelectedTimes.getOrDefault(bestVm.getId(), 0) + 1);
            }
        }

        // 更新負載平衡和資源利用率
        lb = FormulaUtils.loadBalance(vms);
        aru = FormulaUtils.resourceUtilization(vms);
        return allocatedTasks;
    }
    
 // 新增的 MinFun 分配方法
    public List<Pair<Integer, Integer>> assignTasksToVmsMinFun(List<Task> tasks, List<RTACOFogDevice> vms, List<Double> taskCompletionTime, List<RTACOFogDevice> remainingVms, List<Double> energyConsumptionMinFun, List<Integer> labels, double aru, double lb, List<Integer> untrustedVms) {
        List<Pair<Integer, Integer>> allocatedTasks = new ArrayList<>();
        Map<Integer, Integer> vmSelectedTimes = new HashMap<>();
        Map<Integer, List<RTACOFogDevice>> vmGroups = new HashMap<>();

        // 根據標籤初始化 VM 群組
        for (int i = 0; i < labels.size(); i++) {
            int label = labels.get(i);
            if (label != -1) {
                vmGroups.computeIfAbsent(label, k -> new ArrayList<>()).add(vms.get(i));
            }
        }

        // 計算完成時間與能量消耗的最小值和最大值
        double minCompletionTime = Double.POSITIVE_INFINITY;
        double maxCompletionTime = Double.NEGATIVE_INFINITY;
        double minEnergyConsumption = Double.POSITIVE_INFINITY;
        double maxEnergyConsumption = Double.NEGATIVE_INFINITY;

        for (Task task : tasks) {
            for (RTACOFogDevice vm : vms) {
                double ct = FormulaUtils.completionTime(task, vm);
                double ec = FormulaUtils.energyConsumption(task, vm);
                minCompletionTime = Math.min(minCompletionTime, ct);
                maxCompletionTime = Math.max(maxCompletionTime, ct);
                minEnergyConsumption = Math.min(minEnergyConsumption, ec);
                maxEnergyConsumption = Math.max(maxEnergyConsumption, ec);
            }
        }

        Random random = new Random();
        
     // 在包含 lambda 表達式的 for 迴圈前加入以下程式碼
        final double minCT = minCompletionTime;
        final double maxCT = maxCompletionTime;
        final double minEC = minEnergyConsumption;
        final double maxEC = maxEnergyConsumption;
        

        for (Task task : tasks) {
            // 隨機選擇一個 VM 群組
            int label = new ArrayList<>(vmGroups.keySet()).get(random.nextInt(vmGroups.size()));
            List<RTACOFogDevice> suitableVms = new ArrayList<>();

            for (RTACOFogDevice vm : vmGroups.get(label)) {
                if (vm.getAvailableCpu() >= task.getCpuRequirement() && vm.getAvailableMemory() >= task.getMemoryRequirement()) {
                    suitableVms.add(vm);
                }
            }

            if (suitableVms.isEmpty()) {
                continue;
            }

            // 選擇具有最低系統成本的 VM
            RTACOFogDevice bestVm = suitableVms.stream().min((vm1, vm2) -> Double.compare(
                    ObjectiveFunction.sysCost(task, vm1, minCT, maxCT, minEC, maxEC),
                    ObjectiveFunction.sysCost(task, vm2, minCT, maxCT, minEC, maxEC)
            )).orElse(null);

            if (bestVm != null) {
                allocatedTasks.add(new Pair<>(task.getId(), bestVm.getId()));

                if (untrustedVms.contains(bestVm.getId())) {
                    taskCompletionTime.add(FormulaUtils.completionTime(task, bestVm) * 1.5);
                    energyConsumptionMinFun.add(FormulaUtils.energyConsumption(task, bestVm) * 2.5);
                } else {
                    taskCompletionTime.add(FormulaUtils.completionTime(task, bestVm));
                    energyConsumptionMinFun.add(FormulaUtils.energyConsumption(task, bestVm));
                }

                bestVm.setAvailableCpu(bestVm.getAvailableCpu() - task.getCpuRequirement());
                bestVm.setAvailableMemory(bestVm.getAvailableMemory() - task.getMemoryRequirement());
                vmSelectedTimes.put(bestVm.getId(), vmSelectedTimes.getOrDefault(bestVm.getId(), 0) + 1);
            }
        }

        lb = FormulaUtils.loadBalance(vms);
        aru = FormulaUtils.resourceUtilization(vms);

        return allocatedTasks;
    }
    
 // 建立一個不可變的範圍類別來保存最小和最大值
    class Range {
        final double minCompletionTime;
        final double maxCompletionTime;
        final double minEnergyConsumption;
        final double maxEnergyConsumption;

        Range(double minCompletionTime, double maxCompletionTime, double minEnergyConsumption, double maxEnergyConsumption) {
            this.minCompletionTime = minCompletionTime;
            this.maxCompletionTime = maxCompletionTime;
            this.minEnergyConsumption = minEnergyConsumption;
            this.maxEnergyConsumption = maxEnergyConsumption;
        }
    }
    
 // 新增一個方法來進行 VM 分群和任務分配
 // 在 RTACOPlacement.java 中的 assignTasksWithClustering 方法內部
    public List<Pair<Integer, Integer>> assignTasksWithClustering(
            List<Task> tasks, 
            List<RTACOFogDevice> vms, 
            List<Double> taskCompletionTime, 
            List<RTACOFogDevice> remainingVms, 
            List<Double> energyConsumptionClustered, 
            double aru, 
            double lb, 
            List<Integer> untrustedVms
    ) {
        List<Pair<Integer, Integer>> allocatedTasks = new ArrayList<>();
        Map<Integer, Integer> vmSelectedTimes = new HashMap<>();
        Map<Integer, List<RTACOFogDevice>> vmGroups = new HashMap<>();

        // 使用 VMClustering 來對 VM 進行分群
        List<Integer> labels = VMClustering.cluster(vms);

        // 根據標籤初始化 VM 群組
        for (int i = 0; i < labels.size(); i++) {
            int label = labels.get(i);
            if (label != -1) {
                vmGroups.computeIfAbsent(label, k -> new ArrayList<>()).add(vms.get(i));
            }
        }

        // 計算完成時間與能量消耗的最小值和最大值
        double minCompletionTime = Double.POSITIVE_INFINITY;
        double maxCompletionTime = Double.NEGATIVE_INFINITY;
        double minEnergyConsumption = Double.POSITIVE_INFINITY;
        double maxEnergyConsumption = Double.NEGATIVE_INFINITY;

        for (Task task : tasks) {
            for (RTACOFogDevice vm : vms) {
                double ct = FormulaUtils.completionTime(task, vm);
                double ec = FormulaUtils.energyConsumption(task, vm);
                minCompletionTime = Math.min(minCompletionTime, ct);
                maxCompletionTime = Math.max(maxCompletionTime, ct);
                minEnergyConsumption = Math.min(minEnergyConsumption, ec);
                maxEnergyConsumption = Math.max(maxEnergyConsumption, ec);
            }
        }

        // 將這些範圍值封裝在 Range 物件內
        final Range range = new Range(minCompletionTime, maxCompletionTime, minEnergyConsumption, maxEnergyConsumption);

        Random random = new Random();

        for (Task task : tasks) {
            // 隨機選擇一個 VM 群組
            int label = new ArrayList<>(vmGroups.keySet()).get(random.nextInt(vmGroups.size()));
            List<RTACOFogDevice> suitableVms = new ArrayList<>();

            for (RTACOFogDevice vm : vmGroups.get(label)) {
                if (vm.getAvailableCpu() >= task.getCpuRequirement() && vm.getAvailableMemory() >= task.getMemoryRequirement()) {
                    suitableVms.add(vm);
                }
            }

            if (suitableVms.isEmpty()) {
                continue;
            }

            // 選擇具有最低系統成本的 VM
            RTACOFogDevice bestVm = suitableVms.stream().min((vm1, vm2) -> Double.compare(
                    ObjectiveFunction.sysCost(task, vm1, range.minCompletionTime, range.maxCompletionTime, range.minEnergyConsumption, range.maxEnergyConsumption),
                    ObjectiveFunction.sysCost(task, vm2, range.minCompletionTime, range.maxCompletionTime, range.minEnergyConsumption, range.maxEnergyConsumption)
            )).orElse(null);

            if (bestVm != null) {
                allocatedTasks.add(new Pair<>(task.getId(), bestVm.getId()));

                if (untrustedVms.contains(bestVm.getId())) {
                    taskCompletionTime.add(FormulaUtils.completionTime(task, bestVm) * 1.5);
                    energyConsumptionClustered.add(FormulaUtils.energyConsumption(task, bestVm) * 2.5);
                } else {
                    taskCompletionTime.add(FormulaUtils.completionTime(task, bestVm));
                    energyConsumptionClustered.add(FormulaUtils.energyConsumption(task, bestVm));
                }

                // 更新 VM 的可用資源
                bestVm.setAvailableCpu(bestVm.getAvailableCpu() - task.getCpuRequirement());
                bestVm.setAvailableMemory(bestVm.getAvailableMemory() - task.getMemoryRequirement());
                vmSelectedTimes.put(bestVm.getId(), vmSelectedTimes.getOrDefault(bestVm.getId(), 0) + 1);
            }
        }

        // 更新負載平衡和資源利用率
        lb = FormulaUtils.loadBalance(vms);
        aru = FormulaUtils.resourceUtilization(vms);

        return allocatedTasks;
    }
    
}
