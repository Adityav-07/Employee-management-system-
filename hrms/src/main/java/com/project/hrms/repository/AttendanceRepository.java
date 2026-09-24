package com.project.hrms.repository;

import com.project.hrms.entity.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    List<Attendance> findByEmployeeIdAndStatus(Long employeeId, String status);

    List<Attendance> findByEmployeeId(Long employeeId);
    Optional<Attendance> findByEmployeeIdAndDate(Long employeeId, LocalDate date);
    long countByEmployeeIdAndStatusAndDateBetween(
    Long employeeId,
    String status,
    LocalDate startDate,
    LocalDate endDate
);
List<Attendance> findByDate(LocalDate date);
List<Attendance> findTop5ByEmployeeIdOrderByDateDesc(
        Long employeeId
);
}