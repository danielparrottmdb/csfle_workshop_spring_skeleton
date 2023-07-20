package com.mongodb.ps.csfleworkshop.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import com.mongodb.ps.csfleworkshop.models.Employee;

public interface EmployeeRepository extends MongoRepository<Employee, String>{
    
}
