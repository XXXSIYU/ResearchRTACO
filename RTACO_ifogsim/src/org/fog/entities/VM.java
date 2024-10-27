package org.fog.entities;

public class VM {
    private int id;
    private double trustValue;
    private int totalCpu;
    private int availableCpu;
    private int totalMemory;
    private int availableMemory;
    private int transmissionRate;
    private int processingSpeed;
    private double x; // VM的座標
    private double y;

    // 構造函數
    public VM(int id, double trustValue, int totalCpu, int availableCpu, int totalMemory, int availableMemory, int transmissionRate, int processingSpeed, double x, double y) {
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

    public int getTotalCpu() {
        return totalCpu;
    }

    public int getAvailableCpu() {
        return availableCpu;
    }

    public int getTotalMemory() {
        return totalMemory;
    }

    public int getAvailableMemory() {
        return availableMemory;
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

    @Override
    public String toString() {
        return "VM " + id + " - CPU: " + availableCpu + ", Memory: " + availableMemory;
    }
}
