package org.fog.placement;

import org.fog.entities.RTACOFogDevice;
import org.fog.application.AppModule;
import org.fog.utils.Constant;
import org.fog.utils.FormulaUtils;
import org.fog.entities.Task;
import org.fog.utils.Pair;
import org.fog.utils.VMClustering;
import org.fog.utils.VMSelection;
import org.fog.utils.ObjectiveFunction;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Random;

/**
 * RTACOPlacement 類實現了不同的任務分配策略，
 * 包括 Baseline、Clustering、APSO、FA 和 MinFun 方法。
 */
public class RTACOPlacement extends ModulePlacement {
    private List<RTACOFogDevice> fogDevices;
    private List<AppModule> modules;
    private Map<Integer, Double> trustValues; // 存放信任值
    private PlacementStrategy placementStrategy; // 分配策略
    private List<Task> tasks; // 任務列表
    

    /**
     * RTACOPlacement 建構子，設置分配策略
     * @param fogDevices Fog 設備列表
     * @param placementStrategy 任務分配策略
     */
    public RTACOPlacement(List<RTACOFogDevice> fogDevices, PlacementStrategy placementStrategy) {
        this.fogDevices = fogDevices;
        this.modules = new ArrayList<>();
        this.trustValues = new HashMap<>();  // 初始化信任值 Map
        this.placementStrategy = placementStrategy; // 設置分配策略
        initializeTrustValues(); // 初始化信任值
    }
    
    /**
     * 新增的構造函數，只接受 Fog 設備列表，不設置分配策略。
     * 允許直接調用不同的分配方法。
     *
     * @param fogDevices Fog 設備列表
     */
    public RTACOPlacement(List<RTACOFogDevice> fogDevices) {
        this.fogDevices = fogDevices;
        this.modules = new ArrayList<>();
        this.trustValues = new HashMap<>();
        this.placementStrategy = null; // 無特定分配策略
        initializeTrustValues();
    }

    /**
     * 設置任務列表的方法
     */
    public void setTasks(List<Task> tasks) {
        this.tasks = tasks;
    }

    @Override
    protected void mapModules() {
        if (tasks == null || tasks.isEmpty()) {
            System.err.println("任務列表為空，無法進行分配。");
            return;
        }

        List<Double> taskCompletionTime = new ArrayList<>();
        List<Double> energyConsumptionClustered = new ArrayList<>();
        List<RTACOFogDevice> remainingVms = new ArrayList<>(fogDevices);
        double aru = 0.0;
        double lb = 0.0;
        List<Integer> untrustedVms = getUntrustedVms(); // 取得不可信任的 VM 列表

        List<Pair<Integer, Integer>> allocatedTasks = new ArrayList<>();

        switch (placementStrategy) {
            case BASELINE:
                allocatedTasks = assignTasksWithBaseline(
                        tasks,
                        fogDevices,
                        taskCompletionTime,
                        remainingVms,
                        energyConsumptionClustered,
                        aru,
                        lb,
                        untrustedVms
                );
                break;
            case CLUSTERING:
                allocatedTasks = assignTasksWithClustering(
                        tasks,
                        fogDevices,
                        taskCompletionTime,
                        remainingVms,
                        energyConsumptionClustered,
                        aru,
                        lb,
                        untrustedVms
                );
                break;
            case APSO:
                allocatedTasks = allocateTaskAPSO(
                        tasks,
                        fogDevices,
                        taskCompletionTime,
                        remainingVms,
                        energyConsumptionClustered,
                        aru,
                        lb,
                        untrustedVms
                );
                break;
            case MINFUN:
                allocatedTasks = assignTasksToVmsMinFun(
                        tasks,
                        fogDevices,
                        taskCompletionTime,
                        remainingVms,
                        energyConsumptionClustered,
                        /* labels */ new ArrayList<>(), // 根據您的需求提供標籤
                        aru,
                        lb,
                        untrustedVms
                );
                break;
            case FA:
                allocatedTasks = allocateTaskFA(
                        tasks,
                        fogDevices,
                        taskCompletionTime,
                        remainingVms,
                        energyConsumptionClustered,
                        aru,
                        lb
                );
                break;
            default:
                System.err.println("未支持的分配策略: " + placementStrategy);
                break;
        }

        // 處理分配結果（例如，更新模組的位置等）
        for (Pair<Integer, Integer> allocation : allocatedTasks) {
            int taskId = allocation.getKey();
            int vmId = allocation.getValue();
            // 根據 taskId 和 vmId 進行相應處理
            // 例如，將模組與 VM 相關聯
            // 您需要根據具體需求實現這部分
            System.out.println("任務 " + taskId + " 被分配到 VM " + vmId);
        }

        // 更新負載均衡和資源利用率（已在各個方法中完成）

        // 打印更新後的信任值
        System.out.println("\n更新後的 VM 信任值：");
        for (RTACOFogDevice vm : fogDevices) {
            System.out.println("VM ID: " + vm.getId() + ", 信任值: " + vm.getTrustValue());
        }
    }

