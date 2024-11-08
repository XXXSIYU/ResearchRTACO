package org.fog.utils;

import org.fog.entities.VM;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * VMGenerator 類用於生成指定數量的虛擬機（VM），每個 VM 具有隨機生成的屬性。
 */
public class VMGenerator {

    /**
     * 生成指定數量的虛擬機。
     *
     * @param numVms 要生成的虛擬機數量
     * @param minCpu 最小 CPU 核心數
     * @param maxCpu 最大 CPU 核心數
     * @param minMemory 最小記憶體（MB）
     * @param maxMemory 最大記憶體（MB）
     * @return 虛擬機列表
     */
    public static List<VM> generateVMs(int numVms, int minCpu, int maxCpu, int minMemory, int maxMemory) {
        List<VM> vms = new ArrayList<>();
        Random rand = new Random();

        for (int i = 0; i < numVms; i++) {
            int id = i + 1;
            // 信任值在 0.3 到 1.0 之間
            double trustValue = 0.3 + (1.0 - 0.3) * rand.nextDouble();  
            // CPU 核心數，範圍 [minCpu, maxCpu]
            int totalCpu = minCpu + rand.nextInt(maxCpu - minCpu + 1);
            int availableCpu = totalCpu;  // 初始 CPU 可用量等於總量
            // 記憶體，範圍 [minMemory, maxMemory]，單位：MB
            int totalMemory = minMemory + rand.nextInt(maxMemory - minMemory + 1);
            int availableMemory = totalMemory;  // 初始記憶體可用量等於總量
            // 傳輸速率，範圍 [300, 600]，單位：Mbps
            int transmissionRate = 300 + rand.nextInt(600 - 300 + 1);  
            // 處理速度，範圍 [1000, 4000]，單位：MIPS
            int processingSpeed = 1000 + rand.nextInt(4000 - 1000 + 1);  
            // 位置，範圍 [0.0, 100.0]
            double x = rand.nextDouble() * 100.0;  
            double y = rand.nextDouble() * 100.0;  

            vms.add(new VM(id, trustValue, totalCpu, availableCpu, totalMemory, availableMemory, transmissionRate, processingSpeed, x, y));
        }

        return vms;
    }

    // 可擴展生成不同類型的 VM 方法
    public static List<VM> generateVMsTypeA(int numVms) {
        return generateVMs(numVms, 6, 12, 512, 4581);
    }

    public static List<VM> generateVMsTypeB(int numVms) {
        return generateVMs(numVms, 10, 18, 1024, 5093);
    }

    // 可以繼續添加 generateVMsTypeC, generateVMsTypeD 等方法
}
