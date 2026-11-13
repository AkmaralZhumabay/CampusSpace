package com.campusspace.controller;

import com.campusspace.model.ScheduleEntry;
import com.campusspace.service.ScheduleService;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/schedule")
public class ScheduleController {

    private final ScheduleService scheduleService;

    public ScheduleController(ScheduleService scheduleService) {
        this.scheduleService = scheduleService;
    }

    @PostMapping(
            value = "/import",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public List<ScheduleEntry> importCsv(
            @RequestParam("file") MultipartFile file)
            throws IOException {

        if (file.isEmpty()) {
            throw new IllegalArgumentException(
                    "Schedule file cannot be empty");
        }

        return scheduleService.importSchedule(
                file.getInputStream());
    }

    @GetMapping
    public List<ScheduleEntry> getSchedule() {
        return scheduleService.getSchedule();
    }
}
