package com.mongodb.ps.csfleworkshop;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.data.convert.PropertyValueConverterFactory;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions.MongoConverterConfigurationAdapter;
import org.springframework.data.mongodb.core.convert.encryption.MongoEncryptionConverter;
import org.springframework.data.mongodb.core.encryption.Encryption;
import org.springframework.data.mongodb.core.encryption.EncryptionKeyResolver;
import org.springframework.data.mongodb.core.encryption.MongoClientEncryption;
// import org.springframework.data.repository.support.Repositories;

import com.mongodb.AutoEncryptionSettings;
import com.mongodb.ClientEncryptionSettings;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.vault.ClientEncryption;
import com.mongodb.client.vault.ClientEncryptions;
import com.mongodb.ps.csfleworkshop.ex0_test_case.TestCaseExercise;
import com.mongodb.ps.csfleworkshop.ex7_use_case_complete.UseCaseCompleteExercise;
/*
import com.mongodb.ps.csfleworkshop.ex7_use_case_complete.models.Employee;
import com.mongodb.ps.csfleworkshop.ex7_use_case_complete.models.EmployeeName;
import com.mongodb.ps.csfleworkshop.ex7_use_case_complete.repositories.EmployeeRepository;
 */
import com.mongodb.ps.csfleworkshop.services.KeyGenerationService;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bson.BsonBinary;
import org.bson.BsonDocument;
import org.bson.BsonValue;
import org.bson.UuidRepresentation;
// import org.bson.types.ObjectId;
import org.slf4j.Logger;

@SpringBootApplication
public class CsfleworkshopApplication extends AbstractMongoClientConfiguration implements CommandLineRunner {

    @Autowired
    ApplicationContext appContext;

    @Value("${spring.data.mongodb.keyvault.uri}")
    private String keyVaultConnectionString;
    @Value("${spring.data.mongodb.keyvault.database}")
    public String keyVaultDb = "encryptionVault";
    @Value("${spring.data.mongodb.keyvault.collection}")
    public String keyVaultColl = "keyVault";

    @Value("${spring.data.mongodb.uri}")
    private String connectionString;
    @Value("${spring.data.mongodb.database}")
    private String encryptedDbName;
    @Value("${spring.data.mongodb.collection}")
    private String encryptedCollName;
    @Value("${crypt.shared.lib.path}")
    private String CRYPT_SHARED_LIB_PATH;

	@Value("${csfle.exercise}")
	private int csfleExerciseNumber;

	private CsfleExercise csfleExercise;

    private final KeyGenerationService keyGenerationService;
	protected static Logger log = LoggerFactory.getLogger(CsfleworkshopApplication.class);

	public CsfleworkshopApplication(KeyGenerationService keyGenerationService) {
		this.keyGenerationService = keyGenerationService;
	}

	public static void main(String[] args) {
		log.warn("Here we go... " + args.length);
		for (String arg: args) {
			log.warn("######### Arg: " + arg);
		}
		SpringApplication.run(CsfleworkshopApplication.class, args);
	}

	public void run (String... args) {
		log.info("Running CsfleworkshopApplication CLI");
		// Debugs don't log by default
		log.debug("won't log");

		CsfleExercise exercise = this.getExercise();
		exercise.runExercise(appContext);
		/*
		Employee e = new Employee(new EmployeeName("Bugs", "Bunny"), "Shh it's a secret",
			Arrays.asList("IC"), 78000.0);
		EmployeeRepository employeeRepository = this.getEmployeeRepository();
		ObjectId eId = employeeRepository.insert(e).getId();
		log.info("eId: " + eId);
		Employee e2 = employeeRepository.findById(eId.toString()).get();
		log.info("e2: " + e2 + ";" + e2.getSalary());
		 */
	}

