package com.mongodb.ps.csfleworkshop.ex4_manual_complete;

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
import com.mongodb.ps.csfleworkshop.ex4_manual_complete.models.Employee;
import com.mongodb.ps.csfleworkshop.ex4_manual_complete.models.EmployeeName;
import com.mongodb.ps.csfleworkshop.ex4_manual_complete.models.EmployeeNameX;
import com.mongodb.ps.csfleworkshop.ex4_manual_complete.models.EmployeeX;
import com.mongodb.ps.csfleworkshop.ex4_manual_complete.models.EmployeeAddress;
import com.mongodb.ps.csfleworkshop.ex4_manual_complete.repositories.EmployeeRepository4;
import com.mongodb.ps.csfleworkshop.ex4_manual_complete.repositories.EmployeeRepository4X;
import com.mongodb.ps.csfleworkshop.services.KeyGenerationService;

public class ManualEncryptAutoDecryptExercise implements CsfleExercise {

    protected ApplicationContext appContext;

	protected static Logger log = LoggerFactory.getLogger(ManualEncryptAutoDecryptExercise.class);
    
    public ManualEncryptAutoDecryptExercise(ApplicationContext applicationContext) {
        this.appContext = applicationContext;
    }

	public EmployeeRepository4 getEmployeeRepository(ApplicationContext appContext) {
		Repositories repos = new Repositories(appContext);
		EmployeeRepository4 repo = (EmployeeRepository4) repos.getRepositoryFor(Employee.class).get();
		return repo;
	}

	public EmployeeRepository4X getEmployeeXRepository(ApplicationContext appContext) {
		Repositories repos = new Repositories(appContext);
		EmployeeRepository4X repo = (EmployeeRepository4X) repos.getRepositoryFor(EmployeeX.class).get();
		return repo;
	}
	public KeyGenerationService getKeyGenerationService(ApplicationContext applicationContext) {
		return applicationContext.getBean(KeyGenerationService.class);
	}

    public void runExercise() {
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

		// Insert the employee doc
		EmployeeRepository4 employeeRepository = this.getEmployeeRepository(appContext);
		ObjectId eId = employeeRepository.insert(e).getId();
		log.info("eId: " + eId);

		// Find using the deterministically encrypted first and last names
		EmployeeRepository4X employeeRepositoryX = this.getEmployeeXRepository(appContext);
		EmployeeNameX nameQuery = new EmployeeNameX("Manish", "Engineer");
		EmployeeX e2 = employeeRepositoryX.findByName(nameQuery).get(0);
		log.info("e2: " + e2);

    }
    public BsonDocument getSchemaDocument(UUID dekUuid) {
        return null;
    }

    public boolean useAutoEncryption() {
        return false;
    }
}

