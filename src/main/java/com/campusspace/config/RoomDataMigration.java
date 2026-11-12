
package com.campusspace.config;

import com.campusspace.model.Room;
import com.campusspace.policy.RoomAccessPolicy;
import com.campusspace.repository.RoomRepository;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class RoomDataMigration implements CommandLineRunner {

    private final RoomRepository roomRepository;
    private final RoomAccessPolicy accessPolicy;

    private static final Pattern BLOCK_PATTERN =
            Pattern.compile("\\b(C\\d+(?:\\.\\d+)?)\\b");

    private static final Pattern FLOOR_PATTERN =
            Pattern.compile("\\bFLOOR\\s*(\\d+)\\b");

    private static final Pattern ROOM_PATTERN =
            Pattern.compile("^\\d{3}[A-Z]?$");

    public RoomDataMigration(
            RoomRepository roomRepository,
            RoomAccessPolicy accessPolicy) {

        this.roomRepository = roomRepository;
        this.accessPolicy = accessPolicy;
    }

    @Override
    public void run(String... args) {

        List<Room> rooms = roomRepository.findAll();

        for (Room room : rooms) {

            // Do not overwrite manually reviewed metadata.
            if (room.getBlock() != null ||
                    room.getRoomNumber() != null ||
                    room.getFloor() != null) {
                continue;
            }

            String name = room.getName() == null
                    ? ""
                    : room.getName().trim()
                    .toUpperCase(Locale.ROOT);

            String location = room.getLocation() == null
                    ? ""
                    : room.getLocation()
                    .toUpperCase(Locale.ROOT);

            Matcher blockMatch =
                    BLOCK_PATTERN.matcher(location);

            Matcher floorMatch =
                    FLOOR_PATTERN.matcher(location);

            // Leave uncertain legacy rooms unreviewed.
            if (!ROOM_PATTERN.matcher(name).matches()
                    || !blockMatch.find()
                    || !floorMatch.find()) {
                continue;
            }

            String block = blockMatch.group(1);
            int floor = Integer.parseInt(
                    floorMatch.group(1));

            String building = block.split("\\.")[0];

            room.setBuilding(building);
            room.setBlock(block);
            room.setFloor(floor);
            room.setRoomNumber(name);

            // Temporarily mark as bookable,
            // then apply the administrative restrictions.
            room.setBookable(true);

            if (!accessPolicy.isBookable(room)) {
                room.setBookable(false);
            }

            roomRepository.save(room);

            System.out.println(
                    "Room metadata migrated: "
                            + building + " / "
                            + block + " / "
                            + name
            );
        }
    }
}
