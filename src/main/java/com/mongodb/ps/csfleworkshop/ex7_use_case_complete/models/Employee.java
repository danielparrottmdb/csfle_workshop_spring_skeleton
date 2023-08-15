package com.mongodb.ps.csfleworkshop.ex7_use_case_complete.models;

import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.EncryptionAlgorithms;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.ExplicitEncrypted;
import org.springframework.data.mongodb.core.mapping.Field;

@Document("employees")
public class Employee {

    @Id
    private ObjectId id;

    //private String firstName;

    //private String lastName;

    private EmployeeName name;

    private List<String> role;

    @Indexed(unique = true)
    @Field
    private String taxIdentifier;

    // This is set up for manual, random encryption
    @Field
    @ExplicitEncrypted(algorithm = EncryptionAlgorithms.AEAD_AES_256_CBC_HMAC_SHA_512_Random, keyAltName = "second-data-key")
    private double salary;

    public Employee() {
    }

    public Employee(EmployeeName name, String taxIdentifier, List<String> role, double salary) {
        this.name = name;
        this.role = role;
        this.taxIdentifier = taxIdentifier;
        this.salary = salary;
    }

    public EmployeeName getName() {
        return name;
    }

    public void setName(EmployeeName name) {
        this.name = name;
    }

    /*
    public Employee(String firstName, String lastName, String taxIdentifier, List<String> role) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.taxIdentifier = taxIdentifier;
        this.role = role;
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
     */


    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
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

}
