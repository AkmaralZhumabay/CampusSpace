package com.campusspace.patterns.creation;

import com.campusspace.model.Room;

public abstract class RoomFactory {

    public Room createRoom(String name, String location) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException(
                    "Room name is required");
        }

        RoomBuilder builder = new RoomBuilder()
                .setName(name.trim())
                .setLocation(location == null ? "" : location.trim());

        return configureRoom(builder).build();
    }

    protected abstract RoomBuilder configureRoom(
            RoomBuilder builder);
}
