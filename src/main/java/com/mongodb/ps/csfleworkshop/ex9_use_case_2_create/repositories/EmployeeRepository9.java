package com.mongodb.ps.csfleworkshop.ex9_use_case_2_create.repositories;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.mongodb.ps.csfleworkshop.ex9_use_case_2_create.models.Employee;
import com.mongodb.ps.csfleworkshop.ex9_use_case_2_create.models.EmployeeName;

public interface EmployeeRepository9 extends MongoRepository<Employee, String> {
    List<Employee> findByName(EmployeeName name);
}
