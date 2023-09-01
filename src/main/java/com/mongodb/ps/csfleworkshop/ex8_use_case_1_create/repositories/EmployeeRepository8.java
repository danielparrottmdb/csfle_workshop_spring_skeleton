package com.mongodb.ps.csfleworkshop.ex8_use_case_1_create.repositories;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.mongodb.ps.csfleworkshop.ex8_use_case_1_create.models.Employee;
import com.mongodb.ps.csfleworkshop.ex8_use_case_1_create.models.EmployeeName;

public interface EmployeeRepository8 extends MongoRepository<Employee, String> {
    List<Employee> findByName(EmployeeName name);
}
