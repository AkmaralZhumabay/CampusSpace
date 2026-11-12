package com.campusspace.patterns.observer;

import com.campusspace.model.Booking;

public interface BookingObserver {
    void update(Booking booking, String eventType);
}
