package com.mongodb.ps.csfleworkshop.ex7_use_case_complete;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Locale;
import java.util.Random;
import java.util.UUID;

import org.bson.BsonDocument;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.data.repository.support.Repositories;

import com.github.javafaker.Faker;
import com.mongodb.ps.csfleworkshop.CsfleExercise;
import com.mongodb.ps.csfleworkshop.ex7_use_case_complete.models.Employee;
import com.mongodb.ps.csfleworkshop.ex7_use_case_complete.models.EmployeeAddress;
import com.mongodb.ps.csfleworkshop.ex7_use_case_complete.models.EmployeeName;
import com.mongodb.ps.csfleworkshop.ex7_use_case_complete.repositories.EmployeeRepository7;
import com.mongodb.ps.csfleworkshop.services.KeyGenerationService;

public class UseCaseCompleteExercise implements CsfleExercise {
	protected static Logger log = LoggerFactory.getLogger(UseCaseCompleteExercise.class);

	public EmployeeRepository7 getEmployeeRepository(ApplicationContext appContext) {
		Repositories repos = new Repositories(appContext);
		EmployeeRepository7 repo = (EmployeeRepository7) repos.getRepositoryFor(Employee.class).get();
		return repo;
	}

	public KeyGenerationService getKeyGenerationService(ApplicationContext applicationContext) {
		KeyGenerationService kgs = applicationContext.getBean(KeyGenerationService.class); 
		return kgs;
	}

	public String getConnectionString(ApplicationContext applicationContext) {
		return applicationContext.getEnvironment().getProperty("spring.data.mongodb.uri");
	}

	public String getKeyVaultNamespace(ApplicationContext applicationContext) {

		Environment environment = applicationContext.getEnvironment();
		String kvDatabase = environment.getProperty("spring.data.mongodb.keyvault.database");
		String kvCollection = environment.getProperty("spring.data.mongodb.keyvault.collection");
		return  kvDatabase + "." +  kvCollection;
	}

    @Override
    public void runExercise(ApplicationContext appContext) {
		// PUT CODE HERE TO RETRIEVE OUR COMMON (our first) DEK:
		// Get the DEK UUID for the employee (based on their _id)
		// Get the employee doc with the right _id
		// Get the schema map, noting the employee DEK for encrypted fields other than first and last name
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
				LocalDate.of(1989,1,1),
				"+61 400 000 111",
				78000.0,
				"Shh it's a secret"
		);

		// Now make sure an encryption key for the employee exists
		KeyGenerationService keyGenerationService = this.getKeyGenerationService(appContext);
        // This key is unused locally but ensures the employee DEK exists 
        final UUID employeeDEKId = keyGenerationService.generateKey(employeeId);
		log.info("employeeDEKId: " + employeeDEKId);

		// Insert the employee doc
		EmployeeRepository7 employeeRepository = this.getEmployeeRepository(appContext);
		String eId = employeeRepository.insert(e).getId();
		log.info("eId: " + eId);

		// Find using the deterministically encrypted first and last names
		// Employee e2 = employeeRepository.findById(eId.toString()).get();
		EmployeeName nameQuery = new EmployeeName(firstName, lastName);
		Employee e2 = employeeRepository.findByName(nameQuery).get(0);
		log.info("e2: " + e2 + ";" + e2.getSalary());

		// Delete the key and find again - will it work?

		// Now sleep for 60 seconds and find again - will it work this time?
    }

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
