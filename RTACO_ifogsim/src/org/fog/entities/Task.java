package org.fog.entities;

/**
 * 這個類表示 Fog 模擬中的一個任務，包括其 CPU 和內存需求、長度、任務大小、結果大小等屬性。
 * 任務 ID 和完成期限 (Deadline) 也包括在內，用於 Fog 資源管理中的任務分配和優先級調整。
 */
public class Task {
    private int id;  // 任務的唯一 ID
    private double cpuRequirement;   // 任務的 CPU 需求
    private double memoryRequirement; // 任務的內存需求
    private double length;  // 任務的長度，通常表示指令數量
    private double taskSize;  // 任務的大小，通常表示需要傳輸的數據量
    private double resultSize;  // 任務執行後的結果大小
    private double deadline;  // 任務的完成期限

    // 完整的構造函數，包括 ID、CPU 和內存需求、長度、任務大小、結果大小、完成期限
    public Task(int id, double cpuRequirement, double memoryRequirement, double length, double taskSize, double resultSize, double deadline) {
        this.id = id;
        this.cpuRequirement = cpuRequirement;
        this.memoryRequirement = memoryRequirement;
        this.length = length;
        this.taskSize = taskSize;
        this.resultSize = resultSize;
        this.deadline = deadline;
    }

    // 獲取任務的 ID
    public int getId() {
        return id;
    }

    // 獲取任務的 CPU 需求
    public double getCpuRequirement() {
        return cpuRequirement;
    }

    // 獲取內存需求
    public double getMemoryRequirement() {
        return memoryRequirement;
    }

    // 獲取任務的長度
    public double getLength() {
        return length;
    }

    // 獲取任務的大小
    public double getTaskSize() {
        return taskSize;
    }

    // 獲取任務的結果大小
    public double getResultSize() {
        return resultSize;
    }

    // 獲取任務的完成期限
    public double getDeadline() {
        return deadline;
    }
}
