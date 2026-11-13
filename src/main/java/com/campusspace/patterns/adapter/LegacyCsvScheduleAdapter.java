package com.campusspace.patterns.adapter;

import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

@Component
public class LegacyCsvScheduleAdapter
        implements ScheduleImporter {

    @Override
    public List<ScheduleRow> readSchedule(InputStream input) {
        List<ScheduleRow> rows = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(input, StandardCharsets.UTF_8))) {

            String line;
            int lineNumber = 0;

            while ((line = reader.readLine()) != null) {
                lineNumber++;

                if (lineNumber == 1 &&
                        line.trim().equalsIgnoreCase(
                                "room_id,course,start,end")) {
                    continue;
                }

                if (line.isBlank()) {
                    continue;
                }

                // Demo CSV format: four columns without quoted commas
                String[] parts = line.split(",", -1);

                if (parts.length != 4) {
                    throw new IllegalArgumentException(
                            "Invalid CSV at line " + lineNumber);
                }

                try {
                    Long roomId = Long.parseLong(parts[0].trim());
                    String course = parts[1].trim();
                    LocalDateTime start =
                            LocalDateTime.parse(parts[2].trim());
                    LocalDateTime end =
                            LocalDateTime.parse(parts[3].trim());

                    if (roomId <= 0 || course.isBlank() ||
                            !start.isBefore(end)) {
                        throw new IllegalArgumentException(
                                "Invalid schedule values");
                    }

                    rows.add(new ScheduleRow(
                            roomId, course, start, end));

                } catch (NumberFormatException |
                         DateTimeParseException e) {
                    throw new IllegalArgumentException(
                            "Invalid values at CSV line " + lineNumber,
                            e);
                } catch (IllegalArgumentException e) {
                    throw new IllegalArgumentException(
                            "Invalid values at CSV line " + lineNumber,
                            e);
                }
            }

        } catch (IOException e) {
            throw new IllegalArgumentException(
                    "Cannot read schedule file", e);
        }

        if (rows.isEmpty()) {
            throw new IllegalArgumentException(
                    "Schedule file is empty");
        }

        return rows;
    }
}
