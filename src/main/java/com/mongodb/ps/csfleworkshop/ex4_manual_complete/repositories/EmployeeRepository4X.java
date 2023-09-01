package com.mongodb.ps.csfleworkshop.ex4_manual_complete.repositories;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.mongodb.ps.csfleworkshop.ex4_manual_complete.models.EmployeeX;
import com.mongodb.ps.csfleworkshop.ex4_manual_complete.models.EmployeeName;
import com.mongodb.ps.csfleworkshop.ex4_manual_complete.models.EmployeeNameX;

public interface EmployeeRepository4X extends MongoRepository<EmployeeX, String> {
    List<EmployeeX> findByName(EmployeeNameX name);
    List<EmployeeX> findByName(EmployeeName name);
}