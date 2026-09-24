package com.project.hrms.entity;

import java.time.LocalDate;

import jakarta.persistence.*;
//import lombok.*;

@Entity
//@Data
//@NoArgsConstructor
//@AllArgsConstructor
public class Attendance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long employeeId;
    private LocalDate date;   // "2026-05-07"
    private String status; // PRESENT / ABSENT
    // ===== GETTERS & SETTERS =====

public Long getId() { return id; }

public Long getEmployeeId() { return employeeId; }
public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }

public LocalDate getDate() { return date; }
public void setDate(LocalDate date) { this.date = date; }

public String getStatus() { return status; }
public void setStatus(String status) { this.status = status; }
}