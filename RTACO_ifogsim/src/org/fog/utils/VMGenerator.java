package org.fog.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.fog.entities.RTACOFogDevice;
import org.cloudbus.cloudsim.power.models.PowerModel;
import org.cloudbus.cloudsim.power.models.PowerModelLinear;

public class VMGenerator {

    /**
     * 生成指定數量的 RTACOFogDevice。
     *
     * @param numDevices 要生成的設備數量
     * @return RTACOFogDevice 的列表
     * @throws Exception 如果創建設備時發生錯誤
     */
    public static List<RTACOFogDevice> generateRTACOFogDevices(int numDevices) throws Exception {
        List<RTACOFogDevice> devices = new ArrayList<>();
        Random rand = new Random();

        for (int i = 0; i < numDevices; i++) {
            String name = "RTACODevice-" + i;
            // 設備的 MIPS，範圍 [1000, 4000]
            long mips = 1000 + rand.nextInt(3001);
            // 記憶體，範圍 [2048, 8192]，單位：MB
            int ram = 2048 + rand.nextInt(6145);
            // 上行帶寬，範圍 [1000, 2000]，單位：Mbps
            double uplinkBandwidth = 1000 + rand.nextInt(1001);
            // 下行帶寬，範圍 [1000, 2000]，單位：Mbps
            double downlinkBandwidth = 1000 + rand.nextInt(1001);
            // 每 MIPS 的資源消耗率
            double ratePerMips = 0.01;
            // 傳輸速率，範圍 [300, 600]，單位：Mbps
            double transmissionRate = 300 + rand.nextInt(301);
            // 上行延遲，範圍 [10, 100]，單位：ms
            double uplinkLatency = 10 + rand.nextDouble() * 90;


            // 使用簡單的線性功耗模型
            PowerModel powerModel = new PowerModelLinear(100, 1000);

            // 創建 RTACOFogDevice 實例，傳遞 transmissionRate
            RTACOFogDevice device = new RTACOFogDevice(
                    name,
                    mips,
                    ram,
                    uplinkBandwidth,
                    downlinkBandwidth,
                    uplinkLatency, // 新增 uplinkLatency
                    ratePerMips,
                    powerModel,
                    transmissionRate
            );

            devices.add(device);
        }

        return devices;
    }
}
