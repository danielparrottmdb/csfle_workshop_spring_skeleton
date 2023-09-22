package com.mongodb.ps.csfleworkshop.ex4_manual_encrypt_auto_decrypt.repositories;

import java.util.List;

import org.bson.BsonBinary;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import com.mongodb.ps.csfleworkshop.ex4_manual_encrypt_auto_decrypt.models.Employee;
import com.mongodb.ps.csfleworkshop.ex4_manual_encrypt_auto_decrypt.models.EmployeeName;

public interface EmployeeRepository4 extends MongoRepository<Employee, String> {
    List<Employee> findByName(EmployeeName name);

    @Query("{ 'name.firstName' : ?0, 'name.lastName': ?1 }")
    public List<Employee> findEncryptedNames(BsonBinary encryptedFirstname, BsonBinary encryptedLastname);

}
