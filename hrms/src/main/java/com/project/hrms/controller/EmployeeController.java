package com.project.hrms.controller;
import com.project.hrms.entity.Employee;
import com.project.hrms.entity.Holiday;
import com.project.hrms.entity.LeaveRequest;
import com.project.hrms.repository.EmployeeRepository;
import com.project.hrms.repository.HolidayRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import com.project.hrms.repository.LeaveRepository;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import com.project.hrms.config.JwtUtil;
import com.project.hrms.dto.LoginRequest;
import com.project.hrms.entity.Attendance;
import com.project.hrms.repository.AttendanceRepository;
import java.util.Optional;
import com.project.hrms.dto.SalaryResponse;
import com.project.hrms.dto.AttendanceResponse;
import com.project.hrms.dto.AttendanceUpdateRequest;
import com.project.hrms.dto.LeaveResponse;
import com.project.hrms.dto.RecentActivityResponse;
import java.util.Comparator;
@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/employees")
public class EmployeeController {
@Autowired
    private EmployeeRepository repository;
    @Autowired
private LeaveRepository leaveRepository;
@Autowired
private AttendanceRepository attendanceRepository;
@Autowired
private PasswordEncoder passwordEncoder ;
@Autowired
private JwtUtil jwtUtil;
@Autowired
private EmployeeRepository employeeRepository;
@Autowired
private HolidayRepository holidayRepository;


    // CREATE
    @PostMapping
    public Employee addEmployee(@RequestBody Employee employee) {
        employee.setPassword(passwordEncoder.encode(employee.getPassword()));
          employee.setFinalSalary(employee.getBaseSalary());
           employee.setTotalLeaveAllowed(12);

    // Current leave balance
    employee.setLeaveBalance(12);

    // Leave balance at the beginning of the month
    employee.setMonthStartLeaveBalance(12);
    employee.setFinalSalary(employee.getBaseSalary());

          
        return repository.save(employee);
    }

    // READ
    @GetMapping
    public List<Employee> getAllEmployees() {
         System.out.println("===== GET ALL EMPLOYEES CALLED =====");
        return repository.findAll();
    }
    // LOGIN
@PostMapping("/login")
public Map<String, String> login(@RequestBody LoginRequest loginRequest) {

    Employee employee = repository.findByEmail(loginRequest.getEmail())
            .orElseThrow(() -> new RuntimeException("User not found"));

      if (!passwordEncoder.matches(loginRequest.getPassword(), employee.getPassword())) {
        throw new RuntimeException("Invalid password");
    }

  String token = jwtUtil.generateToken(employee.getEmail(), employee.getRole());

Map<String, String> response = new HashMap<>();
response.put("token", token);
response.put("role", employee.getRole());

return response;
}
    // UPDATE
    @PutMapping("/{id}")
    public Employee updateEmployee(@PathVariable Long id, @RequestBody Employee updatedEmployee) {

        Employee employee = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        employee.setName(updatedEmployee.getName());
        employee.setEmail(updatedEmployee.getEmail());
        employee.setBaseSalary(updatedEmployee.getBaseSalary());
        employee.setFinalSalary(updatedEmployee.getBaseSalary());
        //employee.setLeaveBalance(updatedEmployee.getLeaveBalance());

        employee.setRole(updatedEmployee.getRole());

        return repository.save(employee);
    }

