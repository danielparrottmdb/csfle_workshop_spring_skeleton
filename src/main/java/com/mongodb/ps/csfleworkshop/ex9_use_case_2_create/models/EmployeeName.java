package com.mongodb.ps.csfleworkshop.ex9_use_case_2_create.models;

// import org.springframework.data.mongodb.core.mapping.Field;

public class EmployeeName {

    private String firstName;
    private String lastName;
    // @Field(write=Field.Write.ALWAYS)
    private String otherNames;

    public EmployeeName() {
    }

    public EmployeeName(String firstName, String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public EmployeeName(String firstName, String lastName, String otherNames) {
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