package com.mongodb.ps.csfleworkshop.ex1_manual_encrypt.repositories;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.mongodb.ps.csfleworkshop.ex1_manual_encrypt.models.Employee;
import com.mongodb.ps.csfleworkshop.ex1_manual_encrypt.models.EmployeeName;

public interface EmployeeRepository1 extends MongoRepository<Employee, String> {
    List<Employee> findByName(EmployeeName name);
}
