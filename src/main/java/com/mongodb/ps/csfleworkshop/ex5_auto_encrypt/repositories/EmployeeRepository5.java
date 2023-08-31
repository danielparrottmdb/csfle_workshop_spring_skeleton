package com.mongodb.ps.csfleworkshop.ex5_auto_encrypt.repositories;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.mongodb.ps.csfleworkshop.ex5_auto_encrypt.models.Employee;
import com.mongodb.ps.csfleworkshop.ex5_auto_encrypt.models.EmployeeName;

public interface EmployeeRepository5 extends MongoRepository<Employee, String> {
    List<Employee> findByName(EmployeeName name);
}
