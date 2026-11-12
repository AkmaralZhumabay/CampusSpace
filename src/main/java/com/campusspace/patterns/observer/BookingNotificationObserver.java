package com.campusspace.patterns.observer;

import com.campusspace.model.Booking;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class BookingNotificationObserver implements BookingObserver {

    private final List<String> notifications =
            new CopyOnWriteArrayList<>();

    @Override
    public void update(Booking booking, String eventType) {
        String message = "Booking #" + booking.getId()
                + " " + eventType
                + " - Room " + booking.getRoom().getName();

        notifications.add(message);
        System.out.println("Notification: " + message);
    }

    public List<String> getNotifications() {
        return List.copyOf(notifications);
    }
}
