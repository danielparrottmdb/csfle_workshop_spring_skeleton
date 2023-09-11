package com.mongodb.ps.csfleworkshop.ex4_manual_complete.repositories;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.mongodb.ps.csfleworkshop.ex4_manual_complete.models.EmployeeM;
import com.mongodb.ps.csfleworkshop.ex4_manual_complete.models.EmployeeName;
import com.mongodb.ps.csfleworkshop.ex4_manual_complete.models.EmployeeNameM;

public interface EmployeeRepository4M extends MongoRepository<EmployeeM, String> {
    List<EmployeeM> findByName(EmployeeNameM name);
    List<EmployeeM> findByName(EmployeeName name);
}