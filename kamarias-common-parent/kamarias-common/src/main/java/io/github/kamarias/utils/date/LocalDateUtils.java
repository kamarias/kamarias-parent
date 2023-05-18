package io.github.kamarias.utils.date;

import java.time.*;
import java.util.Date;

/**
 * LocalDate时间工具类
 * @author wangyuxing@gogpay.cn
 * @date 2023/2/21 14:23
 */
public class LocalDateUtils {

    /**
     * 获取两个 LocalDateTime 相差多少秒数
     *
     * @param startDateTime
     * @param endDateTime
     * @return
     */
    public static long getSecondsBetween(LocalDateTime startDateTime, LocalDateTime endDateTime) {
        Duration duration = Duration.between(startDateTime, endDateTime);
        long seconds = duration.getSeconds();
        return seconds;
    }


    /**
     * Date 转 LocalDateTime
     *
     * @param date
     * @return
     */
    public static LocalDateTime toLocalDateTime(Date date) {
        Instant instant = date.toInstant();
        ZoneId zoneId = ZoneId.systemDefault();
        LocalDateTime localDateTime = instant.atZone(zoneId).toLocalDateTime();
        return localDateTime;
    }

    /**
     * 获取到毫秒级时间戳
     *
     * @param localDateTime 具体时间
     * @return long 毫秒级时间戳
     */
    public static long toEpochMilli(LocalDateTime localDateTime) {
        return localDateTime.toInstant(ZoneOffset.of("+8")).toEpochMilli();
    }

    /**
     * 毫秒级时间戳转 LocalDateTime
     *
     * @param epochMilli 毫秒级时间戳
     * @return LocalDateTime
     */
    public static LocalDateTime ofEpochMilli(long epochMilli) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(epochMilli), ZoneOffset.of("+8"));
    }

    /**
     * 获取到秒级时间戳
     *
     * @param localDateTime 具体时间
     * @return long 秒级时间戳
     */
    public static long toEpochSecond(LocalDateTime localDateTime) {
        return localDateTime.toEpochSecond(ZoneOffset.of("+8"));
    }

    /**
     * 秒级时间戳转 LocalDateTime
     *
     * @param epochSecond 秒级时间戳
     * @return LocalDateTime
     */
    public static LocalDateTime ofEpochSecond(long epochSecond) {
        return LocalDateTime.ofEpochSecond(epochSecond, 0, ZoneOffset.of("+8"));
    }

}
