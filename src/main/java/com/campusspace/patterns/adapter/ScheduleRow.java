package com.campusspace.patterns.adapter;

import java.time.LocalDateTime;

public record ScheduleRow(
        Long roomId,
        String courseName,
        LocalDateTime startTime,
        LocalDateTime endTime
) {
}
