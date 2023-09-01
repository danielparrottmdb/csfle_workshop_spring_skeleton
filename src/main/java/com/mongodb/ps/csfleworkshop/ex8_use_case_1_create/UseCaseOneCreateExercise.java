package com.mongodb.ps.csfleworkshop.ex8_use_case_1_create;

import java.nio.ByteBuffer;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import org.bson.BsonBinary;
import org.bson.BsonDocument;
import org.bson.BsonString;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.Binary;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.data.repository.support.Repositories;

import com.github.javafaker.Faker;
import com.mongodb.ClientEncryptionSettings;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.vault.DataKeyOptions;
import com.mongodb.client.vault.ClientEncryption;
import com.mongodb.client.vault.ClientEncryptions;
import com.mongodb.ps.csfleworkshop.CsfleExercise;
import com.mongodb.ps.csfleworkshop.ex8_use_case_1_create.models.Employee;
import com.mongodb.ps.csfleworkshop.ex8_use_case_1_create.models.EmployeeAddress;
import com.mongodb.ps.csfleworkshop.ex8_use_case_1_create.models.EmployeeName;
import com.mongodb.ps.csfleworkshop.ex8_use_case_1_create.repositories.EmployeeRepository8;
import com.mongodb.ps.csfleworkshop.services.KeyGenerationService;
import com.mongodb.ps.csfleworkshop.services.KeyGenerationServiceKmipImpl;

public class UseCaseOneCreateExercise implements CsfleExercise {
	protected static Logger log = LoggerFactory.getLogger(UseCaseOneCreateExercise.class);

	protected ApplicationContext appContext;

	public UseCaseOneCreateExercise(ApplicationContext applicationContext) {
		this.appContext = applicationContext;
	}

	public EmployeeRepository8 getEmployeeRepository(ApplicationContext appContext) {
		Repositories repos = new Repositories(appContext);
		EmployeeRepository8 repo = (EmployeeRepository8) repos.getRepositoryFor(Employee.class).get();
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
		KeyGenerationServiceKmipImpl keyGenerationService = (KeyGenerationServiceKmipImpl) this.getKeyGenerationService(appContext);
		String connectionString = keyGenerationService.connectionString;
		String keyVaultDb = keyGenerationService.keyVaultDb;
		String keyVaultColl = keyGenerationService.keyVaultColl;

        UUID employeeDEKId = null;
        // find the key
        MongoClient keyVaultClient = MongoClients.create(connectionString);
        MongoCollection<Document> keyVaultCollection = keyVaultClient
                .getDatabase(keyVaultDb)
                .getCollection(keyVaultColl);
        Bson byKeyAltName = Filters.eq("keyAltNames", employeeId);
        FindIterable<Document> keyDocs = keyVaultCollection.find(byKeyAltName);
        if (keyDocs.iterator().hasNext()) {
            Document keyDocument = keyDocs.first();
            Binary bDataKeyId = keyDocument.get("_id", Binary.class);
            employeeDEKId = this.toUUID(bDataKeyId);
        } else {
            // If not present then create a new one
			Map<String, Map<String,Object>> kmsProviders = keyGenerationService.getKmsProviders();
            MongoClientSettings mcs = MongoClientSettings.builder()
                    .applyConnectionString(new ConnectionString(connectionString))
                    .build();
            String keyVaultNamespace = keyVaultDb + "." + keyVaultColl;
            ClientEncryptionSettings clientEncryptionSettings = ClientEncryptionSettings.builder()
                    .keyVaultMongoClientSettings(mcs)
                    .keyVaultNamespace(keyVaultNamespace)
                    .kmsProviders(kmsProviders)
                    .build();
            ClientEncryption clientEncryption = ClientEncryptions.create(clientEncryptionSettings);

            String keyId = "1"; // as spring configured param
            String kmipEndpoint = (String) kmsProviders.get(keyGenerationService.KMS_PROVIDER).get("endpoint");
            BsonDocument masterKey = new BsonDocument();
            List<String> keyAltNames = new ArrayList<>();

			// TODO - UNCOMMENT AND PUT CODE HERE TO CREATE THE NEW DEK USING THE MASTER KEY
			/*
            masterKey.append( ...
            keyAltNames.add( ... 
            BsonBinary bbDataKeyId = clientEncryption.createDataKey( ...
            employeeDEKId = this.toUUID(bbDataKeyId);
			 */
            clientEncryption.close();
        }

		log.info("employeeDEKId: " + employeeDEKId);

		// Insert the employee doc
		EmployeeRepository8 employeeRepository = this.getEmployeeRepository(appContext);
		String eId = employeeRepository.insert(e).getId();
		log.info("eId: " + eId);

		// TODO - Find using deterministically encrypted first and last names
		EmployeeName nameQuery = null;
		Employee e2 = null;
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
		// TODO - use JSON Pointers for the employee DEK
		String schemaJson = """
				{
				    "bsonType" : "object",
				    "encryptMetadata" : {
				        "keyId": "/_id", // TODO - PUT APPROPRIATE CODE OR VARIABLE HERE
				        "algorithm" : "AEAD_AES_256_CBC_HMAC_SHA_512-Random"
				    },
				    "properties" : {
				        "name" : {
				            "bsonType": "object",
				            "properties" : {
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

    public UUID toUUID(Binary b) {
        return this.toUUID(b.getData());
    }

    public UUID toUUID(BsonBinary b) {
        return this.toUUID(b.getData());
    }

    public UUID toUUID(byte[] b) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(b);
        long high = byteBuffer.getLong();
        long low = byteBuffer.getLong();
        return new UUID(high, low);
    }
}
