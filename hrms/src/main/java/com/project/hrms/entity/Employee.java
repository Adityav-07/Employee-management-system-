package com.project.hrms.entity;

import jakarta.persistence.*;
//import lombok.*;
//import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
@Entity
//@Data
//@NoArgsConstructor
//@AllArgsConstructor
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    @Column(unique = true)
    private String email;
    private String role;
    private double baseSalary;
    private int leaveBalance;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
private String password;
    private double finalSalary;
    private int totalLeaveAllowed;
    private int monthStartLeaveBalance;
  //  public String getPassword() {
    //return password;
//}


/*public void setPassword(String password) {
    this.password = password;
}

public double getFinalSalary() {
    return finalSalary;
}

public void setFinalSalary(double finalSalary) {
    this.finalSalary = finalSalary;
}*/
// ===== GETTERS & SETTERS =====

public Long getId() { return id; }
public void setId(Long id) { this.id = id; }

public String getName() { return name; }
public void setName(String name) { this.name = name; }

public String getEmail() { return email; }
public void setEmail(String email) { this.email = email; }

public String getRole() { return role; }
public void setRole(String role) { this.role = role; }

public double getBaseSalary() { return baseSalary; }
public void setBaseSalary(double baseSalary) { this.baseSalary = baseSalary; }

public double getFinalSalary() { return finalSalary; }
public void setFinalSalary(double finalSalary) { this.finalSalary = finalSalary; }

public int getLeaveBalance() { return leaveBalance; }
public void setLeaveBalance(int leaveBalance) { this.leaveBalance = leaveBalance; }

public int getTotalLeaveAllowed() { return totalLeaveAllowed; }
public void setTotalLeaveAllowed(int totalLeaveAllowed) { this.totalLeaveAllowed = totalLeaveAllowed; }

public String getPassword() { return password; }
public void setPassword(String password) { this.password = password; }
public int getMonthStartLeaveBalance() {
    return monthStartLeaveBalance;
}

public void setMonthStartLeaveBalance(int monthStartLeaveBalance) {
    this.monthStartLeaveBalance = monthStartLeaveBalance;
}

}