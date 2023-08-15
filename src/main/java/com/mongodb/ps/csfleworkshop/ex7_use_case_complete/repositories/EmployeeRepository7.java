package com.mongodb.ps.csfleworkshop.ex7_use_case_complete.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.mongodb.ps.csfleworkshop.ex7_use_case_complete.models.Employee;

public interface EmployeeRepository7 extends MongoRepository<Employee, String>{
    
}
