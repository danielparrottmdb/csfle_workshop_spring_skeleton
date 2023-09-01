package com.mongodb.ps.csfleworkshop.ex3_manual_complete;

import java.time.LocalDate;
import java.util.Arrays;
// import java.util.Date;
import java.util.UUID;

import org.bson.BsonDocument;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.data.repository.support.Repositories;

import com.mongodb.ps.csfleworkshop.CsfleExercise;
import com.mongodb.ps.csfleworkshop.ex3_manual_complete.models.Employee;
import com.mongodb.ps.csfleworkshop.ex3_manual_complete.models.EmployeeName;
import com.mongodb.ps.csfleworkshop.ex3_manual_complete.models.EmployeeAddress;
import com.mongodb.ps.csfleworkshop.ex3_manual_complete.repositories.EmployeeRepository3;
import com.mongodb.ps.csfleworkshop.services.KeyGenerationService;

public class ManualCompleteExercise implements CsfleExercise {

    protected ApplicationContext appContext;

	protected static Logger log = LoggerFactory.getLogger(ManualCompleteExercise.class);
    
    public ManualCompleteExercise(ApplicationContext applicationContext) {
        this.appContext = applicationContext;
    }

	public EmployeeRepository3 getEmployeeRepository(ApplicationContext appContext) {
		Repositories repos = new Repositories(appContext);
		EmployeeRepository3 repo = (EmployeeRepository3) repos.getRepositoryFor(Employee.class).get();
		return repo;
	}

	public KeyGenerationService getKeyGenerationService(ApplicationContext applicationContext) {
		return applicationContext.getBean(KeyGenerationService.class);
	}

    public void runExercise() {
		// PUT CODE HERE TO RETRIEVE OUR COMMON (our first) DEK:
        // NB - there is a bug in spring-data-mongodb date conversion @ 4.1 so use LocalDate
        //Date dob = new Date(1989, 1, 1);
        LocalDate dob = LocalDate.of(1989, 1, 1);
		Employee e = new Employee(
				new EmployeeName("Manish", "Engineer"),
				new EmployeeAddress(
						"537 Bson Rd",
						"Mongoville",
						"7258",
						"Tasmania",
						"Oz"),
				Arrays.asList("IC"),
				//ZonedDateTime.of(LocalDate.of(1989, 1, 1), LocalTime.of(0, 0, 0)),
				//ZonedDate.of(1989, 1, 1),
				dob,
				"1800MONGO",
				89000.0,
				"103-443-924");

		// Now make sure an encryption key for the employee exists
		KeyGenerationService keyGenerationService = this.getKeyGenerationService(appContext);
		// Get the DEK UUID for the employee (based on their _id)
		final UUID dataKey1 = keyGenerationService.generateKey();
		log.info("dataKey1: " + dataKey1);

		// Insert the employee doc
		EmployeeRepository3 employeeRepository = this.getEmployeeRepository(appContext);
		ObjectId eId = employeeRepository.insert(e).getId();
		log.info("eId: " + eId);

		// Find using the deterministically encrypted first and last names
		// Employee e2 = employeeRepository.findById(eId.toString()).get();
		EmployeeName nameQuery = new EmployeeName("Manish", "Engineer");
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

