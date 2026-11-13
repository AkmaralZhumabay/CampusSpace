package com.campusspace.service;

import com.campusspace.model.*;
import com.campusspace.repository.*;
import com.campusspace.policy.RoomAccessPolicy;
import com.campusspace.patterns.observer.BookingEventPublisher;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private RoomAccessPolicy roomAccessPolicy;

    @Mock
    private BookingEventPublisher bookingEventPublisher;

    @Mock
    private ScheduleRepository scheduleRepository;

    @InjectMocks
    private BookingService bookingService;

    private Room room;
    private Student student;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    @BeforeEach
    void setUp() {
        room = new Room("Study Room", 6, "C1.2", false, true);
        student = mock(Student.class);

        startTime = LocalDateTime.now().plusDays(1);
        endTime = startTime.plusHours(2);
    }

    private void prepareValidBooking() {
        when(roomRepository.findById(1L))
                .thenReturn(Optional.of(room));

        when(studentRepository.findById(1L))
                .thenReturn(Optional.of(student));

        when(roomAccessPolicy.isBookable(room))
                .thenReturn(true);

        when(bookingRepository
                .findByRoomIdAndStatusAndStartTimeLessThanAndEndTimeGreaterThan(
                        eq(1L),
                        eq(BookingStatus.CONFIRMED),
                        any(LocalDateTime.class),
                        any(LocalDateTime.class)))
                .thenReturn(List.of());

        when(scheduleRepository
                .findByRoomIdAndStartTimeLessThanAndEndTimeGreaterThan(
                        eq(1L),
                        any(LocalDateTime.class),
                        any(LocalDateTime.class)))
                .thenReturn(List.of());

        when(bookingRepository.save(any(Booking.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void testSuccessfulBookingCreation() {
        prepareValidBooking();

        Booking result = bookingService.createBooking(
                1L, 1L, startTime, endTime);

        assertNotNull(result);
        assertEquals(room, result.getRoom());
        assertEquals(student, result.getStudent());
        assertEquals(startTime, result.getStartTime());
        assertEquals(endTime, result.getEndTime());
        assertEquals(BookingStatus.CONFIRMED, result.getStatus());

        verify(bookingRepository).save(any(Booking.class));
    }

    @Test
    void testObserverNotificationAfterBooking() {
        prepareValidBooking();

        Booking result = bookingService.createBooking(
                1L, 1L, startTime, endTime);

        verify(bookingEventPublisher)
                .publish(result, "CONFIRMED");
    }

    @Test
    void testInvalidBookingTime() {
        LocalDateTime invalidStart = LocalDateTime.now().plusDays(1);
        LocalDateTime invalidEnd = invalidStart.minusHours(1);

        assertThrows(IllegalArgumentException.class, () ->
                bookingService.createBooking(
                        1L, 1L, invalidStart, invalidEnd));

        verify(bookingRepository, never()).save(any());
    }

    @Test
    void testRestrictedRoomCannotBeBooked() {
        when(roomRepository.findById(1L))
                .thenReturn(Optional.of(room));

        when(roomAccessPolicy.isBookable(room))
                .thenReturn(false);

        assertThrows(IllegalArgumentException.class, () ->
                bookingService.createBooking(
                        1L, 1L, startTime, endTime));

        verify(bookingRepository, never()).save(any());
    }

    @Test
    void testOverlappingBookingIsRejected() {
        when(roomRepository.findById(1L))
                .thenReturn(Optional.of(room));

        when(studentRepository.findById(1L))
                .thenReturn(Optional.of(student));

        when(roomAccessPolicy.isBookable(room))
                .thenReturn(true);

        Booking existingBooking = mock(Booking.class);

        when(bookingRepository
                .findByRoomIdAndStatusAndStartTimeLessThanAndEndTimeGreaterThan(
                        eq(1L),
                        eq(BookingStatus.CONFIRMED),
                        any(LocalDateTime.class),
                        any(LocalDateTime.class)))
                .thenReturn(List.of(existingBooking));

        assertThrows(IllegalArgumentException.class, () ->
                bookingService.createBooking(
                        1L, 1L, startTime, endTime));

        verify(bookingRepository, never()).save(any());
    }
}
