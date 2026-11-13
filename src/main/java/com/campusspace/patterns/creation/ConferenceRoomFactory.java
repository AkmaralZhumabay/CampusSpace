package com.campusspace.patterns.creation;

public class ConferenceRoomFactory extends RoomFactory {

    @Override
    protected RoomBuilder configureRoom(RoomBuilder builder) {
        return builder
                .setCapacity(12)
                .setProjector(true)
                .setWhiteboard(true);
    }
}
