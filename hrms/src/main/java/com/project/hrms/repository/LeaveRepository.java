package com.project.hrms.repository;

import com.project.hrms.entity.LeaveRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface LeaveRepository extends JpaRepository<LeaveRequest, Long> {
    List<LeaveRequest> findByEmployeeIdAndStatus(Long employeeId, String status);
    List<LeaveRequest> findByStatus(String status);
    

List<LeaveRequest> findByEmployeeId(Long employeeId);
List<LeaveRequest> findByEmployeeIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
        Long employeeId,
        LocalDate endDate,
        LocalDate startDate
);
List<LeaveRequest> findTop5ByEmployeeIdOrderByStartDateDesc(
        Long employeeId
);
}
