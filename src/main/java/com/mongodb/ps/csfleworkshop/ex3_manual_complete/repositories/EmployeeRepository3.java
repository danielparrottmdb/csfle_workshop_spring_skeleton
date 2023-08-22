package com.mongodb.ps.csfleworkshop.ex3_manual_complete.repositories;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.mongodb.ps.csfleworkshop.ex3_manual_complete.models.Employee;
import com.mongodb.ps.csfleworkshop.ex3_manual_complete.models.EmployeeName;

public interface EmployeeRepository3 extends MongoRepository<Employee, String> {
    List<Employee> findByName(EmployeeName name);
}
