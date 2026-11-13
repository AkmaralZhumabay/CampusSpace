package com.campusspace.patterns.creation;

import java.util.Locale;

public class RoomFactoryProvider {

    public static RoomFactory getFactory(String type) {
        if (type == null) {
            throw new IllegalArgumentException(
                    "Room type is required");
        }

        return switch (type.trim().toUpperCase(Locale.ROOT)) {
            case "STUDY" -> new StudyRoomFactory();
            case "CONFERENCE" -> new ConferenceRoomFactory();
            default -> throw new IllegalArgumentException(
                    "Unknown room type: " + type);
        };
    }
}
