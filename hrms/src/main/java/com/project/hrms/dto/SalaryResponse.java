package com.project.hrms.dto;

public class SalaryResponse {

    private String employeeName;
    private String role;

    private double baseSalary;
    private double finalSalary;

    private int leaveBalance;

    private long absentDays;
    private long approvedLeaveDays;
    private long unpaidLeaveDays;

    private double deduction;

    public SalaryResponse(
            String employeeName,
            String role,
            double baseSalary,
            double finalSalary,
            int leaveBalance,
            long absentDays,
            long approvedLeaveDays,
            long unpaidLeaveDays,
            double deduction
    ) {
        this.employeeName = employeeName;
        this.role = role;
        this.baseSalary = baseSalary;
        this.finalSalary = finalSalary;
        this.leaveBalance = leaveBalance;
        this.absentDays = absentDays;
        this.approvedLeaveDays = approvedLeaveDays;
        this.unpaidLeaveDays = unpaidLeaveDays;
        this.deduction = deduction;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public String getRole() {
        return role;
    }

    public double getBaseSalary() {
        return baseSalary;
    }

    public double getFinalSalary() {
        return finalSalary;
    }

    public int getLeaveBalance() {
        return leaveBalance;
    }

    public long getAbsentDays() {
        return absentDays;
    }

    public long getApprovedLeaveDays() {
        return approvedLeaveDays;
    }

    public long getUnpaidLeaveDays() {
        return unpaidLeaveDays;
    }

    public double getDeduction() {
        return deduction;
    }
}