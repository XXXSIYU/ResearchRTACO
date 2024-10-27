package org.fog.utils;

import org.fog.entities.Task;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TaskGenerator {

    // 生成隨機任務列表
    public static List<Task> generateTasks(int numTasks) {
        List<Task> tasks = new ArrayList<>();
        Random rand = new Random();

        for (int i = 0; i < numTasks; i++) {
            int id = i + 1;
            double cpuRequirement = 0.5 + (1.0 - 0.5) * rand.nextDouble(); // 隨機生成 CPU 需求
            int memoryRequirement = rand.nextInt(512 - 2) + 2;  // 隨機生成記憶體需求
            double deadline = 0.4 + (0.6 - 0.4) * rand.nextDouble();  // 任務完成期限
            int taskSize = rand.nextInt(10 - 1) + 1;  // 任務大小
            int length = rand.nextInt(500 - 1) + 1;  // 任務長度
            double resultSize = taskSize * 0.1;  // 結果大小為任務大小的 10%

            tasks.add(new Task(id, cpuRequirement, memoryRequirement, deadline, taskSize, length, resultSize));
        }

        return tasks;
    }
}
