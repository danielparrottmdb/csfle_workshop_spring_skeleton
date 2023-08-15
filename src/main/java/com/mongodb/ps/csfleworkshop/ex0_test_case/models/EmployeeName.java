package com.mongodb.ps.csfleworkshop.ex0_test_case.models;

public class EmployeeName {

    private String firstName;
    private String lastName;

    // @JsonInclude(Include.NON_NULL)
    // private String otherName;

    public EmployeeName() {
    }

    public EmployeeName(String firstName, String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
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
}
