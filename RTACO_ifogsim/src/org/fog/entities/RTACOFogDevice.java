package org.fog.entities;

import org.cloudbus.cloudsim.power.models.PowerModel;

/**
 * RTACOFogDevice 類代表一個 Fog 設備，包含了設備的各種屬性和方法。
 */
public class RTACOFogDevice {
    private static int idCounter = 0; // 靜態計數器，用於分配唯一的 ID

    private int id;
    private String name;
    private double mips;
    private double availableCpu;
    private double availableMemory;
    private double energyPerMips;
    private double trustValue;
    private double x;
    private double y;
    private double transmissionRate; // 傳輸速率，單位：Mbps
    private double processingSpeed; // 處理速度，單位：MIPS
    private double taskSize; // 任務大小，單位：MB
    private double resultSize; // 結果大小，單位：MB

    private double uplinkBandwidth; // 上行帶寬，單位：Mbps
    private double downlinkBandwidth; // 下行帶寬，單位：Mbps
    private double uplinkLatency; // 上行延遲，單位：ms
    private double ratePerMips; // 每 MIPS 的資源消耗率
    private PowerModel powerModel; // 功耗模型

    /**
     * RTACOFogDevice 構造函數，用於創建一個新的 Fog 設備實例。
     *
     * @param name             設備名稱
     * @param mips             設備的 MIPS 值
     * @param ram              設備的記憶體，單位：MB
     * @param uplinkBandwidth  上行帶寬，單位：Mbps
     * @param downlinkBandwidth 下行帶寬，單位：Mbps
     * @param uplinkLatency    上行延遲，單位：ms
     * @param ratePerMips      每 MIPS 的資源消耗率
     * @param powerModel       功耗模型
     * @param transmissionRate 傳輸速率，單位：Mbps
     */
    public RTACOFogDevice(String name, double mips, int ram, double uplinkBandwidth, double downlinkBandwidth,
                          double uplinkLatency, double ratePerMips, PowerModel powerModel, double transmissionRate) {
        this.id = idCounter++;
        this.name = name;
        this.mips = mips;
        this.availableCpu = mips;
        this.availableMemory = ram;
        this.energyPerMips = ratePerMips;
        this.trustValue = initialTrustValue();
        this.x = 0.0;
        this.y = 0.0;
        this.transmissionRate = transmissionRate;
        this.processingSpeed = mips; // 假設處理速度等於 MIPS
        this.taskSize = 0.0;
        this.resultSize = 0.0;
        this.uplinkBandwidth = uplinkBandwidth;
        this.downlinkBandwidth = downlinkBandwidth;
        this.uplinkLatency = uplinkLatency;
        this.ratePerMips = ratePerMips;
        this.powerModel = powerModel;
    }

    /**
     * 獲取設備的唯一 ID。
     *
     * @return 設備 ID
     */
    public int getId() {
        return id;
    }

    /**
     * 獲取設備名稱。
     *
     * @return 設備名稱
     */
    public String getName() {
        return name;
    }

    /**
     * 獲取設備的 MIPS 值。
     *
     * @return MIPS 值
     */
    public double getMips() {
        return mips;
    }

    /**
     * 獲取設備的可用 CPU 資源。
     *
     * @return 可用 CPU 資源
     */
    public double getAvailableCpu() {
        return availableCpu;
    }

    /**
     * 設置設備的可用 CPU 資源。
     *
     * @param availableCpu 新的可用 CPU 資源
     */
    public void setAvailableCpu(double availableCpu) {
        this.availableCpu = availableCpu;
    }

    /**
     * 獲取設備的可用記憶體資源。
     *
     * @return 可用記憶體資源
     */
    public double getAvailableMemory() {
        return availableMemory;
    }

    /**
     * 設置設備的可用記憶體資源。
     *
     * @param availableMemory 新的可用記憶體資源
     */
    public void setAvailableMemory(double availableMemory) {
        this.availableMemory = availableMemory;
    }

    /**
     * 獲取設備每 MIPS 的能量消耗率。
     *
     * @return 每 MIPS 的能量消耗率
     */
    public double getEnergyPerMips() {
        return energyPerMips;
    }

    /**
     * 初始化設備的信任值。
     *
     * @return 初始信任值
     */
    public double initialTrustValue() {
        return 1.0; // 示例初始信任值
    }

    /**
     * 獲取設備的信任值。
     *
     * @return 信任值
     */
    public double getTrustValue() {
        return trustValue;
    }

    /**
     * 設置設備的信任值。
     *
     * @param trustValue 新的信任值
     */
    public void setTrustValue(double trustValue) {
        this.trustValue = trustValue;
    }

    /**
     * 獲取設備的 CPU 負載。
     *
     * @return CPU 負載
     */
    public double getCpuLoad() {
        return (mips - availableCpu) / mips;
    }

    /**
     * 獲取設備的記憶體負載。
     *
     * @return 記憶體負載
     */
    public double getMemoryLoad() {
        // 假設總內存為 2048 MB
        return 1.0 - (availableMemory / 2048.0);
    }

    /**
     * 獲取設備的 X 座標。
     *
     * @return X 座標
     */
    public double getX() {
        return x;
    }

    /**
     * 設置設備的 X 座標。
     *
     * @param x 新的 X 座標
     */
    public void setX(double x) {
        this.x = x;
    }

