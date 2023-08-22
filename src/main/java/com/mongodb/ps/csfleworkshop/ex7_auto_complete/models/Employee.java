package com.mongodb.ps.csfleworkshop.ex7_auto_complete.models;

import java.time.LocalDate;
import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document("employees")
public class Employee {

    @Id
    private ObjectId id;

    private EmployeeName name;

    private EmployeeAddress address;

    private List<String> role;

    private LocalDate dob;
    private String phoneNumber;
    private double salary;

    @Indexed(unique = true)
    @Field
    private String taxIdentifier;

    public Employee() {
    }

    public Employee(EmployeeName name, EmployeeAddress address, List<String> role, LocalDate dob,
            String phoneNumber, double salary, String taxIdentifier) {
        this.name = name;
        this.address = address;
        this.role = role;
        this.dob = dob;
        this.phoneNumber = phoneNumber;
        this.salary = salary;
        this.taxIdentifier = taxIdentifier;
    }

    public EmployeeName getName() {
        return name;
    }

    public void setName(EmployeeName name) {
        this.name = name;
    }

    public EmployeeAddress getAddress() {
        return address;
    }

    public void setAddress(EmployeeAddress address) {
        this.address = address;
    }

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public LocalDate getDob() {
        return dob;
    }

    public void setDob(LocalDate dob) {
        this.dob = dob;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getTaxIdentifier() {
        return taxIdentifier;
    }

    public void setTaxIdentifier(String taxIdentifier) {
        this.taxIdentifier = taxIdentifier;
    }

    public List<String> getRole() {
        return role;
    }

    public void setRole(List<String> role) {
        this.role = role;
    }

    public double getSalary() {
        return salary;
    }

    public void setSalary(double salary) {
        this.salary = salary;
    }

    @Override
    public String toString() {
        return "Employee [id=" + id + ", name=" + name + ", address=" + address + ", role=" + role + ", dob=" + dob
                + ", phoneNumber=" + phoneNumber + ", salary=" + salary + ", taxIdentifier=" + taxIdentifier + "]";
    }

}
