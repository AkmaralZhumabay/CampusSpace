package com.campusspace.repository;

import com.campusspace.model.Booking;
import com.campusspace.model.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository
        extends JpaRepository<Booking, Long> {

    List<Booking> findByRoomIdAndStatusAndStartTimeLessThanAndEndTimeGreaterThan(
            Long roomId,
            BookingStatus status,
            LocalDateTime endTime,
            LocalDateTime startTime
    );

    List<Booking> findByStudentId(Long studentId);
}
