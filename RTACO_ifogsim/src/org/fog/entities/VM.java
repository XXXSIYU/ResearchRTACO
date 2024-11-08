package org.fog.entities;

import java.util.Objects;

public class VM {
    private int id;
    private double trustValue;
    private double totalCpu;       // 總 CPU 資源（單位：MIPS）
    private double availableCpu;   // 可用的 CPU 資源（單位：MIPS）
    private double totalMemory;    // 總記憶體資源（單位：MB）
    private double availableMemory;// 可用的記憶體資源（單位：MB）
    private int transmissionRate;  // 傳輸速率（單位：Mbps）
    private int processingSpeed;   // 處理速度（單位：MIPS）
    private double x; // VM 的座標（單位：...）
    private double y; // VM 的座標（單位：...）

    // 建構子
    public VM(int id, double trustValue, double totalCpu, double availableCpu, double totalMemory, double availableMemory,
              int transmissionRate, int processingSpeed, double x, double y) {
        if (processingSpeed <= 0 || transmissionRate <= 0) {
            throw new IllegalArgumentException("處理速度和傳輸速率必須大於零");
        }
        this.id = id;
        this.trustValue = trustValue;
        this.totalCpu = totalCpu;
        this.availableCpu = availableCpu;
        this.totalMemory = totalMemory;
        this.availableMemory = availableMemory;
        this.transmissionRate = transmissionRate;
        this.processingSpeed = processingSpeed;
        this.x = x;
        this.y = y;
    }

    // Getter 和 Setter 方法
    public int getId() {
        return id;
    }

    public double getTrustValue() {
        return trustValue;
    }

    public void setTrustValue(double trustValue) {
        this.trustValue = trustValue;
    }

    public double getTotalCpu() {
        return totalCpu;
    }

    public double getAvailableCpu() {
        return availableCpu;
    }

    public void setAvailableCpu(double availableCpu) {
        this.availableCpu = availableCpu;
    }

    public double getTotalMemory() {
        return totalMemory;
    }

    public double getAvailableMemory() {
        return availableMemory;
    }

    public void setAvailableMemory(double availableMemory) {
        this.availableMemory = availableMemory;
    }

    public int getTransmissionRate() {
        return transmissionRate;
    }

    public int getProcessingSpeed() {
        return processingSpeed;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    // 更新信任值
    public void updateTrustValue(double delta) {
        this.trustValue = Math.max(0.0, Math.min(1.0, this.trustValue + delta));
    }

    // 檢查是否有足夠的資源
    public boolean hasSufficientResources(double cpuRequirement, double memoryRequirement) {
        return this.availableCpu >= cpuRequirement && this.availableMemory >= memoryRequirement;
    }

    // 分配資源
    public void allocateResources(double cpuRequirement, double memoryRequirement) {
        if (hasSufficientResources(cpuRequirement, memoryRequirement)) {
            this.availableCpu -= cpuRequirement;
            this.availableMemory -= memoryRequirement;
        } else {
            throw new IllegalArgumentException("資源不足，無法分配");
        }
    }

    // 釋放資源
    public void releaseResources(double cpuRequirement, double memoryRequirement) {
        this.availableCpu = Math.min(this.totalCpu, this.availableCpu + cpuRequirement);
        this.availableMemory = Math.min(this.totalMemory, this.availableMemory + memoryRequirement);
    }

    @Override
    public String toString() {
        return "VM{" +
                "id=" + id +
                ", trustValue=" + trustValue +
                ", availableCpu=" + availableCpu + "/" + totalCpu +
                ", availableMemory=" + availableMemory + "/" + totalMemory +
                ", processingSpeed=" + processingSpeed +
                ", transmissionRate=" + transmissionRate +
                ", x=" + x +
                ", y=" + y +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        VM vm = (VM) o;

        return id == vm.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
