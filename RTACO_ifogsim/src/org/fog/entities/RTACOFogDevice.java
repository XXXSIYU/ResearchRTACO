package org.fog.entities;

import org.cloudbus.cloudsim.power.models.PowerModel;
import java.util.Random;

public class RTACOFogDevice extends FogDevice {
    // 新增的屬性
    private double transmissionRate;
    private double availableCpu;
    private double totalCpu;
    private double availableMemory;
    private double totalMemory;
    private double processingSpeed;
    private double trustValue;
    private Random random;
    private double x;
    private double y;

    public RTACOFogDevice(
            String name, long mips, int ram,
            double uplinkBandwidth, double downlinkBandwidth, double uplinkLatency,
            double ratePerMips, PowerModel powerModel, double transmissionRate) throws Exception {

        // 正確傳遞所有必要的參數，包括 uplinkLatency
        super(name, mips, ram, uplinkBandwidth, downlinkBandwidth, uplinkLatency, ratePerMips, powerModel);

        // 初始化 RTACOFogDevice 特有的屬性
        this.availableCpu = mips; // 初始化設備的可用 CPU 為設備的 MIPS 值
        this.totalCpu = mips;  // 設定總 CPU
        this.availableMemory = ram; // 初始化設備的可用 RAM
        this.totalMemory = ram;  // 設定總記憶體
        this.transmissionRate = transmissionRate;  // 設定傳輸速率
        this.processingSpeed = mips;  // 假設 MIPS 是處理速度

        // 先初始化 random 再調用 initialTrustValue
        this.random = new Random(); // 初始化隨機數生成器
        this.x = random.nextDouble(); // 初始化 X 軸坐標
        this.y = random.nextDouble(); // 初始化 Y 軸坐標
        System.out.println("Initializing RTACOFogDevice: " + name + ", X: " + x + ", Y: " + y);
        

        this.trustValue = initialTrustValue(); // 設置初始信任值
    }

    // 假設有一個方法來設置初始信任值
    public double initialTrustValue() {
    	double trust = 0.8 + (0.2 * random.nextDouble()); // 生成 0.8 到 1.0 之間的隨機信任值
        System.out.println("Generated initial trust value: " + trust);
        return trust; 
    }

    // Getter 和 Setter 方法
    public double getTransmissionRate() {
        return transmissionRate;
    }

    public void setTransmissionRate(double transmissionRate) {
        this.transmissionRate = transmissionRate;
    }

    public double getAvailableCpu() {
        return availableCpu;
    }

    public void setAvailableCpu(double availableCpu) {
        this.availableCpu = availableCpu;
    }

    public double getTotalCpu() {
        return totalCpu;
    }

    public void setTotalCpu(double totalCpu) {
        this.totalCpu = totalCpu;
    }

    public double getAvailableMemory() {
        return availableMemory;
    }

    public void setAvailableMemory(double availableMemory) {
        this.availableMemory = availableMemory;
    }

    public double getTotalMemory() {
        return totalMemory;
    }

    public void setTotalMemory(double totalMemory) {
        this.totalMemory = totalMemory;
    }

    public double getProcessingSpeed() {
        return processingSpeed;
    }

    public void setProcessingSpeed(double processingSpeed) {
        this.processingSpeed = processingSpeed;
    }

    public double getTrustValue() {
        return trustValue;
    }

    public void setTrustValue(double trustValue) {
        this.trustValue = trustValue;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) { // 新增方法
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) { // 新增方法
        this.y = y;
    }


    // ...（其他方法和邏輯）
}