    /**
     * 獲取設備的 Y 座標。
     *
     * @return Y 座標
     */
    public double getY() {
        return y;
    }

    /**
     * 設置設備的 Y 座標。
     *
     * @param y 新的 Y 座標
     */
    public void setY(double y) {
        this.y = y;
    }

    /**
     * 獲取設備的傳輸速率。
     *
     * @return 傳輸速率，單位：Mbps
     */
    public double getTransmissionRate() {
        return transmissionRate;
    }

    /**
     * 設置設備的傳輸速率。
     *
     * @param transmissionRate 新的傳輸速率，單位：Mbps
     */
    public void setTransmissionRate(double transmissionRate) {
        this.transmissionRate = transmissionRate;
    }

    /**
     * 獲取設備的處理速度。
     *
     * @return 處理速度，單位：MIPS
     */
    public double getProcessingSpeed() {
        return processingSpeed;
    }

    /**
     * 設置設備的處理速度。
     *
     * @param processingSpeed 新的處理速度，單位：MIPS
     */
    public void setProcessingSpeed(double processingSpeed) {
        this.processingSpeed = processingSpeed;
    }

    /**
     * 獲取設備的任務大小。
     *
     * @return 任務大小，單位：MB
     */
    public double getTaskSize() {
        return taskSize;
    }

    /**
     * 設置設備的任務大小。
     *
     * @param taskSize 新的任務大小，單位：MB
     */
    public void setTaskSize(double taskSize) {
        this.taskSize = taskSize;
    }

    /**
     * 獲取設備的結果大小。
     *
     * @return 結果大小，單位：MB
     */
    public double getResultSize() {
        return resultSize;
    }

    /**
     * 設置設備的結果大小。
     *
     * @param resultSize 新的結果大小，單位：MB
     */
    public void setResultSize(double resultSize) {
        this.resultSize = resultSize;
    }

    /**
     * 獲取設備的上行帶寬。
     *
     * @return 上行帶寬，單位：Mbps
     */
    public double getUplinkBandwidth() {
        return uplinkBandwidth;
    }

    /**
     * 設置設備的上行帶寬。
     *
     * @param uplinkBandwidth 新的上行帶寬，單位：Mbps
     */
    public void setUplinkBandwidth(double uplinkBandwidth) {
        this.uplinkBandwidth = uplinkBandwidth;
    }

    /**
     * 獲取設備的下行帶寬。
     *
     * @return 下行帶寬，單位：Mbps
     */
    public double getDownlinkBandwidth() {
        return downlinkBandwidth;
    }

    /**
     * 設置設備的下行帶寬。
     *
     * @param downlinkBandwidth 新的下行帶寬，單位：Mbps
     */
    public void setDownlinkBandwidth(double downlinkBandwidth) {
        this.downlinkBandwidth = downlinkBandwidth;
    }

    /**
     * 獲取設備的上行延遲。
     *
     * @return 上行延遲，單位：ms
     */
    public double getUplinkLatency() {
        return uplinkLatency;
    }

    /**
     * 設置設備的上行延遲。
     *
     * @param uplinkLatency 新的上行延遲，單位：ms
     */
    public void setUplinkLatency(double uplinkLatency) {
        this.uplinkLatency = uplinkLatency;
    }

    /**
     * 獲取設備的資源消耗率。
     *
     * @return 每 MIPS 的資源消耗率
     */
    public double getRatePerMips() {
        return ratePerMips;
    }

    /**
     * 設置設備的資源消耗率。
     *
     * @param ratePerMips 新的資源消耗率
     */
    public void setRatePerMips(double ratePerMips) {
        this.ratePerMips = ratePerMips;
    }

    /**
     * 獲取設備的功耗模型。
     *
     * @return 功耗模型
     */
    public PowerModel getPowerModel() {
        return powerModel;
    }

    /**
     * 設置設備的功耗模型。
     *
     * @param powerModel 新的功耗模型
     */
    public void setPowerModel(PowerModel powerModel) {
        this.powerModel = powerModel;
    }

    /**
     * 獲取設備的總 CPU 資源。
     *
     * @return 總 CPU 資源，單位：MIPS
     */
    public double getTotalCpu() {
        return mips;
    }

    /**
     * 獲取設備的總記憶體資源。
     *
     * @return 總記憶體資源，單位：MB
     */
    public double getTotalMemory() {
        return availableMemory + 1024.0; // 假設總內存為 1024 MB（根據實際需求調整）
    }

    /**
     * 覆寫 toString 方法，便於打印設備信息。
     *
     * @return 設備的字符串表示
     */
    @Override
    public String toString() {
        return "RTACOFogDevice{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", mips=" + mips +
                ", availableCpu=" + availableCpu +
                ", availableMemory=" + availableMemory +
                ", energyPerMips=" + energyPerMips +
                ", trustValue=" + trustValue +
                ", x=" + x +
                ", y=" + y +
                ", transmissionRate=" + transmissionRate +
                ", processingSpeed=" + processingSpeed +
                ", taskSize=" + taskSize +
                ", resultSize=" + resultSize +
                ", uplinkBandwidth=" + uplinkBandwidth +
                ", downlinkBandwidth=" + downlinkBandwidth +
                ", uplinkLatency=" + uplinkLatency +
                ", ratePerMips=" + ratePerMips +
                '}';
    }
}