    /**
     * 初始化所有 VM 的信任值。
     */
    private void initializeTrustValues() {
        for (RTACOFogDevice device : fogDevices) {
            double initialTrust = device.initialTrustValue();
            trustValues.put(device.getId(), initialTrust);
            device.setTrustValue(initialTrust); // 設置 VM 對象中的信任值
            System.out.println("VM ID: " + device.getId() + ", Initial Trust Value: " + initialTrust);
        }
    }

    /**
     * 添加 updateTrustValue 方法
     */
    private void updateTrustValue(int vmId, boolean taskSuccess) {
        double currentTrust = trustValues.getOrDefault(vmId, 1.0);
        if (taskSuccess) {
            currentTrust += 0.1;
        } else {
            currentTrust -= 0.1;
        }
        // 確保信任值在 0 到 1 之間
        currentTrust = Math.max(0.0, Math.min(currentTrust, 1.0));
        trustValues.put(vmId, currentTrust);
        // 更新 VM 對象中的信任值
        for (RTACOFogDevice vm : fogDevices) {
            if (vm.getId() == vmId) {
                vm.setTrustValue(currentTrust);
                System.out.println("VM ID: " + vmId + ", Updated Trust Value: " + currentTrust);
                break;
            }
        }
    }

    /**
     * Baseline 任務分配方法
     */
    public List<Pair<Integer, Integer>> assignTasksWithBaseline(
            List<Task> tasks,
            List<RTACOFogDevice> vms,
            List<Double> taskCompletionTime,
            List<RTACOFogDevice> remainingVms,
            List<Double> energyConsumptionClustered,
            double aru,
            double lb,
            List<Integer> untrustedVms) {

        List<Pair<Integer, Integer>> allocatedTasks = new ArrayList<>();
        Map<Integer, Integer> vmSelectedTimes = new HashMap<>();
        Random random = new Random();

        // 按信任值排序，優先選擇信任值高的 VM
        vms.sort((vm1, vm2) -> Double.compare(trustValues.get(vm2.getId()), trustValues.get(vm1.getId())));

        for (Task task : tasks) {
            RTACOFogDevice bestVm = null;
            for (RTACOFogDevice vm : vms) {
                if (vm.getAvailableCpu() >= task.getCpuRequirement() &&
                    vm.getAvailableMemory() >= task.getMemoryRequirement() &&
                    task.getDeadline() >= FormulaUtils.completionTime(task, vm)) {

                    bestVm = vm;
                    break; // 找到第一個滿足條件的 VM
                }
            }

            if (bestVm != null) {
                allocatedTasks.add(new Pair<>(task.getId(), bestVm.getId()));

                // 模擬任務成功與否（這裡隨機決定，實際情況應根據真實數據）
                boolean taskSuccess = random.nextBoolean();

                // 調整完成時間和能量消耗
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

                // 更新 VM 被選中的次數
                vmSelectedTimes.put(bestVm.getId(), vmSelectedTimes.getOrDefault(bestVm.getId(), 0) + 1);

                // 更新信任值
                updateTrustValue(bestVm.getId(), taskSuccess);
            }
        }

        // 計算負載均衡和資源利用率
        lb = FormulaUtils.loadBalance(vms);
        aru = FormulaUtils.resourceUtilization(vms);

        return allocatedTasks;
    }

    /**
     * Clustering 任務分配方法
     */
    public List<Pair<Integer, Integer>> assignTasksWithClustering(
            List<Task> tasks,
            List<RTACOFogDevice> vms,
            List<Double> taskCompletionTime,
            List<RTACOFogDevice> remainingVms,
            List<Double> energyConsumptionClustered,
            double aru,
            double lb,
            List<Integer> untrustedVms) {

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
        
        // 確保有多個群組
        if (vmGroups.size() <= 1) {
            // 如果只有一個群組，則手動分配
            for (int i = 0; i < vms.size(); i++) {
                vmGroups.computeIfAbsent(i % 5, k -> new ArrayList<>()).add(vms.get(i)); // 假設分成5個群組
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
            if (vmGroups.isEmpty()) {
                continue;
            }
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

                // 模擬任務成功與否（這裡隨機決定，實際情況應根據真實數據）
                boolean taskSuccess = random.nextBoolean();

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

                // 更新 VM 被選中的次數
                vmSelectedTimes.put(bestVm.getId(), vmSelectedTimes.getOrDefault(bestVm.getId(), 0) + 1);

                // 更新信任值
                updateTrustValue(bestVm.getId(), taskSuccess);
            }
        }

        // 計算負載均衡和資源利用率
        lb = FormulaUtils.loadBalance(vms);
        aru = FormulaUtils.resourceUtilization(vms);

        return allocatedTasks;
    }

