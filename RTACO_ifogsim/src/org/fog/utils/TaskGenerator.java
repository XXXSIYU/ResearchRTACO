package org.fog.utils;

import org.fog.entities.Task;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TaskGenerator {

    // 生成隨機任務列表
    public static List<Task> generateTasks(int numTasks) {
        List<Task> tasks = new ArrayList<>();
        Random rand = new Random(); // 可選：傳入種子以確保重複性

        for (int i = 0; i < numTasks; i++) {
            int id = i + 1;
            // 隨機生成 CPU 需求（單位：核數，範圍：0.5 到 1.0）
            double cpuRequirement = 0.5 + (1.0 - 0.5) * rand.nextDouble();
            // 隨機生成記憶體需求（單位：MB，範圍：2 到 512）
            int memoryRequirement = rand.nextInt(512 - 2 + 1) + 2;
            // 隨機生成完成期限（單位：秒，範圍：0.4 到 0.6）
            double deadline = 0.4 + (0.6 - 0.4) * rand.nextDouble();
            // 隨機生成任務大小（單位：MB，範圍：1 到 10）
            int taskSize = rand.nextInt(10 - 1 + 1) + 1;
            // 隨機生成任務長度（單位：MI，範圍：1 到 500）
            int length = rand.nextInt(500 - 1 + 1) + 1;
            // 計算結果大小（單位：MB），為任務大小的 10%
            double resultSize = taskSize * 0.1;

            // 創建 Task 物件，參數順序與構造函數一致
            tasks.add(new Task(id, cpuRequirement, memoryRequirement, length, taskSize, resultSize, deadline));
        }

        return tasks;
    }
}
