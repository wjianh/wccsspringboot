package com.wjh.utils;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;

public class SystemUtils {
    public static Date getSystemDate(){
        ZoneId zoneId = ZoneId.systemDefault();
        ZonedDateTime zdt = LocalDateTime.now().atZone(zoneId);
        Date dateTime = Date.from(zdt.toInstant());//将localDateTime 转为date类型
        return dateTime;
    }
}