    /**
     * APSO 任務分配方法
     * 這是一個示例實現，您需要根據 APSO 的具體算法來實現。
     */
    public List<Pair<Integer, Integer>> allocateTaskAPSO(
            List<Task> tasks,
            List<RTACOFogDevice> vms,
            List<Double> taskCompletionTime,
            List<RTACOFogDevice> remainingVms,
            List<Double> energyConsumptionClustered,
            double aru,
            double lb,
            List<Integer> untrustedVms) {

        List<Pair<Integer, Integer>> allocatedTasks = new ArrayList<>();
        Map<Integer, Integer> vmSelectedTimes = new HashMap<>();
        // APSO 分配邏輯的具體實現
        // 這裡僅提供一個簡單的示例

        // 按信任值排序，優先選擇信任值高的 VM
        vms.sort((vm1, vm2) -> Double.compare(trustValues.get(vm2.getId()), trustValues.get(vm1.getId())));

        for (Task task : tasks) {
            // 遍歷所有 VM，選擇具有最低目標函數值的 VM
            RTACOFogDevice bestVm = null;
            double bestObjective = Double.MAX_VALUE;

            for (RTACOFogDevice vm : vms) {
                if (vm.getAvailableCpu() >= task.getCpuRequirement() &&
                    vm.getAvailableMemory() >= task.getMemoryRequirement() &&
                    task.getDeadline() >= FormulaUtils.completionTime(task, vm)) {

                    double objValue = ObjectiveFunction.objectiveFunction(task, vm, vmSelectedTimes);
                    // 考慮信任值
                    objValue -= vm.getTrustValue() * 10; // 示例：信任值對目標函數的影響
                    if (objValue < bestObjective) {
                        bestObjective = objValue;
                        bestVm = vm;
                    }
                }
            }

            if (bestVm != null) {
                allocatedTasks.add(new Pair<>(task.getId(), bestVm.getId()));

                // 模擬任務成功與否（這裡隨機決定，實際情況應根據真實數據）
                boolean taskSuccess = new Random().nextBoolean();

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

                // 更新 VM 被選中的次數
                vmSelectedTimes.put(bestVm.getId(), vmSelectedTimes.getOrDefault(bestVm.getId(), 0) + 1);

                // 更新信任值
                updateTrustValue(bestVm.getId(), taskSuccess);
            }
        }

        // 計算負載均衡和資源利用率
        lb = FormulaUtils.loadBalance(vms);
        aru = FormulaUtils.resourceUtilization(vms);

        return allocatedTasks;
    }

