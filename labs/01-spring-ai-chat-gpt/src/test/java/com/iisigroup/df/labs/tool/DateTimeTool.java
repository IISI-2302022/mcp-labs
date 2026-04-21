package com.iisigroup.df.labs.tool;

import lombok.val;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.context.i18n.LocaleContextHolder;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class DateTimeTool {

    @Tool(description = "Get the current date and time in the user's timezone")
    public String getCurrentDateTime() {
        return LocalDateTime.now().atZone(LocaleContextHolder.getTimeZone().toZoneId()).toString();
    }

    @Tool(description = "Set a user alarm for the given time")
    public void setAlarm(@ToolParam(description = "Time in ISO-8601 format") String time) {
        val alarmTime = LocalDateTime.parse(time, DateTimeFormatter.ISO_DATE_TIME);
        System.out.println("Alarm set for " + alarmTime);
    }

    @Tool(description = "UUID 產生器")
    public String generateUUID(@ToolParam(required = false, description = "前幾碼") Integer length, ToolContext toolContext) {
        val uuid = UUID.randomUUID().toString().replace("-", "");
        if (length == null) {
            length = (Integer) toolContext.getContext().get("defaultLength");
        }
        if (length >= uuid.length()) {
            return uuid;
        }
        return uuid.substring(0, length);
    }


}