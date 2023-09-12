package com.mongodb.ps.csfleworkshop.ex4_manual_complete.models;

import org.bson.BsonBinary;

public class EmployeeNameM {

    private BsonBinary firstName;
    private BsonBinary lastName;
    private BsonBinary otherNames;

    public EmployeeNameM() {
    }

    public EmployeeNameM(BsonBinary firstName, BsonBinary lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public EmployeeNameM(BsonBinary firstName, BsonBinary lastName, BsonBinary otherNames) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.otherNames = otherNames;
    }

    public BsonBinary getFirstName() {
        return firstName;
    }

    public void setFirstName(BsonBinary firstName) {
        this.firstName = firstName;
    }

    public BsonBinary getLastName() {
        return lastName;
    }

    public void setLastName(BsonBinary lastName) {
        this.lastName = lastName;
    }

    public BsonBinary getOtherNames() {
        return otherNames;
    }

    public void setOtherNames(BsonBinary otherNames) {
        this.otherNames = otherNames;
    }

    @Override
    public String toString() {
        return "EmployeeName [firstName=" + firstName + ", lastName=" + lastName + "]";
    }
}