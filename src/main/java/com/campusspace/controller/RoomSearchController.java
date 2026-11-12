
package com.campusspace.controller;

import com.campusspace.model.Room;
import com.campusspace.patterns.facade.CampusBookingFacade;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/search")
public class RoomSearchController {

    private final CampusBookingFacade bookingFacade;

    public RoomSearchController(
            CampusBookingFacade bookingFacade) {
        this.bookingFacade = bookingFacade;
    }

    @GetMapping("/rooms")
    public List<Room> searchRooms(
            @RequestParam int people,
            @RequestParam(defaultValue = "BEST_FIT")
            String strategy) {

        return bookingFacade.findSuitableRooms(
                people,
                strategy
        );
    }
}
