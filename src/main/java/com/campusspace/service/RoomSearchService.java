package com.campusspace.service;

import com.campusspace.model.Room;
import com.campusspace.repository.RoomRepository;
import com.campusspace.policy.RoomAccessPolicy;
import com.campusspace.patterns.strategy.*;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RoomSearchService {

    private final RoomRepository roomRepository;
    private final RoomAccessPolicy roomAccessPolicy;

    public RoomSearchService(
            RoomRepository roomRepository,
            RoomAccessPolicy roomAccessPolicy) {

        this.roomRepository = roomRepository;
        this.roomAccessPolicy = roomAccessPolicy;
    }

    public List<Room> searchRooms(
            int people,
            String strategyType) {

        if (people <= 0) {
            throw new IllegalArgumentException(
                    "Number of people must be greater than zero");
        }

        List<Room> suitableRooms = roomRepository.findAll()
                .stream()
                .filter(room -> roomAccessPolicy.isBookable(room))
                .filter(room -> room.getCapacity() >= people)
                .toList();

        RoomSearchStrategy strategy;

        if ("BEST_FIT".equalsIgnoreCase(strategyType)) {
            strategy = new BestFitStrategy();
        } else if ("LARGEST_FIRST".equalsIgnoreCase(strategyType)) {
            strategy = new LargestFirstStrategy();
        } else {
            throw new IllegalArgumentException(
                    "Unknown search strategy");
        }

        return strategy.sortRooms(suitableRooms);
    }
}
