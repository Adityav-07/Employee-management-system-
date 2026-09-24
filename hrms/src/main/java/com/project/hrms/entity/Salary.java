package com.project.hrms.entity;

import jakarta.persistence.*;

@Entity
public class Salary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long employeeId;
    private int month;
    private int year;

    private double baseSalary;
    private double deduction;
    private double finalSalary;

    public Long getId() { return id; }

    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }

    public int getMonth() { return month; }
    public void setMonth(int month) { this.month = month; }

    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }

    public double getBaseSalary() { return baseSalary; }
    public void setBaseSalary(double baseSalary) { this.baseSalary = baseSalary; }

    public double getDeduction() { return deduction; }
    public void setDeduction(double deduction) { this.deduction = deduction; }

    public double getFinalSalary() { return finalSalary; }
    public void setFinalSalary(double finalSalary) { this.finalSalary = finalSalary; }
}