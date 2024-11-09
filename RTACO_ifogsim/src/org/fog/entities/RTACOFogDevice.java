package org.fog.entities;

import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.power.models.PowerModel;
import org.fog.utils.Constant; // 引入常量類
import java.util.Random;

/**
 * RTACOFogDevice 是 FogDevice 的擴展類，增加了信任值管理、資源分配和任務完成的計算邏輯，
 * 支持使用 RTACO (Real-Time Ant Colony Optimization) 演算法進行設備選擇。
 */
public class RTACOFogDevice extends FogDevice {
    private double availableCpu;     // 可用的 CPU 資源（以 MIPS 計算）
    private double totalCpu;         // 總 CPU 資源
    private double availableMemory;  // 可用的記憶體資源（以 RAM 單位計算）
    private double totalMemory;      // 總記憶體資源
    private double transmissionRate; // 設備的傳輸速率
    private double trustValue;       // 設備的信任值，用來決定設備是否適合執行任務
    private Random random;           // 用於生成隨機數的 Random 物件，供信任值更新等使用
    private double processingSpeed;  // 設備的處理速度 (MIPS)
    private double x;                // X 軸坐標，用於螢火蟲算法
    private double y;                // Y 軸坐標，用於螢火蟲算法
    

    /**
     * 構造函數，初始化 RTACOFogDevice 並調用父類的構造函數設置設備屬性。
     * 
     * @param name 設備名稱
     * @param mips 設備的 CPU 資源（MIPS）RT
     * @param ram 設備的記憶體資源（RAM）
     * @param uplinkBandwidth 上行帶寬
     * @param downlinkBandwidth 下行帶寬
     * @param ratePerMips 每 MIPS 的資源消耗率
     * @param powerModel 電源模型，用來計算設備的功耗
     * @param transmissionRate 傳輸速率
     */
    public RTACOFogDevice(
            String name, long mips, int ram,
            double uplinkBandwidth, double downlinkBandwidth, double uplinkLatency, double ratePerMips,
            PowerModel powerModel, double transmissionRate) throws Exception {
        super(name, mips, ram, uplinkBandwidth, downlinkBandwidth, uplinkLatency, ratePerMips, powerModel);
        this.availableCpu = mips; // 初始化設備的可用 CPU 為設備的 MIPS 值
        this.totalCpu = mips;  // 設定總 CPU
        this.availableMemory = ram; // 初始化設備的可用 RAM
        this.totalMemory = ram;  // 設定總記憶體
        this.transmissionRate = transmissionRate;  // 設定傳輸速率
        this.processingSpeed = mips;  // 假設 MIPS 是處理速度
        this.trustValue = initialTrustValue(); // 設置初始信任值
        this.random = new Random(); // 初始化隨機數生成器
        this.x = random.nextDouble(); // 初始化 X 軸坐標
        this.y = random.nextDouble(); // 初始化 Y 軸坐標
    }


    /**
     * 初始化信任值，範圍在 0.8 到 1.0 之間。
     * @return 隨機生成的信任值
     */
    public double initialTrustValue() {
        return 0.8 + (0.2 * random.nextDouble()); // 生成 0.8 到 1.0 之間的隨機信任值
    }
    
    // 獲取總 CPU 資源
    public double getTotalCpu() {
        return totalCpu;
    }
    
    
    /**
     * 獲取設備的可用 CPU 資源。
     * @return 可用的 CPU 資源（MIPS）
     */
    public double getAvailableCpu() {
        return availableCpu;
    }
    
    // 設置可用 CPU 資源
    public void setAvailableCpu(double availableCpu) {
        this.availableCpu = availableCpu;
    }
    
    // 獲取總記憶體資源
    public double getTotalMemory() {
        return totalMemory;
    }
    
    /**
     * 獲取設備的可用記憶體資源。
     * @return 可用的記憶體資源（RAM）
     */
    public double getAvailableMemory() {
        return availableMemory;
    }
    
    // 設置可用記憶體資源
    public void setAvailableMemory(double availableMemory) {
        this.availableMemory = availableMemory;
    }
    
    // 獲取設備的傳輸速率
    public double getTransmissionRate() {
        return transmissionRate;
    }
    
    // 獲取設備的處理速度
    public double getProcessingSpeed() {
        return processingSpeed;
    }
    
    
    /**
     * 獲取設備當前的信任值。
     * @return 當前的信任值
     */
    public double getTrustValue() {
        return trustValue;
    }

 // 獲取設備的 X 坐標
    public double getX() {
        return x;
    }

    // 設置設備的 X 坐標
    public void setX(double x) {
        this.x = x;
    }

    // 獲取設備的 Y 坐標
    public double getY() {
        return y;
    }

    // 設置設備的 Y 坐標
    public void setY(double y) {
        this.y = y;
    }
    
    /**
     * 更新信任值，考慮任務的資源需求和設備的可用資源情況。
     * 根據任務的 CPU 和記憶體需求計算懲罰並更新信任值。
     * 
     * @param vm 虛擬機（未來可擴展的參數）
     * @param task 任務，包含 CPU 和記憶體需求
     * @param success 任務是否成功
     */
    public void updateTrustValue(Vm vm, Task task, boolean success) {
        if (success) {
            // 根據任務的資源需求與設備可用資源計算懲罰
            double penalty = 0.1 * (task.getCpuRequirement() / availableCpu + task.getMemoryRequirement() / availableMemory);
            trustValue = Math.min(1.0, trustValue + Constant.ALPHA_TRUST - penalty); // 成功時信任值增加並扣除懲罰
        } else {
            trustValue = Math.max(0.0, trustValue - Constant.BETA_TRUST); // 失敗時信任值減少
        }
    }
    
    /**
     * 計算能量消耗。
     * @param taskCpu 任務的 CPU 需求
     * @param taskMemory 任務的記憶體需求
     * @return 總能量消耗
     */
    public double calculateEnergyConsumption(double taskCpu, double taskMemory) {
        return (taskCpu / availableCpu) * Constant.ENERGY_CONSUMPTION_PROCESSING + Constant.ENERGY_CONSUMPTION_TRANSMISSION;
    }
    

    /**
     * 計算任務是否成功完成。成功率基於設備的可用資源與任務的需求，並包含隨機因子。
     * 
     * @param task 任務，包含 CPU 和記憶體需求
     * @return 如果成功完成，返回 true；否則返回 false
     */
    public boolean taskCompletion(Task task) {
        // 計算成功率，取決於設備的可用資源和任務的資源需求
        double successProbability = Math.min(availableCpu / task.getCpuRequirement(), availableMemory / task.getMemoryRequirement());
        successProbability *= 0.8 + (0.2 * random.nextDouble());  // 增加隨機成功機率因子
        return random.nextDouble() < successProbability; // 隨機決定任務是否成功
    }
    
    
 // 如果需要，也可以添加 setTransmissionRate 方法
    public void setTransmissionRate(double transmissionRate) {
        this.transmissionRate = transmissionRate;
    }

    // 可以在這裡擴展其他功能，如資源分配或能源消耗的管理
}
