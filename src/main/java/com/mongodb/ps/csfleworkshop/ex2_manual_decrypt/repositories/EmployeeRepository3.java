package com.mongodb.ps.csfleworkshop.ex2_manual_decrypt.repositories;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.mongodb.ps.csfleworkshop.ex2_manual_decrypt.models.Employee;
import com.mongodb.ps.csfleworkshop.ex2_manual_decrypt.models.EmployeeName;

public interface EmployeeRepository3 extends MongoRepository<Employee, String> {
    List<Employee> findByName(EmployeeName name);
}
