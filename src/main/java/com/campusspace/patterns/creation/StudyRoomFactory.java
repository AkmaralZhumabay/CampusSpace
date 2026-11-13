package com.campusspace.patterns.creation;

public class StudyRoomFactory extends RoomFactory {

    @Override
    protected RoomBuilder configureRoom(RoomBuilder builder) {
        return builder
                .setCapacity(6)
                .setProjector(false)
                .setWhiteboard(true);
    }
}
