package com.campusspace.patterns;

import com.campusspace.model.Room;
import com.campusspace.patterns.creation.StudyRoomFactory;
import com.campusspace.patterns.creation.ConferenceRoomFactory;
import com.campusspace.patterns.strategy.BestFitStrategy;
import com.campusspace.patterns.strategy.LargestFirstStrategy;
import com.campusspace.policy.RoomAccessPolicy;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class DesignPatternsTest {

    @Test
    void testStudyRoomFactory() {
        Room room = new StudyRoomFactory()
                .createRoom("Study Test", "C1.2");

        assertEquals(6, room.getCapacity());
        assertTrue(room.isHasWhiteboard());
        assertFalse(room.isHasProjector());
    }

    @Test
    void testConferenceRoomFactory() {
        Room room = new ConferenceRoomFactory()
                .createRoom("Conference Test", "C1.2");

        assertEquals(12, room.getCapacity());
        assertTrue(room.isHasProjector());
        assertTrue(room.isHasWhiteboard());
    }

    @Test
    void testBestFitStrategy() {
        Room small = new Room("Small", 6, "C1.2", false, true);
        Room large = new Room("Large", 24, "C1.2", true, true);

        List<Room> result = new BestFitStrategy()
                .sortRooms(List.of(large, small));

        assertEquals(6, result.get(0).getCapacity());
        assertEquals(24, result.get(1).getCapacity());
    }

    @Test
    void testLargestFirstStrategy() {
        Room small = new Room("Small", 6, "C1.2", false, true);
        Room large = new Room("Large", 24, "C1.2", true, true);

        List<Room> result = new LargestFirstStrategy()
                .sortRooms(List.of(small, large));

        assertEquals(24, result.get(0).getCapacity());
        assertEquals(6, result.get(1).getCapacity());
    }

    @Test
    void testAdministrativeRoomRestriction() {
        Room room = new Room();
        room.setBuilding("C1");
        room.setBlock("C1.1");
        room.setFloor(2);
        room.setRoomNumber("265");
        room.setBookable(true);

        RoomAccessPolicy policy = new RoomAccessPolicy();

        assertFalse(policy.isBookable(room));
    }

    @Test
    void testAllowedRoom() {
        Room room = new Room();
        room.setBuilding("C1");
        room.setBlock("C1.2");
        room.setFloor(2);
        room.setRoomNumber("242K");
        room.setBookable(true);

        RoomAccessPolicy policy = new RoomAccessPolicy();

        assertTrue(policy.isBookable(room));
    }

    @Test
    void testEmptyRoomList() {
        List<Room> result = new BestFitStrategy()
                .sortRooms(List.of());

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testSingleRoomSorting() {
        Room room = new Room("Single Room", 10, "C1.2", false, true);

        List<Room> result = new LargestFirstStrategy()
                .sortRooms(List.of(room));

        assertEquals(1, result.size());
        assertEquals("Single Room", result.get(0).getName());
    }
}
