package com.mongodb.ps.csfleworkshop.ex1_manual_encrypt.models;

import org.springframework.data.mongodb.core.EncryptionAlgorithms;
import org.springframework.data.mongodb.core.mapping.ExplicitEncrypted;
import org.springframework.data.mongodb.core.mapping.Field;

//import com.mongodb.client.model.Field;

// import org.springframework.data.mongodb.core.mapping.Field;

public class EmployeeName {

    // TODO - Use the ExplicitEncrypted annotation to flag fields for manual
    // encryption.  Make sure the key and algorithm are correct!
    private String firstName;
    private String lastName;

    // TODO - what is the effect of this?
    // TODO:SDE - force a null value to be written in order to fail encryption
    @Field(write=Field.Write.ALWAYS)
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