    // DELETE
    @DeleteMapping("/{id}")
    public String deleteEmployee(@PathVariable Long id) {
        repository.deleteById(id);
        return "Employee deleted successfully";
    }
    // APPLY LEAVE
@PostMapping("/leave")
public LeaveRequest applyLeave(@RequestBody LeaveRequest leaveRequest) {

    String email = SecurityContextHolder.getContext()
            .getAuthentication()
            .getName();

    Employee employee = repository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found"));

    leaveRequest.setEmployeeId(employee.getId());

    // ✅ VALIDATION START
    if (leaveRequest.getStartDate() == null || leaveRequest.getEndDate() == null) {
        throw new RuntimeException("Start date and End date are required");
    }

    if (leaveRequest.getStartDate().isAfter(leaveRequest.getEndDate())) {
        throw new RuntimeException("Start date cannot be after end date");
    }
    // 🔥 HOLIDAY VALIDATION (NEW)
/*LocalDate start = leaveRequest.getStartDate();
LocalDate end = leaveRequest.getEndDate();

while (!start.isAfter(end)) {
     // 🔥 ADD DEBUG HERE
    System.out.println("Checking date: " + start);
    System.out.println("Is holiday: " + holidayRepository.existsByDate(start));
    if (holidayRepository.existsByDate(start)) {
        throw new RuntimeException("Leave cannot be applied on holiday: " + start);
    }
    start = start.plusDays(1);
}*/
LocalDate temp = leaveRequest.getStartDate();
LocalDate end = leaveRequest.getEndDate();

long workingDays = 0;

while (!temp.isAfter(end)) {

    DayOfWeek day = temp.getDayOfWeek();

    boolean isWeekend = (day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY);
    boolean isHoliday = holidayRepository.existsByDate(temp);

    if (!isWeekend && !isHoliday) {
        workingDays++;
    }

    temp = temp.plusDays(1);
}

    if (leaveRequest.getStartDate().isBefore(LocalDate.now())) {
        throw new RuntimeException("Cannot apply leave for past dates");
    }
   /*  // ✅ MAX LEAVE DAYS VALIDATION
    long days = ChronoUnit.DAYS.between(
            leaveRequest.getStartDate(),
            leaveRequest.getEndDate()
    ) + 1;

    if (days > 30) {
        throw new RuntimeException("Leave cannot exceed 30 days");
    }*/
   if (workingDays > 30) {
    throw new RuntimeException("Leave cannot exceed 30 working days");
}//if (workingDays > employee.getLeaveBalance()) {
   // throw new RuntimeException("Insufficient leave balance");}
if (workingDays == 0) {
    throw new RuntimeException("No working days in selected range");
}
    // ✅ CHECK OVERLAPPING LEAVE
List<LeaveRequest> overlappingLeaves =
        leaveRepository.findByEmployeeIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                employee.getId(),
                leaveRequest.getEndDate(),
                leaveRequest.getStartDate()
        );

if (!overlappingLeaves.isEmpty()) {
    throw new RuntimeException("Leave dates overlap with existing leave");
}
    // ✅ VALIDATION END

    leaveRequest.setStatus("PENDING");

    return leaveRepository.save(leaveRequest);
}
@PutMapping("/leave/{id}")
public LeaveRequest updateLeaveStatus(@PathVariable Long id, @RequestBody Map<String, String> request) {

    LeaveRequest leave = leaveRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Leave request not found"));

    String status = request.get("status");
// ❗ Prevent double approval
if (leave.getStatus().equals("APPROVED")) {
    return leave;
}
    leave.setStatus(status);
    if (status.equals("APPROVED")) {

    Employee employee = repository.findById(leave.getEmployeeId())
            .orElseThrow(() -> new RuntimeException("Employee not found"));

    // 🔥 Calculate number of leave days
    LocalDate start = leave.getStartDate();
    LocalDate end = leave.getEndDate();

    //long days = ChronoUnit.DAYS.between(start, end) + 1;
    long workingDays = 0;
LocalDate temp = start;

while (!temp.isAfter(end)) {

    DayOfWeek day = temp.getDayOfWeek();

    boolean isWeekend = (day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY);
    boolean isHoliday = holidayRepository.existsByDate(temp);

    if (!isWeekend && !isHoliday) {
        workingDays++;
    }

    temp = temp.plusDays(1);
}

    if (employee.getLeaveBalance() >= workingDays) {
        // Paid leave
        employee.setLeaveBalance(
    (int)(employee.getLeaveBalance() - workingDays)
);;
    } else {
        // Partial or no leave balance
        long unpaidDays = workingDays - employee.getLeaveBalance();

        double perDaySalary = employee.getBaseSalary() / 30;
        double deduction = perDaySalary * unpaidDays;

        double newFinalSalary = employee.getFinalSalary() - deduction;
employee.setFinalSalary(newFinalSalary);

        employee.setLeaveBalance(0);
    }

    repository.save(employee);
     LocalDate current = start;

    /*while (!current.isAfter(end)) {

        Optional<Attendance> existing =
                attendanceRepository.findByEmployeeIdAndDate(
                        employee.getId(),
                        current
                );

        if (existing.isEmpty()) {
            Attendance attendance = new Attendance();
            attendance.setEmployeeId(employee.getId());
            attendance.setDate(current);
            attendance.setStatus("LEAVE");

            attendanceRepository.save(attendance);
        }

        current = current.plusDays(1);
    }*/
   

while (!current.isAfter(end)) {

    DayOfWeek day = current.getDayOfWeek();

    boolean isWeekend = (day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY);
    boolean isHoliday = holidayRepository.existsByDate(current);

    if (!isWeekend && !isHoliday) {

        Optional<Attendance> existing = attendanceRepository
            .findByEmployeeIdAndDate(employee.getId(), current);

        if (existing.isEmpty()) {
            Attendance attendance = new Attendance();
            attendance.setEmployeeId(employee.getId());
            attendance.setDate(current);
            attendance.setStatus("LEAVE");

            attendanceRepository.save(attendance);
        }
    }

    current = current.plusDays(1);
}

    
}
    return leaveRepository.save(leave);
}

