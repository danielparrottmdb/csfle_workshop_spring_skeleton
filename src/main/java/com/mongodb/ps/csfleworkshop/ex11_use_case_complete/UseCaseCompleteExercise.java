package com.mongodb.ps.csfleworkshop.ex11_use_case_complete;

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
import com.mongodb.ps.csfleworkshop.ex11_use_case_complete.models.Employee;
import com.mongodb.ps.csfleworkshop.ex11_use_case_complete.models.EmployeeAddress;
import com.mongodb.ps.csfleworkshop.ex11_use_case_complete.models.EmployeeName;
import com.mongodb.ps.csfleworkshop.ex11_use_case_complete.repositories.EmployeeRepository11;
import com.mongodb.ps.csfleworkshop.services.KeyGenerationService;

public class UseCaseCompleteExercise implements CsfleExercise {
	protected static Logger log = LoggerFactory.getLogger(UseCaseCompleteExercise.class);

	protected ApplicationContext appContext;

	public UseCaseCompleteExercise(ApplicationContext applicationContext) {
		this.appContext = applicationContext;
	}

	public EmployeeRepository11 getEmployeeRepository(ApplicationContext appContext) {
		Repositories repos = new Repositories(appContext);
		EmployeeRepository11 repo = (EmployeeRepository11) repos.getRepositoryFor(Employee.class).get();
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
						"537 White Hills Rd",
						"Evandale",
						"7258",
						"Tasmania",
						"Oz"),
				Arrays.asList("IC"),
				LocalDate.of(1989, 1, 1),
				"+61 400 000 111",
				78000.0,
				"Shh it's a secret");

		// Now make sure an encryption key for the employee exists
		KeyGenerationService keyGenerationService = this.getKeyGenerationService(appContext);
		// Get the DEK UUID for the employee (based on their _id)
		final UUID employeeDEKId = keyGenerationService.generateKey(employeeId);
		log.info("employeeDEKId: " + employeeDEKId);

		// Insert the employee doc
		EmployeeRepository11 employeeRepository = this.getEmployeeRepository(appContext);
		String eId = employeeRepository.insert(e).getId();
		log.info("eId: " + eId);

		// Find using the deterministically encrypted first and last names
		// Employee e2 = employeeRepository.findById(eId.toString()).get();
		EmployeeName nameQuery = new EmployeeName(firstName, lastName);
		Employee e2 = employeeRepository.findByName(nameQuery).get(0);
		log.info("e2: " + e2 + ";" + e2.getSalary());

		// Delete the key and find again - will it work?
		keyGenerationService.deleteKey(employeeId);
		e2 = employeeRepository.findByName(nameQuery).get(0);
		log.info("e2: " + e2.getName() + ";" + e2.getSalary());

		// Now sleep for 60 seconds and find again - will it work this time?
		try {
			log.info("Sleeping for 60s.....");
			Thread.sleep(60 * 1000);
		} catch (InterruptedException ie) {
			log.error("Interrupted while sleeping", ie);
		}
		log.info("Awake!");

		try {
			List<Employee> emps = employeeRepository.findByName(nameQuery);
			if (emps.size() == 0) {
				log.info("No employee found!");
			} else {
				log.info("e2: " + e2.getName() + ";" + e2.getSalary());
			}
		} catch (PermissionDeniedDataAccessException pddae) {
			log.error("PDDAE - Error retrieving employee after key deletion (as expected!):", pddae);
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
