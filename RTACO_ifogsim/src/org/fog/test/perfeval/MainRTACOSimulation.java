package org.fog.test.perfeval;

import org.cloudbus.cloudsim.core.CloudSim;
import org.fog.application.Application;
import org.fog.entities.FogDevice;
import org.fog.placement.RTACOPlacement;
import org.fog.scheduler.StreamOperatorScheduler;
import org.fog.utils.FogUtils;
import org.fog.utils.Logger;
import org.fog.utils.NetworkUsageMonitor;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import org.fog.application.AppModule; 


public class MainRTACOSimulation {

    public static void main(String[] args) {
        Logger.ENABLED = true;  // 启用日志

        try {
            // 1. 初始化 CloudSim
            int numUser = 1;  // 用户数量
            Calendar calendar = Calendar.getInstance();
            CloudSim.init(numUser, calendar, false);

            // 2. 创建 Fog 设备和应用模块
            List<FogDevice> fogDevices = createFogDevices();
            List<Application> applications = createApplications();

            // 3. 初始化 RTACOPlacement 并分配任务
            RTACOPlacement rtacoPlacement = new RTACOPlacement(fogDevices, applications.get(0).getModules());
            CloudSim.startSimulation();

            // 4. 执行模拟
            for (FogDevice fogDevice : fogDevices) {
                fogDevice.startEntity();
            }

            CloudSim.stopSimulation();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 模拟环境下创建 Fog 设备
    private static List<FogDevice> createFogDevices() {
        List<FogDevice> fogDevices = new ArrayList<>();
        // 在这里创建 Fog 设备
        // FogDevice device1 = new FogDevice(...);
        // fogDevices.add(device1);
        return fogDevices;
    }

 // 创建应用程序
    private static List<Application> createApplications() {
        List<Application> applications = new ArrayList<>();

        // 创建应用程序并添加模块
        Application app1 = new Application("App1", 1);  // 假设用户 ID 为 1;
        app1.addModule(new AppModule("Module1", 100));  // 假设模块需要 MIPS 参数
        applications.add(app1);

        return applications;
    }

}