/*@GetMapping("/leave/all")
public List<LeaveRequest> getAllLeaves() {

    String email = SecurityContextHolder.getContext()
            .getAuthentication()
            .getName();

    Employee loggedIn = repository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found"));

    if (!loggedIn.getRole().equals("HR")) {
        throw new RuntimeException("Access Denied");
    }

    return leaveRepository.findAll();
}*/
@GetMapping("/leave/all")
public List<LeaveResponse> getAllLeaves() {

    String email = SecurityContextHolder.getContext()
            .getAuthentication()
            .getName();

    Employee loggedIn = repository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found"));

    if (!loggedIn.getRole().equals("HR")) {
        throw new RuntimeException("Access Denied");
    }

    List<LeaveRequest> leaves = leaveRepository.findAll();

    List<LeaveResponse> response = new java.util.ArrayList<>();

    for (LeaveRequest leave : leaves) {

        Employee employee = repository.findById(leave.getEmployeeId())
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        LeaveResponse dto = new LeaveResponse();

        dto.setId(leave.getId());
        dto.setEmployeeId(leave.getEmployeeId());
        dto.setEmployeeName(employee.getName());
        dto.setEmployeeEmail(employee.getEmail());
        dto.setStartDate(leave.getStartDate());
        dto.setEndDate(leave.getEndDate());
        dto.setReason(leave.getReason());
        dto.setStatus(leave.getStatus());

        response.add(dto);
    }

    return response;
}
@GetMapping("/leave/me")
public List<LeaveRequest> getMyLeaves() {

    String email = SecurityContextHolder.getContext()
            .getAuthentication()
            .getName();

    Employee employee = repository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found"));

    // Reuse the existing method
    return getEmployeeLeaves(employee.getId());

}
/*@GetMapping("/leave/pending")
public List<LeaveRequest> getPendingLeaves() {

    String email = SecurityContextHolder.getContext()
            .getAuthentication()
            .getName();

    Employee loggedIn = repository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found"));

    if (!loggedIn.getRole().equals("HR")) {
        throw new RuntimeException("Access Denied");
    }

    return leaveRepository.findByStatus("PENDING");
}*/
@GetMapping("/leave/pending")
public List<LeaveResponse> getPendingLeaves() {

    String email = SecurityContextHolder.getContext()
            .getAuthentication()
            .getName();

    Employee loggedIn = repository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found"));

    if (!loggedIn.getRole().equals("HR")) {
        throw new RuntimeException("Access Denied");
    }

    List<LeaveRequest> leaves = leaveRepository.findByStatus("PENDING");

    List<LeaveResponse> response = new java.util.ArrayList<>();

    for (LeaveRequest leave : leaves) {

        Employee employee = repository.findById(leave.getEmployeeId())
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        LeaveResponse dto = new LeaveResponse();

        dto.setId(leave.getId());
        dto.setEmployeeId(leave.getEmployeeId());
        dto.setEmployeeName(employee.getName());
        dto.setEmployeeEmail(employee.getEmail());
        dto.setStartDate(leave.getStartDate());
        dto.setEndDate(leave.getEndDate());
        dto.setReason(leave.getReason());
        dto.setStatus(leave.getStatus());

        response.add(dto);
    }

    return response;
}
/*@GetMapping("/leave/approved")
public List<LeaveRequest> getApprovedLeaves() {
    return leaveRepository.findByStatus("APPROVED");
}*/
@GetMapping("/leave/approved")
public List<LeaveResponse> getApprovedLeaves() {

    List<LeaveRequest> leaves = leaveRepository.findByStatus("APPROVED");

    List<LeaveResponse> response = new java.util.ArrayList<>();

    for (LeaveRequest leave : leaves) {

        Employee employee = repository.findById(leave.getEmployeeId())
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        LeaveResponse dto = new LeaveResponse();

        dto.setId(leave.getId());
        dto.setEmployeeId(leave.getEmployeeId());
        dto.setEmployeeName(employee.getName());
        dto.setEmployeeEmail(employee.getEmail());
        dto.setStartDate(leave.getStartDate());
        dto.setEndDate(leave.getEndDate());
        dto.setReason(leave.getReason());
        dto.setStatus(leave.getStatus());

        response.add(dto);
    }

    return response;
}
/*@GetMapping("/leave/rejected")
public List<LeaveRequest> getRejectedLeaves() {
    return leaveRepository.findByStatus("REJECTED");
}*/
@GetMapping("/leave/rejected")
public List<LeaveResponse> getRejectedLeaves() {

    List<LeaveRequest> leaves = leaveRepository.findByStatus("REJECTED");

    List<LeaveResponse> response = new java.util.ArrayList<>();

    for (LeaveRequest leave : leaves) {

        Employee employee = repository.findById(leave.getEmployeeId())
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        LeaveResponse dto = new LeaveResponse();

        dto.setId(leave.getId());
        dto.setEmployeeId(leave.getEmployeeId());
        dto.setEmployeeName(employee.getName());
        dto.setEmployeeEmail(employee.getEmail());
        dto.setStartDate(leave.getStartDate());
        dto.setEndDate(leave.getEndDate());
        dto.setReason(leave.getReason());
        dto.setStatus(leave.getStatus());

        response.add(dto);
    }

    return response;
}
@GetMapping("/{id}/leaves")
public List<LeaveRequest> getEmployeeLeaves(@PathVariable Long id) {

    // ✅ Get logged-in user
    String email = SecurityContextHolder.getContext()
            .getAuthentication()
            .getName();

    Employee loggedIn = repository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found"));

    // 🔥 SECURITY CHECK
    if (!loggedIn.getRole().equals("HR") && !loggedIn.getId().equals(id)) {
        throw new RuntimeException("Access Denied");
    }

    return leaveRepository.findByEmployeeId(id);
}

