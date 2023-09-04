package com.mongodb.ps.csfleworkshop.ex10_use_case_delete;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.UUID;

import org.bson.BsonDocument;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.dao.PermissionDeniedDataAccessException;
import org.springframework.data.repository.support.Repositories;

import com.github.javafaker.Faker;
import com.mongodb.ps.csfleworkshop.CsfleExercise;
import com.mongodb.ps.csfleworkshop.ex10_use_case_delete.models.Employee;
import com.mongodb.ps.csfleworkshop.ex10_use_case_delete.models.EmployeeAddress;
import com.mongodb.ps.csfleworkshop.ex10_use_case_delete.models.EmployeeName;
import com.mongodb.ps.csfleworkshop.ex10_use_case_delete.repositories.EmployeeRepository10;
import com.mongodb.ps.csfleworkshop.services.KeyGenerationService;

public class UseCaseDeleteExercise implements CsfleExercise {
	protected static Logger log = LoggerFactory.getLogger(UseCaseDeleteExercise.class);

	protected ApplicationContext appContext;

	public UseCaseDeleteExercise(ApplicationContext applicationContext) {
		this.appContext = applicationContext;
	}

	public EmployeeRepository10 getEmployeeRepository(ApplicationContext appContext) {
		Repositories repos = new Repositories(appContext);
		EmployeeRepository10 repo = (EmployeeRepository10) repos.getRepositoryFor(Employee.class).get();
		return repo;
	}

	public KeyGenerationService getKeyGenerationService(ApplicationContext applicationContext) {
		return applicationContext.getBean(KeyGenerationService.class);
	}

	public boolean useAutoEncryption() {
		return true;
	}

	//public void runExercise(ApplicationContext appContext) {
	@Override
	public void runExercise() {
		// PUT CODE HERE TO RETRIEVE OUR COMMON (our first) DEK:
		Random random = new Random();
		String employeeId = Integer.toString(10000 + random.nextInt(90000));
		Faker faker = new Faker(new Locale("en", "AU"));
		String firstName = faker.name().firstName();
		String lastName = faker.name().lastName();

		Employee e = new Employee(
				employeeId,
				new EmployeeName(firstName, lastName),
				new EmployeeAddress(
						"3 Bson Street",
						"Mongoville",
						"3999",
						"Victoria",
						"Oz"),
				Arrays.asList("CIO"),
				LocalDate.of(1978, 10, 10),
				"1800MONGO",
				999999.99,
				"78SD20NN01");

		// Now make sure an encryption key for the employee exists
		KeyGenerationService keyGenerationService = this.getKeyGenerationService(appContext);
		// Get the DEK UUID for the employee (based on their _id)
		final UUID employeeDEKId = keyGenerationService.generateKey(employeeId);
		log.info("employeeDEKId: " + employeeDEKId);

		// Insert the employee doc
		EmployeeRepository10 employeeRepository = this.getEmployeeRepository(appContext);
		String eId = employeeRepository.insert(e).getId();
		log.info("eId: " + eId);

		// Find using the deterministically encrypted first and last names
		// Employee e2 = employeeRepository.findById(eId.toString()).get();
		EmployeeName nameQuery = new EmployeeName(firstName, lastName);
		Employee e2 = employeeRepository.findByName(nameQuery).get(0);
		log.info("e2: " + e2);

		//  TODO - PUT CODE HERE TO DELETE THE KEY AND FIND AGAIN
		Employee e3 = null;
		log.info("e3: " + e3.getName());

		//  TODO - PUT CODE HERE TO SLEEP FOR 60s AND FIND AGAIN

		try {
			List<Employee> emps = employeeRepository.findByName(nameQuery);
			if (emps.size() == 0) {
				log.info("No employee found!");
			} else {
				log.info("emps: " + emps);
			}
		} catch (PermissionDeniedDataAccessException pddae) {
			log.error("PDDAE - Error retrieving employee after key deletion!:", pddae);
		}
	}

	/**
	 * Get the schema map for this exercise, noting the employee DEK for
	 * encrypted fields other than first and last name.
	 * 
	 * @param dekUuid The UUID of the data encryption key used for deterministic
	 *                encryption of searchable fields.
	 * @return A representaion of the encryption schema as a BsonDocument.
	 */
	@Override
	public BsonDocument getSchemaDocument(UUID dekUuid) {
		String schemaJson = """
				{
				    "bsonType" : "object",
				    "encryptMetadata" : {
				        "keyId": "/_id",
				        "algorithm" : "AEAD_AES_256_CBC_HMAC_SHA_512-Random"
				    },
				    "properties" : {
				        "name" : {
				            "bsonType": "object",
				            "properties" : {
				                "firstName" : {
				                    "encrypt" : {
				                        "keyId" : [ UUID("%s") ],
				                        "bsonType" : "string",
				                        "algorithm" : "AEAD_AES_256_CBC_HMAC_SHA_512-Deterministic",
				                    }
				                },
				                "lastName" : {
				                    "encrypt" : {
				                        "keyId" : [ UUID("%s") ],
				                        "bsonType" : "string",
				                        "algorithm" : "AEAD_AES_256_CBC_HMAC_SHA_512-Deterministic",
				                }
				                },
				                "otherNames" : {
				                    "encrypt" : {
				                        "bsonType" : "string",
				                    }
				                }
				            }
				        },
				        "address" : {
				            "encrypt" : {
				                "bsonType" : "object"
				            }
				        },
				        "dob" : {
				            "encrypt" : {
				                "bsonType" : "date"
				            }
				        },
				        "phoneNumber" : {
				            "encrypt" : {
				                "bsonType" : "string"
				            }
				        },
				        "salary" : {
				            "encrypt" : {
				                "bsonType" : "double"
				            }
				        },
				        "taxIdentifier" : {
				            "encrypt" : {
				                "bsonType" : "string"
				            }
				        }
				    }
				}
				        """.formatted(dekUuid, dekUuid);
		BsonDocument schemaBsonDoc = BsonDocument.parse(schemaJson);
		return schemaBsonDoc;
	}
}
