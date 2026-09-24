package com.project.hrms.dto;

import java.time.LocalDate;

public class AttendanceResponse {

    private Long attendanceId;
    private Long employeeId;
    private String employeeName;
    private LocalDate date;
    private String status;
    private String role;

    public AttendanceResponse(
        Long attendanceId,
        Long employeeId,
        String employeeName,
        String role,
        LocalDate date,
        String status
) {
    this.attendanceId = attendanceId;
    this.employeeId = employeeId;
    this.employeeName = employeeName;
    this.role = role;
    this.date = date;
    this.status = status;
}

    public Long getAttendanceId() {
        return attendanceId;
    }

    public Long getEmployeeId() {
        return employeeId;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public LocalDate getDate() {
        return date;
    }

    public String getStatus() {
        return status;
    }
    public String getRole() {
    return role;
}
}