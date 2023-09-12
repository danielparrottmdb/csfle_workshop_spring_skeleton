package com.mongodb.ps.csfleworkshop.ex4_manual_encrypt_auto_decrypt.repositories;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.mongodb.ps.csfleworkshop.ex4_manual_encrypt_auto_decrypt.models.Employee;
import com.mongodb.ps.csfleworkshop.ex4_manual_encrypt_auto_decrypt.models.EmployeeName;

public interface EmployeeRepository4 extends MongoRepository<Employee, String> {
    List<Employee> findByName(EmployeeName name);
}
