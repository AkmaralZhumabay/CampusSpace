package com.campusspace.patterns.adapter;

import java.io.InputStream;
import java.util.List;

public interface ScheduleImporter {

    List<ScheduleRow> readSchedule(InputStream input);
}