    /**
     * FA 任務分配方法
     * 這是一個示例實現，您需要根據 FA 的具體算法來實現。
     */
    public List<Pair<Integer, Integer>> allocateTaskFA(
            List<Task> tasks,
            List<RTACOFogDevice> vms,
            List<Double> taskCompletionTime,
            List<RTACOFogDevice> remainingVms,
            List<Double> energyConsumptionFA,
            double aru,
            double lb) {

        List<Pair<Integer, Integer>> allocatedTasks = new ArrayList<>();
        Map<Integer, Integer> vmSelectedTimes = new HashMap<>();
        double[] lightIntensities = new double[vms.size()];

        Random random = new Random();
        double alpha = 0.5; // 步長
        double epsilon = 0.1; // 隨機因子
        double gamma = 1.0; // 吸引力係數

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
            for (int i = 0; i < vms.size(); i++) {
                for (int j = i + 1; j < vms.size(); j++) {
                    if (lightIntensities[j] > lightIntensities[i]) {
                        double distance = Math.sqrt(Math.pow(vms.get(i).getX() - vms.get(j).getX(), 2) + Math.pow(vms.get(i).getY() - vms.get(j).getY(), 2));
                        double beta = lightIntensities[j] * Math.exp(-gamma * Math.pow(distance, 2));

                        // 計算新的位置
                        double newX = vms.get(i).getX() + beta * (vms.get(j).getX() - vms.get(i).getX()) + alpha * (random.nextDouble() * 2 - 1) * epsilon;
                        double newY = vms.get(i).getY() + beta * (vms.get(j).getY() - vms.get(i).getY()) + alpha * (random.nextDouble() * 2 - 1) * epsilon;

                        vms.get(i).setX(newX);
                        vms.get(i).setY(newY); // 修正此處

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
                if (vm.getAvailableCpu() >= task.getCpuRequirement() &&
                    vm.getAvailableMemory() >= task.getMemoryRequirement() &&
                    task.getDeadline() >= FormulaUtils.completionTime(task, vm)) {

                    double objValue = ObjectiveFunction.objectiveFunction(task, vm, vmSelectedTimes);
                    // 考慮信任值
                    objValue -= vm.getTrustValue() * 10; // 示例：信任值對目標函數的影響
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

                // 更新信任值
                boolean taskSuccess = random.nextBoolean(); // 模擬任務成功與否
                updateTrustValue(bestVm.getId(), taskSuccess);
            }
        }

        // 計算負載均衡和資源利用率
        lb = FormulaUtils.loadBalance(vms);
        aru = FormulaUtils.resourceUtilization(vms);
        return allocatedTasks;
    }
    
    

    /**
     * MINFUN 任務分配方法
     */
    public List<Pair<Integer, Integer>> assignTasksToVmsMinFun(
            List<Task> tasks,
            List<RTACOFogDevice> vms,
            List<Double> taskCompletionTime,
            List<RTACOFogDevice> remainingVms,
            List<Double> energyConsumptionClustered,
            List<Integer> labels,
            double aru,
            double lb,
            List<Integer> untrustedVms) {

        List<Pair<Integer, Integer>> allocatedTasks = new ArrayList<>();
        Map<Integer, Integer> vmSelectedTimes = new HashMap<>();

        for (Task task : tasks) {
            // 遍歷所有 VM，選擇具有最低系統成本的 VM
            RTACOFogDevice bestVm = null;
            double bestCost = Double.MAX_VALUE;

            for (RTACOFogDevice vm : vms) {
                if (vm.getAvailableCpu() >= task.getCpuRequirement() &&
                    vm.getAvailableMemory() >= task.getMemoryRequirement() &&
                    task.getDeadline() >= FormulaUtils.completionTime(task, vm)) {

                    double cost = ObjectiveFunction.sysCost(task, vm, 0, 1000, 0, 1000); // 您需要根據實際範圍調整
                    // 考慮信任值
                    cost -= trustValues.get(vm.getId()) * 10; // 示例：信任值對成本的影響
                    if (cost < bestCost) {
                        bestCost = cost;
                        bestVm = vm;
                    }
                }
            }

            if (bestVm != null) {
                allocatedTasks.add(new Pair<>(task.getId(), bestVm.getId()));
                taskCompletionTime.add(FormulaUtils.completionTime(task, bestVm));
                energyConsumptionClustered.add(FormulaUtils.energyConsumption(task, bestVm));

                // 更新 VM 資源
                bestVm.setAvailableCpu(bestVm.getAvailableCpu() - task.getCpuRequirement());
                bestVm.setAvailableMemory(bestVm.getAvailableMemory() - task.getMemoryRequirement());
                vmSelectedTimes.put(bestVm.getId(), vmSelectedTimes.getOrDefault(bestVm.getId(), 0) + 1);

                // 模擬任務成功與否
                boolean taskSuccess = new Random().nextBoolean();
                updateTrustValue(bestVm.getId(), taskSuccess);
            }
        }

        // 計算負載均衡和資源利用率
        lb = FormulaUtils.loadBalance(vms);
        aru = FormulaUtils.resourceUtilization(vms);

        return allocatedTasks;
    }

//    /**
//     * 取得任務列表的方法（示例）
//     */
//    private List<Task> getTasks() {
//        // 根據您的需求實現此方法，返回當前需要分配的任務列表
//        // 這裡暫時返回空列表
//        return new ArrayList<>();
//    }

    /**
     * 取得不可信任的 VM 列表的方法（示例）
     */
    private List<Integer> getUntrustedVms() {
        // 根據您的需求實現此方法，返回不可信任的 VM ID 列表
        // 例如，隨機選擇部分 VM 作為不可信任
        List<Integer> untrustedVms = new ArrayList<>();
        Random random = new Random();
        for (RTACOFogDevice vm : fogDevices) {
            if (random.nextDouble() < 0.1) { // 假設 10% 的 VM 是不可信任的
                untrustedVms.add(vm.getId());
            }
        }
        return untrustedVms;
    }
    
    /**
     * 計算 Makespan 的方法
     */
    public double calculateMakespan(List<Double> taskCompletionTime) {
        return taskCompletionTime.stream().mapToDouble(Double::doubleValue).max().orElse(0.0);
    }
    
    /**
     * 獲取虛擬機列表的方法
     *
     * @return 虛擬機列表
     */
    public List<RTACOFogDevice> getVms() {
        return this.fogDevices;
    }
    
    

    /**
     * Range 類用於封裝完成時間和能量消耗的最小值和最大值。
     */
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

    // 其他現有的方法...
}
