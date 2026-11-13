package com.campusspace.service;

import com.campusspace.model.*;
import com.campusspace.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.campusspace.policy.RoomAccessPolicy;
import com.campusspace.patterns.observer.BookingEventPublisher;
import com.campusspace.repository.ScheduleRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final RoomAccessPolicy roomAccessPolicy;
    private final RoomRepository roomRepository;
    private final StudentRepository studentRepository;
    private final BookingEventPublisher bookingEventPublisher;
    private final ScheduleRepository scheduleRepository;



    public BookingService(
            BookingRepository bookingRepository,
            RoomRepository roomRepository,
            StudentRepository studentRepository,
            RoomAccessPolicy roomAccessPolicy,
            BookingEventPublisher bookingEventPublisher,
            ScheduleRepository scheduleRepository) {

        this.bookingRepository = bookingRepository;
        this.roomRepository = roomRepository;
        this.studentRepository = studentRepository;
        this.roomAccessPolicy = roomAccessPolicy;
        this.bookingEventPublisher = bookingEventPublisher;
        this.scheduleRepository = scheduleRepository;
    }




    @Transactional
    public Booking createBooking(
            Long roomId,
            Long studentId,
            LocalDateTime startTime,
            LocalDateTime endTime) {

        if (roomId == null || studentId == null) {
            throw new IllegalArgumentException(
                    "Room and student are required");
        }

        if (startTime == null || endTime == null) {
            throw new IllegalArgumentException(
                    "Start and end time are required");
        }

        if (!startTime.isBefore(endTime)) {
            throw new IllegalArgumentException(
                    "End time must be after start time");
        }

        if (!startTime.isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException(
                    "Booking must start in the future");
        }

        Room room = roomRepository.findById(roomId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Room not found"));

        if (!roomAccessPolicy.isBookable(room)) {
            throw new IllegalArgumentException(
                    "This room is not available for booking");
        }

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Student not found"));

        List<Booking> conflicts =
                bookingRepository
                        .findByRoomIdAndStatusAndStartTimeLessThanAndEndTimeGreaterThan(
                                roomId,
                                BookingStatus.CONFIRMED,
                                endTime,
                                startTime);

        if (!conflicts.isEmpty()) {
            throw new IllegalArgumentException(
                    "Room is already booked for this time");
        }


        boolean hasScheduledClass = !scheduleRepository
                .findByRoomIdAndStartTimeLessThanAndEndTimeGreaterThan(
                        roomId,
                        endTime,
                        startTime)
                .isEmpty();

        if (hasScheduledClass) {
            throw new IllegalArgumentException(
                    "Room is occupied by a university class at this time");
        }

        Booking booking = new Booking(
                room, student, startTime, endTime);

        Booking savedBooking = bookingRepository.save(booking);

        bookingEventPublisher.publish(savedBooking, "CONFIRMED");

        return savedBooking;
    }

    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }

    public List<Booking> getStudentBookings(Long studentId) {
        return bookingRepository.findByStudentId(studentId);
    }

    @Transactional
    public Booking cancelBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Booking not found"));

        if (booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new IllegalArgumentException(
                    "Only confirmed bookings can be cancelled");
        }

        booking.setStatus(BookingStatus.CANCELLED);
        Booking cancelledBooking = bookingRepository.save(booking);

        bookingEventPublisher.publish(cancelledBooking, "CANCELLED");

        return cancelledBooking;
    }
}
