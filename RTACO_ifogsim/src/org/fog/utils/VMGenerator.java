package org.fog.utils;

import org.fog.entities.VM;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class VMGenerator {

    // 生成虛擬機的方法
    public static List<VM> generateVMs(int numVms, int minCpu, int maxCpu, int minMemory, int maxMemory) {
        List<VM> vms = new ArrayList<>();
        Random rand = new Random();

        for (int i = 0; i < numVms; i++) {
            int id = i + 1;
            double trustValue = 0.3 + (1.0 - 0.3) * rand.nextDouble();  // 隨機生成信任值
            int totalCpu = minCpu + rand.nextInt(maxCpu - minCpu);
            int availableCpu = totalCpu;  // 初始 CPU 可用量等於總量
            int totalMemory = minMemory + rand.nextInt(maxMemory - minMemory);
            int availableMemory = totalMemory;  // 初始記憶體可用量等於總量
            int transmissionRate = 300 + rand.nextInt(600 - 300);  // 隨機生成傳輸速率
            int processingSpeed = 1000 + rand.nextInt(4000 - 1000);  // 隨機生成處理速度
            double x = rand.nextDouble();  // 隨機位置
            double y = rand.nextDouble();  // 隨機位置

            vms.add(new VM(id, trustValue, totalCpu, availableCpu, totalMemory, availableMemory, transmissionRate, processingSpeed, x, y));
        }

        return vms;
    }

    // 可以擴展生成不同規模的 VM 方法
    public static List<VM> generateVMs100(int numVms) {
        return generateVMs(numVms, 6, 12, 512, 4581);
    }

    public static List<VM> generateVMs200(int numVms) {
        return generateVMs(numVms, 10, 18, 1024, 5093);
    }

    // 添加 generateVMs300, generateVMs400, generateVMs500 等
}
