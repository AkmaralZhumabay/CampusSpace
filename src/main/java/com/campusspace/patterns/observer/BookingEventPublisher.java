package com.campusspace.patterns.observer;

import com.campusspace.model.Booking;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;

@Component
public class BookingEventPublisher {

    private final List<BookingObserver> observers;

    public BookingEventPublisher(List<BookingObserver> observers) {
        this.observers = observers;
    }

    public void publish(Booking booking, String eventType) {
        Runnable notifyObservers = () -> {
            for (BookingObserver observer : observers) {
                observer.update(booking, eventType);
            }
        };

        if (TransactionSynchronizationManager
                .isActualTransactionActive()
                && TransactionSynchronizationManager
                .isSynchronizationActive()) {

            TransactionSynchronizationManager
                    .registerSynchronization(
                            new TransactionSynchronization() {
                                @Override
                                public void afterCommit() {
                                    notifyObservers.run();
                                }
                            }
                    );
        } else {
            notifyObservers.run();
        }
    }
}
