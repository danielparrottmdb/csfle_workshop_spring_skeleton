package com.mongodb.ps.csfleworkshop.ex4_manual_encrypt_auto_decrypt.repositories;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.mongodb.ps.csfleworkshop.ex4_manual_encrypt_auto_decrypt.models.EmployeeM;
import com.mongodb.ps.csfleworkshop.ex4_manual_encrypt_auto_decrypt.models.EmployeeName;
import com.mongodb.ps.csfleworkshop.ex4_manual_encrypt_auto_decrypt.models.EmployeeNameM;

public interface EmployeeRepository4M extends MongoRepository<EmployeeM, String> {
    List<EmployeeM> findByName(EmployeeNameM name);
    List<EmployeeM> findByName(EmployeeName name);
}