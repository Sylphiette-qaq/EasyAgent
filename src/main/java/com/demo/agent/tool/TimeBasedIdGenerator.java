package com.demo.agent.tool;

import java.time.Instant;
import java.util.concurrent.ThreadLocalRandom;

public class TimeBasedIdGenerator {

    /**
     * 生成一个基于时间的 10 位数 ID
     * 格式：前8位为时间戳秒数的后8位 + 后2位随机数
     */
    public static long generateId() {
        // 获取当前秒级时间戳
        long seconds = Instant.now().getEpochSecond(); // 通常是10位或以上

        // 获取后8位（从时间戳的末尾截取8位）
        String timePart = String.valueOf(seconds);
        if (timePart.length() > 8) {
            timePart = timePart.substring(timePart.length() - 8);
        }

        // 生成 2 位随机数
        int randomPart = ThreadLocalRandom.current().nextInt(10, 99);

        // 拼接成10位数
        String idStr = timePart + randomPart;

        return Long.parseLong(idStr);
    }

}
