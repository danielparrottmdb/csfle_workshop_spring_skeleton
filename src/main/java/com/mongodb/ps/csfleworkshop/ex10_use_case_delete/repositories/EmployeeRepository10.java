package com.mongodb.ps.csfleworkshop.ex10_use_case_delete.repositories;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.mongodb.ps.csfleworkshop.ex10_use_case_delete.models.Employee;
import com.mongodb.ps.csfleworkshop.ex10_use_case_delete.models.EmployeeName;

public interface EmployeeRepository10 extends MongoRepository<Employee, String> {
    List<Employee> findByName(EmployeeName name);
}
