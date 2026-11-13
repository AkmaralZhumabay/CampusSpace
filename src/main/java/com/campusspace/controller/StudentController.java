package com.campusspace.controller;

import com.campusspace.model.Student;
import com.campusspace.repository.StudentRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/students")
public class StudentController {

    private final StudentRepository studentRepository;

    public StudentController(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    public record StudentRequest(
            String fullName,
            String email
    ) {}

    @GetMapping
    public List<Student> getStudents() {
        return studentRepository.findAll();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Student createStudent(
            @RequestBody StudentRequest request) {

        if (request.fullName() == null ||
                request.fullName().isBlank()) {
            throw new IllegalArgumentException(
                    "Student name is required");
        }

        if (request.email() == null ||
                !request.email().matches(
                        "^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            throw new IllegalArgumentException(
                    "Valid email is required");
        }

        String email = request.email().trim();

        if (studentRepository.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException(
                    "Student email already exists");
        }

        return studentRepository.save(
                new Student(request.fullName().trim(), email)
        );
    }
}
