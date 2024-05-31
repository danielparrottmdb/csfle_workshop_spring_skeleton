package com.mongodb.ps.csfleworkshop.ex7_auto_complete;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Locale;
import java.util.UUID;

import org.bson.BsonDocument;
import org.bson.types.ObjectId;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.data.repository.support.Repositories;

import com.github.javafaker.Faker;
import com.mongodb.ps.csfleworkshop.CsfleExercise;
import com.mongodb.ps.csfleworkshop.ex7_auto_complete.models.Employee;
import com.mongodb.ps.csfleworkshop.ex7_auto_complete.models.EmployeeAddress;
import com.mongodb.ps.csfleworkshop.ex7_auto_complete.models.EmployeeName;
import com.mongodb.ps.csfleworkshop.ex7_auto_complete.repositories.EmployeeRepository7;

public class AutoCompleteExercise implements CsfleExercise {
	protected static Logger log = LoggerFactory.getLogger(AutoCompleteExercise.class);

	protected ApplicationContext appContext;

	public AutoCompleteExercise(ApplicationContext applicationContext) {
		this.appContext = applicationContext;
	}

	public EmployeeRepository7 getEmployeeRepository(ApplicationContext appContext) {
		Repositories repos = new Repositories(appContext);
		EmployeeRepository7 repo = (EmployeeRepository7) repos.getRepositoryFor(Employee.class).get();
		return repo;
	}

	public boolean useAutoEncryption() {
		return true;
	}

	@Override
	public void runExercise() {
		// PUT CODE HERE TO RETRIEVE OUR COMMON (our first) DEK:
		Faker faker = new Faker(new Locale("en", "AU"));
		String firstName = faker.name().firstName();
		String lastName = faker.name().lastName();

		Employee e = new Employee(
				new EmployeeName(firstName, lastName),
				new EmployeeAddress(
						"2 Bson Street",
						"Mongoville",
						"3999",
						"Victoria",
						"Oz"),
				Arrays.asList("CIO"),
				LocalDate.of(1980, 10, 11),
				"1800MONGO",
				999999.99,
				"78SD20NN001");


		// Insert the employee doc
		EmployeeRepository7 employeeRepository = this.getEmployeeRepository(appContext);
		ObjectId eId = employeeRepository.insert(e).getId();
		log.info("eId: " + eId);

		// Find using the deterministically encrypted first and last names
		EmployeeName nameQuery = new EmployeeName(firstName, lastName);
		Employee e2 = employeeRepository.findByName(nameQuery).get(0);
		log.info("e2: " + e2);
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
                        "keyId" : [ UUID("%s") ],
				        "algorithm" : "AEAD_AES_256_CBC_HMAC_SHA_512-Random"
				    },
				    "properties" : {
				        "name" : {
				            "bsonType": "object",
				            "properties" : {
				                "firstName" : {
				                    "encrypt" : {
				                        "bsonType" : "string",
				                        "algorithm" : "AEAD_AES_256_CBC_HMAC_SHA_512-Deterministic",
				                    }
				                },
				                "lastName" : {
				                    "encrypt" : {
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
				        """.formatted(dekUuid);
		BsonDocument schemaBsonDoc = BsonDocument.parse(schemaJson);
		return schemaBsonDoc;
	}
}