@PostMapping("/attendance")
public Attendance markAttendance(@RequestBody Attendance attendance) {

    String email = SecurityContextHolder.getContext()
            .getAuthentication()
            .getName();

    Employee employee = repository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found"));

    attendance.setEmployeeId(employee.getId());
        if (holidayRepository.existsByDate(attendance.getDate())) {
    throw new RuntimeException("Cannot mark attendance on a holiday");
}         
        // 🔥 WEEKEND CHECK (ADD THIS)
DayOfWeek day = attendance.getDate().getDayOfWeek();

if (day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY) {
    throw new RuntimeException("Cannot mark attendance on weekend");
}
    // CHECK DUPLICATE ATTENDANCE
    Optional<Attendance> existingAttendance =
            attendanceRepository.findByEmployeeIdAndDate(
                    employee.getId(),
                    attendance.getDate()
            );
            System.out.println("Employee ID: " + employee.getId());
System.out.println("Date: " + attendance.getDate());
System.out.println(existingAttendance);

    if (existingAttendance.isPresent()) {
        throw new RuntimeException("Attendance already marked for this date");
    }
    // 👉 If marking ABSENT manually
if ("ABSENT".equalsIgnoreCase(attendance.getStatus())) {

    if (employee.getLeaveBalance() > 0) {
        employee.setLeaveBalance(employee.getLeaveBalance() - 1);
        employeeRepository.save(employee);

        System.out.println("Leave balance reduced for employee: " + employee.getId());
    }
}

    return attendanceRepository.save(attendance);
}
@GetMapping("/{id}/attendance")
public List<Attendance> getAttendance(@PathVariable Long id) {

    String email = SecurityContextHolder.getContext()
            .getAuthentication()
            .getName();

    Employee loggedInEmployee = repository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found"));

    String role = loggedInEmployee.getRole();

    // HR can access anyone
    if (role.equals("HR")) {
        return attendanceRepository.findByEmployeeId(id);
    }

    // Employee can access only own attendance
    if (!loggedInEmployee.getId().equals(id)) {
        throw new RuntimeException("Access Denied");
    }

    return attendanceRepository.findByEmployeeId(id);
}
@GetMapping("/my-attendance")
public List<Attendance> getMyAttendance() {

    String email = SecurityContextHolder.getContext()
            .getAuthentication()
            .getName();

    Employee employee = repository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found"));

    return attendanceRepository.findByEmployeeId(employee.getId());
}
@PutMapping("/hr/attendance/{attendanceId}")
public Attendance updateAttendance(
        @PathVariable Long attendanceId,
        @RequestBody Attendance updatedAttendance
) {

    Attendance existingAttendance = attendanceRepository
            .findById(attendanceId)
            .orElseThrow(() -> new RuntimeException("Attendance not found"));

    existingAttendance.setStatus(updatedAttendance.getStatus());

    return attendanceRepository.save(existingAttendance);
}
@PostMapping("/hr/attendance")
public Attendance createOrUpdateAttendance(
        @RequestBody AttendanceUpdateRequest request
) {

    LocalDate today = LocalDate.now();

    Optional<Attendance> existing =
            attendanceRepository.findByEmployeeIdAndDate(
                    request.getEmployeeId(),
                    today
            );

    if (existing.isPresent()) {

        Attendance attendance = existing.get();

        attendance.setStatus(request.getStatus());

        return attendanceRepository.save(attendance);

    }

    Attendance attendance = new Attendance();

    attendance.setEmployeeId(request.getEmployeeId());

    attendance.setDate(today);

    attendance.setStatus(request.getStatus());

    return attendanceRepository.save(attendance);
}
@GetMapping("/attendance/all")
public List<AttendanceResponse> getAllAttendance() {

    String email = SecurityContextHolder.getContext()
            .getAuthentication()
            .getName();

    Employee loggedIn = repository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found"));

    if (!loggedIn.getRole().equals("HR")) {
        throw new RuntimeException("Access Denied");
    }

    LocalDate today = LocalDate.now();

    List<Employee> employees = repository.findAll();

List<AttendanceResponse> response = new ArrayList<>();

for (Employee employee : employees) {

    // Don't show HR users
    if (employee.getRole().equalsIgnoreCase("HR")) {
        continue;
    }

    Optional<Attendance> attendance =
            attendanceRepository.findByEmployeeIdAndDate(
                    employee.getId(),
                    today
            );

    if (attendance.isPresent()) {

        response.add(new AttendanceResponse(
                attendance.get().getId(),
                employee.getId(),
                employee.getName(),
                employee.getRole(),
                today,
                attendance.get().getStatus()
        ));

    } else {

        response.add(new AttendanceResponse(
                null,
                employee.getId(),
                employee.getName(),
                employee.getRole(),
                today,
                "NOT MARKED"
        ));

    }
}

return response;
}
/*@GetMapping("/attendance/all")
public List<AttendanceResponse> getAllAttendance() {

    String email = SecurityContextHolder.getContext()
            .getAuthentication()
            .getName();

    Employee loggedIn = repository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found"));

    if (!loggedIn.getRole().equals("HR")) {
        throw new RuntimeException("Access Denied");
    }

    LocalDate today = LocalDate.now();

    List<Employee> employees = repository.findAll();

    List<AttendanceResponse> response = new ArrayList<>();

    for (Employee employee : employees) {

        Optional<Attendance> attendance =
                attendanceRepository.findByEmployeeIdAndDate(
                        employee.getId(),
                        today
                );

        if (attendance.isPresent()) {

            response.add(

                new AttendanceResponse(

                        attendance.get().getId(),

                        employee.getId(),

                        employee.getName(),

                        employee.getRole(),

                        attendance.get().getDate(),

                        attendance.get().getStatus()

                )

            );

        } else {

            response.add(

                new AttendanceResponse(

                        null,

                        employee.getId(),

                        employee.getName(),

                        employee.getRole(),

                        today,

                        "NOT MARKED"

                )

            );

        }

    }

    return response;
}*/
/*@GetMapping("/{id}/calculate-salary")
public double calculateSalary(@PathVariable Long id) {

    Employee employee = repository.findById(id)
            .orElseThrow(() -> new RuntimeException("Employee not found"));

    // 1. Attendance → ABSENT days
    List<Attendance> attendanceList = attendanceRepository.findByEmployeeId(id);

    long absentDays = attendanceList.stream()
            .filter(a -> a.getStatus().equalsIgnoreCase("ABSENT"))
            .count();

    // 2. Leaves → TOTAL leave days
    List<LeaveRequest> leaves = leaveRepository.findByEmployeeId(id);

    long totalLeaveDays = leaves.stream()
            .filter(l -> l.getStatus().equalsIgnoreCase("APPROVED"))
            .mapToLong(l -> ChronoUnit.DAYS.between(
                   l.getStartDate(),
l.getEndDate()
            ) + 1)
            .sum();

    // 3. Unpaid leave calculation
   int originalLeaveBalance = employee.getTotalLeaveAllowed(); // ⚠️ change based on test case
long unpaidLeaves = 0;

if (totalLeaveDays > originalLeaveBalance) {
    unpaidLeaves = totalLeaveDays - originalLeaveBalance;
}
  // 4. Total unpaid days
    long totalUnpaidDays = absentDays + unpaidLeaves;

    // 5. Salary calculation
    double perDaySalary = employee.getBaseSalary() / 30;

    double finalSalary = employee.getBaseSalary()
            - (totalUnpaidDays * perDaySalary);

    employee.setFinalSalary(finalSalary);
    repository.save(employee);

    return finalSalary;
}*/
@GetMapping("/{id}/calculate-salary")
public SalaryResponse calculateSalary(@PathVariable Long id)  {

    Employee employee = repository.findById(id)
            .orElseThrow(() -> new RuntimeException("Employee not found"));

    // 1. Attendance → ABSENT days
    List<Attendance> attendanceList = attendanceRepository.findByEmployeeId(id);

    long absentDays = attendanceList.stream()
            .filter(a -> a.getStatus().equalsIgnoreCase("ABSENT"))
            .count();

    // 2. Leaves → TOTAL leave days
    List<LeaveRequest> leaves = leaveRepository.findByEmployeeId(id);

    long totalLeaveDays = leaves.stream()
            .filter(l -> l.getStatus().equalsIgnoreCase("APPROVED"))
            .mapToLong(l -> ChronoUnit.DAYS.between(
                   l.getStartDate(),
l.getEndDate()
            ) + 1)
            .sum();

    // 3. Unpaid leave calculation
   int originalLeaveBalance = employee.getTotalLeaveAllowed(); // ⚠️ change based on test case
long unpaidLeaves = 0;

if (totalLeaveDays > originalLeaveBalance) {
    unpaidLeaves = totalLeaveDays - originalLeaveBalance;
}
  // 4. Total unpaid days
    long totalUnpaidDays = absentDays + unpaidLeaves;

    // 5. Salary calculation
    double perDaySalary = employee.getBaseSalary() / 30;

    double finalSalary = employee.getBaseSalary()
            - (totalUnpaidDays * perDaySalary);

    employee.setFinalSalary(finalSalary);
    repository.save(employee);

    double deduction = employee.getBaseSalary() - finalSalary;

return new SalaryResponse(

        employee.getName(),

        employee.getRole(),

        employee.getBaseSalary(),

        finalSalary,

        employee.getLeaveBalance(),

        absentDays,

        totalLeaveDays,

        unpaidLeaves,

        deduction

);
}
@GetMapping("/salary/me")
public SalaryResponse getMySalary() {

    String email = SecurityContextHolder
            .getContext()
            .getAuthentication()
            .getName();

    Employee employee = repository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found"));

    // Reuse existing method
    return calculateSalary(employee.getId());

}
@GetMapping("/dashboard")
public Map<String, Object> getEmployeeDashboard() {

    String email = SecurityContextHolder.getContext()
            .getAuthentication()
            .getName();

    Employee employee = repository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found"));

    Map<String, Object> response = new HashMap<>();

    // ✅ Leave balance
    response.put("leaveBalance", employee.getLeaveBalance());

    // ✅ Total leaves taken (this month)
    LocalDate start = LocalDate.now().withDayOfMonth(1);
    LocalDate end = start.withDayOfMonth(start.lengthOfMonth());
long leavesTaken = leaveRepository
    .findByEmployeeIdAndStatus(employee.getId(), "APPROVED")
    .stream()
    .mapToLong(l -> {

        LocalDate temp = l.getStartDate().isBefore(start) ? start : l.getStartDate();
        LocalDate leaveEnd = l.getEndDate().isAfter(end) ? end : l.getEndDate();

        long count = 0;

        while (!temp.isAfter(leaveEnd)) {

            DayOfWeek day = temp.getDayOfWeek();

            boolean isWeekend = (day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY);
            boolean isHoliday = holidayRepository.existsByDate(temp);

            if (!isWeekend && !isHoliday) {
                count++;
            }

            temp = temp.plusDays(1);
        }

        return count;

    }).sum();
    
    response.put("leavesTaken", leavesTaken);
    
    // ✅ Attendance summary
    long presentDays = attendanceRepository
            .countByEmployeeIdAndStatusAndDateBetween(
                    employee.getId(), "PRESENT", start, end);

    long absentDays = attendanceRepository
            .countByEmployeeIdAndStatusAndDateBetween(
                    employee.getId(), "ABSENT", start, end);

    response.put("presentDays", presentDays);
    response.put("absentDays", absentDays);

    // ✅ Salary
    response.put("salary", employee.getFinalSalary());

    return response;
}
@GetMapping("/calendar")
public Map<LocalDate, String> getAttendanceCalendar(
        @RequestParam int month,
        @RequestParam int year) {

    String email = SecurityContextHolder.getContext()
            .getAuthentication()
            .getName();

    Employee employee = repository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found"));

    Map<LocalDate, String> calendar = new LinkedHashMap<>();

    LocalDate start = LocalDate.of(year, month, 1);
    LocalDate end = start.withDayOfMonth(start.lengthOfMonth());

    LocalDate current = start;

    while (!current.isAfter(end)) {
         LocalDate tempDate = current;

        DayOfWeek day = current.getDayOfWeek();

        boolean isWeekend = (day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY);
        boolean isHoliday = holidayRepository.existsByDate(current);

        if (isWeekend) {
            calendar.put(current, "WEEKEND");

        } else if (isHoliday) {
            calendar.put(current, "HOLIDAY");

        } else {

            // Check leave
            boolean isLeave = leaveRepository
                    .findByEmployeeIdAndStatus(employee.getId(), "APPROVED")
                    .stream()
                    .anyMatch(l ->
    !tempDate.isBefore(l.getStartDate()) &&
    !tempDate.isAfter(l.getEndDate())

                    );

            if (isLeave) {
                calendar.put(current, "LEAVE");

            } else {
                // Check attendance
                Optional<Attendance> attendance =
                        attendanceRepository.findByEmployeeIdAndDate(
                                employee.getId(), current);

                if (attendance.isPresent()) {

    calendar.put(current, attendance.get().getStatus());

}
else if (!current.isBefore(LocalDate.now())) {

    // Today and future dates
    calendar.put(current, "FUTURE");

}
else {

    // Past dates only
    calendar.put(current, "ABSENT");

}
            }
        }

        current = current.plusDays(1);
    }

    return calendar;
}
@GetMapping("/reports/summary")
public Map<String, Object> getReportSummary() {

    Map<String, Object> response = new HashMap<>();

    List<Employee> employees = repository.findAll();

    LocalDate today = LocalDate.now();

    long totalEmployees = employees.size();

    long hrMembers = employees.stream()
            .filter(e -> e.getRole().equalsIgnoreCase("HR"))
            .count();

    long present = attendanceRepository
            .findByDate(today)
            .stream()
            .filter(a -> a.getStatus().equalsIgnoreCase("PRESENT"))
            .count();

    long absent = attendanceRepository
            .findByDate(today)
            .stream()
            .filter(a -> a.getStatus().equalsIgnoreCase("ABSENT"))
            .count();

    long leave = attendanceRepository
            .findByDate(today)
            .stream()
            .filter(a -> a.getStatus().equalsIgnoreCase("LEAVE"))
            .count();

    double totalPayroll = employees.stream()
            .mapToDouble(Employee::getFinalSalary)
            .sum();
            double highestSalary = employees.stream()
        .mapToDouble(Employee::getFinalSalary)
        .max()
        .orElse(0);

double lowestSalary = employees.stream()
        .mapToDouble(Employee::getFinalSalary)
        .min()
        .orElse(0);

double averageSalary =
        totalEmployees == 0
                ? 0
                : totalPayroll / totalEmployees;

    response.put("totalEmployees", totalEmployees);
    response.put("hrMembers", hrMembers);
    response.put("present", present);
    response.put("absent", absent);
    response.put("leave", leave);
    response.put("totalPayroll", totalPayroll);
    response.put("highestSalary", highestSalary);
response.put("lowestSalary", lowestSalary);
response.put("averageSalary", averageSalary);

    return response;
}
@GetMapping("/reports/leave-summary")
public Map<String, Long> getLeaveSummary() {

    Map<String, Long> response = new HashMap<>();

    response.put(
            "pending",
            leaveRepository.findAll().stream()
                    .filter(l -> l.getStatus().equalsIgnoreCase("PENDING"))
                    .count()
    );

    response.put(
            "approved",
            leaveRepository.findAll().stream()
                    .filter(l -> l.getStatus().equalsIgnoreCase("APPROVED"))
                    .count()
    );

    response.put(
            "rejected",
            leaveRepository.findAll().stream()
                    .filter(l -> l.getStatus().equalsIgnoreCase("REJECTED"))
                    .count()
    );

    return response;
}
@GetMapping("/profile")
public Employee getProfile() {

    String email = SecurityContextHolder
            .getContext()
            .getAuthentication()
            .getName();

    return repository.findByEmail(email)
            .orElseThrow(() ->
                    new RuntimeException("User not found"));
}
@GetMapping("/holidays/upcoming")
public List<Holiday> getUpcomingHolidays() {

    return holidayRepository
            .findByDateGreaterThanEqualOrderByDateAsc(
                    LocalDate.now()
            );

}
@GetMapping("/recent-activity")
public List<RecentActivityResponse> getRecentActivity() {

    String email = SecurityContextHolder.getContext()
            .getAuthentication()
            .getName();

    Employee employee = repository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found"));

    List<RecentActivityResponse> activities = new ArrayList<>();

    // Latest Attendance
    List<Attendance> attendanceList =
            attendanceRepository.findTop5ByEmployeeIdOrderByDateDesc(
                    employee.getId()
            );

    for (Attendance attendance : attendanceList) {

        activities.add(

                new RecentActivityResponse(

                        "ATTENDANCE",

                        "Attendance marked as " + attendance.getStatus(),

                        attendance.getDate()

                )

        );

    }

    // Latest Leave Requests
    List<LeaveRequest> leaveList =
            leaveRepository.findTop5ByEmployeeIdOrderByStartDateDesc(
                    employee.getId()
            );

    for (LeaveRequest leave : leaveList) {

        activities.add(

                new RecentActivityResponse(

                        "LEAVE",

                        "Leave " + leave.getStatus(),

                        leave.getStartDate()

                )

        );

    }

    // Sort latest first
    activities.sort(

            Comparator.comparing(RecentActivityResponse::getDate)

                    .reversed()

    );

    // Return only latest 5
    return activities.stream()

            .limit(5)

            .toList();

}
}