package com.campusspace.controller;

import com.campusspace.patterns.observer.BookingNotificationObserver;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationsController {

    private final BookingNotificationObserver notificationObserver;

    public NotificationsController(
            BookingNotificationObserver notificationObserver) {
        this.notificationObserver = notificationObserver;
    }

    @GetMapping
    public List<String> getNotifications() {
        return notificationObserver.getNotifications();
    }
}
