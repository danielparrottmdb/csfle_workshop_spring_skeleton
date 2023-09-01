package com.mongodb.ps.csfleworkshop.ex4_manual_complete.models;

public class EmployeeNameX {

    private String firstName;
    private String lastName;
    private String otherNames;

    public EmployeeNameX() {
    }

    public EmployeeNameX(String firstName, String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public EmployeeNameX(String firstName, String lastName, String otherNames) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.otherNames = otherNames;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getOtherNames() {
        return otherNames;
    }

    public void setOtherNames(String otherNames) {
        this.otherNames = otherNames;
    }

    @Override
    public String toString() {
        return "EmployeeName [firstName=" + firstName + ", lastName=" + lastName + "]";
    }
}