	/*
	 * 
	public EmployeeRepository getEmployeeRepository() {
		Repositories repos = new Repositories(appContext);
		EmployeeRepository repo = (EmployeeRepository) repos.getRepositoryFor(Employee.class).get();
		return repo;
	}
	 */

	// @Override
	public String getDatabaseName() {
		return encryptedDbName;
	}

    @Bean
    public MongoClient mongoClient() {

        log.info("Getting MongoClient; exercise: " + csfleExerciseNumber);


        // This key is unused locally but ensures the second-data-key used for explicit encryption exists 
        keyGenerationService.generateKey("second-data-key");
        final UUID dataKey1 = keyGenerationService.generateKey();

		// Get schema map
		CsfleExercise exercise = this.getExercise();
		BsonDocument schema = exercise.getSchemaDocument(dataKey1);
		Map<String, BsonDocument> schemaMap = new HashMap<String, BsonDocument>();
		schemaMap.put(encryptedDbName + "." + encryptedCollName, schema);

        Map<String, Object> extraOptions = new HashMap<String, Object>();
        // extraOptions.put("cryptSharedLibPath", CRYPT_SHARED_LIB_PATH);
        // extraOptions.put("cryptSharedLibRequired", true);
        extraOptions.put("mongocryptdBypassSpawn", true);

        String keyVaultNamespace = keyVaultDb + "." + keyVaultColl;
        MongoClientSettings clientSettings = MongoClientSettings.builder()
                .applyConnectionString(new ConnectionString(connectionString))
                .autoEncryptionSettings(AutoEncryptionSettings.builder()
                        .keyVaultMongoClientSettings(MongoClientSettings.builder()
                            .applyConnectionString(new ConnectionString(keyVaultConnectionString))
                            .build())
                        .keyVaultNamespace(keyVaultNamespace)
                        .kmsProviders(keyGenerationService.getKmsProviders())
                        .schemaMap(schemaMap)
                        .extraOptions(extraOptions)
                        .build())
                .build();

        MongoClient client = MongoClients.create(clientSettings);
        return client;
    }

	public CsfleExercise getExercise() {
		if (csfleExercise == null) {
			switch (csfleExerciseNumber) {
				case 0:
					csfleExercise = new TestCaseExercise();
					break;
				case 7:
					csfleExercise = new UseCaseCompleteExercise();
					break;
				default:
					throw new UnsupportedOperationException("Unknown exercise " + csfleExerciseNumber);
			}
		}

		return csfleExercise;
	}

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

    @Bean
    ClientEncryption clientEncryption() {
        ClientEncryptionSettings encryptionSettings = ClientEncryptionSettings.builder()
            .keyVaultNamespace(keyVaultDb + "." + keyVaultColl)
            .kmsProviders(keyGenerationService.getKmsProviders())
            .keyVaultMongoClientSettings(MongoClientSettings.builder()
                .applyConnectionString(new ConnectionString(connectionString))
                .uuidRepresentation(UuidRepresentation.STANDARD)
                .build()
            )
            .build();
        
        return ClientEncryptions.create(encryptionSettings);    
    }

    @Bean
    MongoEncryptionConverter mongoEncrpytionConverter(ClientEncryption clientEncryption) {
        Encryption<BsonValue, BsonBinary> encryption = MongoClientEncryption.just(clientEncryption);
        EncryptionKeyResolver keyResolver = EncryptionKeyResolver.annotated((ctx) -> null);             

        return new MongoEncryptionConverter(encryption, keyResolver);  
    }

    /*
     * 
     */
    @Override
    protected void configureConverters(MongoConverterConfigurationAdapter adapter) {
        adapter.registerPropertyValueConverterFactory(
            PropertyValueConverterFactory.beanFactoryAware(appContext)
        );
    }


	/**
	 * Get the schema document.
	 * 
	 * This gets the full document that will be used once the employee gets fully populated.
	 * @param dekUuid
	 * @return
	 */
    public BsonDocument _getSchemaDocument(UUID dekUuid) {
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
