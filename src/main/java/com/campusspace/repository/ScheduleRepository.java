package com.campusspace.repository;

import com.campusspace.model.ScheduleEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface ScheduleRepository
        extends JpaRepository<ScheduleEntry, Long> {

    List<ScheduleEntry>
    findByRoomIdAndStartTimeLessThanAndEndTimeGreaterThan(
            Long roomId,
            LocalDateTime endTime,
            LocalDateTime startTime
    );
}
