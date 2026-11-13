
package com.campusspace.service;

import com.campusspace.model.*;
import com.campusspace.patterns.adapter.*;
import com.campusspace.repository.*;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Service
public class ScheduleService {

    private final ScheduleImporter scheduleImporter;
    private final ScheduleRepository scheduleRepository;
    private final BookingRepository bookingRepository;
    private final RoomRepository roomRepository;

    public ScheduleService(
            ScheduleImporter scheduleImporter,
            ScheduleRepository scheduleRepository,
            BookingRepository bookingRepository,
            RoomRepository roomRepository) {

        this.scheduleImporter = scheduleImporter;
        this.scheduleRepository = scheduleRepository;
        this.bookingRepository = bookingRepository;
        this.roomRepository = roomRepository;
    }

    @Transactional
    public List<ScheduleEntry> importSchedule(InputStream input) {

        List<ScheduleRow> rows =
                scheduleImporter.readSchedule(input);

        List<ScheduleEntry> entries = new ArrayList<>();

        for (ScheduleRow row : rows) {

            Room room = roomRepository.findById(row.roomId())
                    .orElseThrow(() ->
                            new IllegalArgumentException(
                                    "Unknown room ID: " + row.roomId()
                            )
                    );

            // Check existing university classes
            boolean classConflict = !scheduleRepository
                    .findByRoomIdAndStartTimeLessThanAndEndTimeGreaterThan(
                            room.getId(),
                            row.endTime(),
                            row.startTime()
                    ).isEmpty();

            // Check existing student bookings
            boolean bookingConflict = !bookingRepository
                    .findByRoomIdAndStatusAndStartTimeLessThanAndEndTimeGreaterThan(
                            room.getId(),
                            BookingStatus.CONFIRMED,
                            row.endTime(),
                            row.startTime()
                    ).isEmpty();

            // Check overlaps within the uploaded CSV
            boolean fileConflict = entries.stream().anyMatch(
                    entry ->
                            entry.getRoom().getId().equals(room.getId())
                                    && row.startTime().isBefore(entry.getEndTime())
                                    && row.endTime().isAfter(entry.getStartTime())
            );

            if (classConflict || bookingConflict || fileConflict) {
                throw new IllegalArgumentException(
                        "Schedule conflict in room "
                                + room.getName()
                                + " (" + room.getLocation() + ")"
                );
            }

            entries.add(new ScheduleEntry(
                    room,
                    row.courseName(),
                    row.startTime(),
                    row.endTime()
            ));
        }

        return scheduleRepository.saveAll(entries);
    }

    public List<ScheduleEntry> getSchedule() {
        return scheduleRepository.findAll();
    }
}
