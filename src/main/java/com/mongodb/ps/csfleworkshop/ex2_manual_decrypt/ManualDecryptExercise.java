package com.mongodb.ps.csfleworkshop.ex2_manual_decrypt;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.UUID;

import org.bson.BsonDocument;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.data.repository.support.Repositories;

import com.mongodb.ps.csfleworkshop.CsfleExercise;
import com.mongodb.ps.csfleworkshop.ex2_manual_decrypt.models.Employee;
import com.mongodb.ps.csfleworkshop.ex2_manual_decrypt.models.EmployeeName;
import com.mongodb.ps.csfleworkshop.ex2_manual_decrypt.models.EmployeeAddress;
import com.mongodb.ps.csfleworkshop.ex2_manual_decrypt.repositories.EmployeeRepository2;
import com.mongodb.ps.csfleworkshop.services.KeyGenerationService;

public class ManualDecryptExercise implements CsfleExercise {

    protected ApplicationContext appContext;

	protected static Logger log = LoggerFactory.getLogger(ManualDecryptExercise.class);
    
    public ManualDecryptExercise(ApplicationContext applicationContext) {
        this.appContext = applicationContext;
    }

	public EmployeeRepository2 getEmployeeRepository(ApplicationContext appContext) {
		Repositories repos = new Repositories(appContext);
		EmployeeRepository2 repo = (EmployeeRepository2) repos.getRepositoryFor(Employee.class).get();
		return repo;
	}

	public KeyGenerationService getKeyGenerationService(ApplicationContext applicationContext) {
		return applicationContext.getBean(KeyGenerationService.class);
	}

    public void runExercise() {
		// PUT CODE HERE TO RETRIEVE OUR COMMON (our first) DEK:
        // NB - there is a bug in spring-data-mongodb date conversion @ 4.1 so use LocalDate
        // Date dob = new Date(1989, 1, 1);
        LocalDate dob = LocalDate.of(1981, 11, 11);
		Employee e = new Employee(
				new EmployeeName("Kuber", "Engineer"),
				new EmployeeAddress(
						"12 Bson Rd",
						"Mongoville",
						"3999",
						"Victoria",
						"Oz"),
				Arrays.asList("DEV"),
				dob,
				"1800MONGO",
				999999.99,
				"78SDSSNN001");

		// Now make sure an encryption key for the employee exists
		KeyGenerationService keyGenerationService = this.getKeyGenerationService(appContext);
		// Get the DEK UUID for the employee (based on their _id)
		final UUID dataKey1 = keyGenerationService.generateKey();
		log.info("dataKey1: " + dataKey1);

		// Insert the employee doc
		EmployeeRepository2 employeeRepository = this.getEmployeeRepository(appContext);
		ObjectId eId = employeeRepository.insert(e).getId();
		log.info("eId: " + eId);

		// TODO - search for the encrypted doc using deterministic fields
		// Find using the deterministically encrypted first and last names
		EmployeeName nameQuery = new EmployeeName();
		Employee e2 = employeeRepository.findByName(nameQuery).get(0);
		log.info("e2: " + e2);

    }
    public BsonDocument getSchemaDocument(UUID dekUuid) {
        return null;
    }

    public boolean useAutoEncryption() {
        return false;
    }
}

