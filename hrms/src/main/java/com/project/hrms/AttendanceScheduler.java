package com.project.hrms;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import com.project.hrms.entity.Employee;
import com.project.hrms.entity.Salary;
//import com.project.hrms.entity.Salary;
import com.project.hrms.entity.Attendance;
//import com.project.hrms.entity.LeaveRequest;

import com.project.hrms.repository.EmployeeRepository;
import com.project.hrms.repository.HolidayRepository;
import com.project.hrms.repository.AttendanceRepository;
import com.project.hrms.repository.LeaveRepository;
//import com.project.hrms.repository.SalaryRepository;
import com.project.hrms.repository.SalaryRepository;


import java.time.DayOfWeek;
import java.time.LocalDate;
//import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
public class AttendanceScheduler {

    @Autowired
    private EmployeeRepository employeeRepository;

@Autowired
private SalaryRepository salaryRepository;

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private LeaveRepository leaveRepository;
    @Autowired
private HolidayRepository holidayRepository;

    // 🔥 TEMP TEST (runs every 1 min)
@Scheduled(cron = "0 59 23 * * ?") // 11:59 PM
    public void markAbsent() {

        LocalDate today = LocalDate.now();
        // 🔥 HOLIDAY CHECK (NEW)
        if (holidayRepository.existsByDate(today)) {
    System.out.println("Holiday detected - skipping attendance for: " + today);
    return;
}
        // 🔥 WEEKEND CHECK (ADD THIS)
DayOfWeek day = today.getDayOfWeek();

if (day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY) {
    System.out.println("Weekend detected - skipping attendance for: " + today);
    return;
}

        List<Employee> employees = employeeRepository.findAll();

        for (Employee emp : employees) {
            System.out.println("Checking employee: " + emp.getId());

            boolean hasAttendance = attendanceRepository
                    .findByEmployeeIdAndDate(emp.getId(), today)
                    .isPresent();
                    System.out.println("Has Attendance: " + hasAttendance);

            boolean hasLeave = leaveRepository
                    .findByEmployeeIdAndStatus(emp.getId(), "APPROVED")
                    .stream()
                    .anyMatch(leave ->
                            !today.isBefore(leave.getStartDate()) &&
                            !today.isAfter(leave.getEndDate())
                    );

System.out.println("Has Leave: " + hasLeave);

            if (!hasAttendance && !hasLeave) {
                System.out.println("MARKING ABSENT FOR: " + emp.getId());

                // 🔥 CORRECT LOGIC (leave first, then salary)

                if (emp.getLeaveBalance() > 0) {

                    emp.setLeaveBalance(emp.getLeaveBalance() - 1);

                    Attendance attendance = new Attendance();
                    attendance.setEmployeeId(emp.getId());
                    attendance.setDate(today);
                    attendance.setStatus("ABSENT");

                    attendanceRepository.save(attendance);

                } else {

                    Attendance attendance = new Attendance();
                    attendance.setEmployeeId(emp.getId());
                    attendance.setDate(today);
                    attendance.setStatus("ABSENT");

                    attendanceRepository.save(attendance);

                    double perDaySalary = emp.getBaseSalary() / 30;
                    emp.setFinalSalary(emp.getFinalSalary() - perDaySalary);
                }

                employeeRepository.save(emp);
            }
        }

        System.out.println("✅ Scheduler ran for: " + today);
    }
   


 @Scheduled(cron = "0 0 0 1 * ?") // runs on 1st of every month




public void storeMonthStartLeaveBalance() {

    List<Employee> employees = employeeRepository.findAll();

    for (Employee emp : employees) {
        emp.setMonthStartLeaveBalance(emp.getLeaveBalance());
        employeeRepository.save(emp);
    }

    System.out.println("Month start leave balance stored ✅");
}
   
@Scheduled(cron = "0 59 23 L * ?") // last day of month
public void generateMonthlySalary() {

    List<Employee> employees = employeeRepository.findAll();

    for (Employee emp : employees) {

       LocalDate start = LocalDate.now().withDayOfMonth(1);
LocalDate end = start.withDayOfMonth(start.lengthOfMonth());

       /*  long presentDays = attendanceRepository
    .countByEmployeeIdAndStatusAndDateBetween(
        emp.getId(),
        "PRESENT",
        start,
        end
    );*/

int totalDays = end.lengthOfMonth();
// 🔥 GET HOLIDAYS COUNT
//long holidays = holidayRepository.countByDateBetween(start, end);

// 🔥 CALCULATE WORKING DAYS
//int workingDays = totalDays - (int) holidays;
   int workingDays = 0;

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
//int leaveBalance = emp.getLeaveBalance();

// Calculate absent days
//long absentDays = totalDays - presentDays;
long absentDays = attendanceRepository
    .countByEmployeeIdAndStatusAndDateBetween(
        emp.getId(),
        "ABSENT",
        start,
        end
    );

// Leaves that can be used
/*long leaveDays = leaveRepository
    .findByEmployeeIdAndStatus(emp.getId(), "APPROVED")
    .stream()
    .filter(l -> !l.getStartDate().isAfter(end) && !l.getEndDate().isBefore(start))
    .mapToLong(l -> ChronoUnit.DAYS.between(
            l.getStartDate().isBefore(start) ? start : l.getStartDate(),
            l.getEndDate().isAfter(end) ? end : l.getEndDate()
        ) + 1)
    .sum();*/
    long leaveDays = leaveRepository
    .findByEmployeeIdAndStatus(emp.getId(), "APPROVED")
    .stream()
    .mapToLong(leave -> {

        LocalDate s = leave.getStartDate().isBefore(start) ? start : leave.getStartDate();
        LocalDate e = leave.getEndDate().isAfter(end) ? end : leave.getEndDate();

        long count = 0;
        LocalDate tempDate = s;

        while (!tempDate.isAfter(e)) {

            DayOfWeek day = tempDate.getDayOfWeek();

            boolean isWeekend = (day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY);
            boolean isHoliday = holidayRepository.existsByDate(tempDate);

            if (!isWeekend && !isHoliday) {
                count++;
            }

            tempDate = tempDate.plusDays(1);
        }

        return count;
    })
    .sum();

// total leaves taken in month
long totalLeavesTaken = absentDays + leaveDays;

// get starting balance
int startingBalance = emp.getMonthStartLeaveBalance();

// calculate unpaid days
long unpaidDays = totalLeavesTaken - startingBalance;

if (unpaidDays < 0) {
    unpaidDays = 0;
}

// salary calculation
//double perDaySalary = emp.getBaseSalary() / workingDays;
double perDaySalary = emp.getBaseSalary() / totalDays;
if (workingDays <= 0) {
    throw new RuntimeException("Invalid working days calculation");
}

double deduction = unpaidDays * perDaySalary;

double finalSalary = emp.getBaseSalary() - deduction;
        // Save salary record
        Salary salary = new Salary();
        salary.setEmployeeId(emp.getId());
        salary.setMonth(end.getMonthValue());
        salary.setYear(end.getYear());
        salary.setBaseSalary(emp.getBaseSalary());
        salary.setDeduction(deduction);
        salary.setFinalSalary(finalSalary);

        salaryRepository.save(salary);

        // Reset employee salary
       emp.setFinalSalary(emp.getBaseSalary());
        employeeRepository.save(emp);
    }

    System.out.println("✅ Monthly salary generated");
}
}