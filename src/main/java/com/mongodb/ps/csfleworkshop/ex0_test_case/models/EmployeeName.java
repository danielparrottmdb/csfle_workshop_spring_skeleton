package com.mongodb.ps.csfleworkshop.ex0_test_case.models;

// import org.springframework.data.mongodb.core.mapping.Field;

public class EmployeeName {

    private String firstName;
    private String lastName;

    // Spring Data does not write nulls to MongoDB by default
    // We can use this to force an encryption error on null
    // @Field(write=Field.Write.ALWAYS)
    private String otherNames;

    public EmployeeName() {
    }

    public EmployeeName(String firstName, String lastName) {
        this(firstName, lastName, null);
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

    public void setOtherNames(String otherName) {
        this.otherNames = otherName;
    }

}
