package com.campusspace.controller;

import com.campusspace.model.Booking;
import com.campusspace.patterns.facade.CampusBookingFacade;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final CampusBookingFacade bookingFacade;

    public BookingController(
            CampusBookingFacade bookingFacade) {
        this.bookingFacade = bookingFacade;
    }

    public record BookingRequest(
            Long roomId,
            Long studentId,
            LocalDateTime startTime,
            LocalDateTime endTime
    ) {}

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Booking createBooking(
            @RequestBody BookingRequest request) {

        return bookingFacade.bookRoom(
                request.roomId(),
                request.studentId(),
                request.startTime(),
                request.endTime()
        );
    }

    @GetMapping
    public List<Booking> getAllBookings() {
        return bookingFacade.getAllBookings();
    }

    @GetMapping("/student/{studentId}")
    public List<Booking> getStudentBookings(
            @PathVariable Long studentId) {

        return bookingFacade.getStudentBookings(
                studentId
        );
    }

    @PutMapping("/{id}/cancel")
    public Booking cancelBooking(
            @PathVariable Long id) {

        return bookingFacade.cancelBooking(id);
    }
}
