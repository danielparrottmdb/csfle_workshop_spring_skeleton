package com.mongodb.ps.csfleworkshop.ex0_test_case;

import java.util.Arrays;
import java.util.UUID;

import org.bson.BsonDocument;
import org.bson.types.ObjectId;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.data.repository.support.Repositories;

import com.mongodb.ps.csfleworkshop.CsfleExercise;
import com.mongodb.ps.csfleworkshop.ex0_test_case.models.Employee;
import com.mongodb.ps.csfleworkshop.ex0_test_case.models.EmployeeName;
import com.mongodb.ps.csfleworkshop.ex0_test_case.repositories.EmployeeRepository0;

public class TestCaseExercise implements CsfleExercise {
	protected static Logger log = LoggerFactory.getLogger(TestCaseExercise.class);

	public EmployeeRepository0 getEmployeeRepository(ApplicationContext appContext) {
		Repositories repos = new Repositories(appContext);
		EmployeeRepository0 repo = (EmployeeRepository0) repos.getRepositoryFor(Employee.class).get();
		return repo;
	}

    @Override
    public void runExercise(ApplicationContext appContext) {
		// PUT CODE HERE TO RETRIEVE OUR COMMON (our first) DEK:
		// Get the DEK UUID for the employee (based on their _id)
		// Get the employee doc with the right _id
		// Get the schema map, noting the employee DEK for encrypted fields other than first and last name


		Employee e = new Employee(new EmployeeName("Bugs", "Bunny"), "Shh it's a secret",
			Arrays.asList("IC"), 78000.0);
		EmployeeRepository0 employeeRepository = this.getEmployeeRepository(appContext);
		ObjectId eId = employeeRepository.insert(e).getId();
		log.info("eId: " + eId);
		Employee e2 = employeeRepository.findById(eId.toString()).get();
		log.info("e2: " + e2 + ";" + e2.getSalary());
    }

    @Override
    public BsonDocument getSchemaDocument(UUID dekUuid) {
        String schemaJson = """
{
    "bsonType" : "object",
    "encryptMetadata" : {
        "keyId" : [
        UUID("%s") 
        ],
        "algorithm" : "AEAD_AES_256_CBC_HMAC_SHA_512-Random"
    },
	"properties" : {
		"name": {
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
