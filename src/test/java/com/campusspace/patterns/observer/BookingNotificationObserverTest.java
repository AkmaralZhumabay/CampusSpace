package com.campusspace.patterns.observer;

import com.campusspace.model.Booking;
import com.campusspace.model.Room;
import com.campusspace.model.Student;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BookingNotificationObserverTest {

    @Test
    void testBookingNotificationIsCreated() {
        BookingNotificationObserver observer =
                new BookingNotificationObserver();

        Room room = new Room(
                "Study Room", 6, "C1.2", false, true);

        Student student = new Student(
                "Test Student", "test@example.com");

        Booking booking = new Booking(
                room,
                student,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(1).plusHours(2)
        );

        observer.update(booking, "CONFIRMED");

        List<String> notifications = observer.getNotifications();

        assertEquals(1, notifications.size());
        assertTrue(notifications.get(0).contains("CONFIRMED"));
        assertTrue(notifications.get(0).contains("Study Room"));
    }

    @Test
    void testNotificationListIsReturnedAsCopy() {
        BookingNotificationObserver observer =
                new BookingNotificationObserver();

        List<String> notifications = observer.getNotifications();

        assertTrue(notifications.isEmpty());

        assertThrows(UnsupportedOperationException.class,
                () -> notifications.add("Fake notification"));

        assertTrue(observer.getNotifications().isEmpty());
    }

    @Test
    void testMultipleNotificationsAreStored() {
        BookingNotificationObserver observer =
                new BookingNotificationObserver();

        Room room = new Room(
                "Conference Room", 12, "C1.2", true, true);

        Student student = new Student(
                "Test Student", "test@example.com");

        Booking booking = new Booking(
                room,
                student,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(1).plusHours(2)
        );

        observer.update(booking, "CONFIRMED");
        observer.update(booking, "CANCELLED");

        List<String> notifications = observer.getNotifications();

        assertEquals(2, notifications.size());
        assertTrue(notifications.get(0).contains("CONFIRMED"));
        assertTrue(notifications.get(1).contains("CANCELLED"));
    }
}
