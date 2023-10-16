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

import com.mongodb.AutoEncryptionSettings;
import com.mongodb.ClientEncryptionSettings;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.vault.ClientEncryption;
import com.mongodb.client.vault.ClientEncryptions;
import com.mongodb.ps.csfleworkshop.ex10_use_case_delete.UseCaseDeleteExercise;
import com.mongodb.ps.csfleworkshop.ex11_use_case_complete.UseCaseCompleteExercise;
import com.mongodb.ps.csfleworkshop.ex1_manual_encrypt.ManualEncryptExercise;
import com.mongodb.ps.csfleworkshop.ex2_manual_decrypt.ManualDecryptExercise;
import com.mongodb.ps.csfleworkshop.ex3_manual_complete.ManualCompleteExercise;
import com.mongodb.ps.csfleworkshop.ex4_manual_encrypt_auto_decrypt.ManualEncryptAutoDecryptExercise;
import com.mongodb.ps.csfleworkshop.ex5_auto_encrypt.AutoEncryptExercise;
import com.mongodb.ps.csfleworkshop.ex6_auto_decrypt.AutoDecryptExercise;
import com.mongodb.ps.csfleworkshop.ex7_auto_complete.AutoCompleteExercise;
import com.mongodb.ps.csfleworkshop.ex8_use_case_1_create.UseCaseOneCreateExercise;
import com.mongodb.ps.csfleworkshop.ex9_use_case_2_create.UseCaseTwoCreateExercise;
import com.mongodb.ps.csfleworkshop.services.KeyGenerationService;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bson.BsonBinary;
import org.bson.BsonDocument;
import org.bson.BsonValue;
import org.bson.UuidRepresentation;

import org.slf4j.Logger;


@SpringBootApplication
public class CsfleworkshopApplication extends AbstractMongoClientConfiguration implements CommandLineRunner {

    @Autowired
    ApplicationContext appContext;

    // TODO - make sure these values are correct in the application.properties
    // or mongodb.properties files under src/main/resources/
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

    // This can be in properties or at the command line,
    // e.g.  `mvn spring-boot:run -Dspring-boot.run.arguments=--csfle.exercise=7`
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

        // TODO - Keystores - make sure these are converted from the .pem and
        // .cert files and update the location / password as needed
        System.setProperty("javax.net.ssl.keyStore", "/home/ec2-user/keystore.jks");
        System.setProperty("javax.net.ssl.keyStorePassword", "mongodb");
        System.setProperty("javax.net.ssl.trustStore", "/home/ec2-user/truststore.jks");
        System.setProperty("javax.net.ssl.trustStorePassword","mongodb");

        for (String arg : args) {
            log.warn("######### Arg: " + arg);
        }
        SpringApplication.run(CsfleworkshopApplication.class, args);
    }

    public void run(String... args) {
        log.info("Running CsfleworkshopApplication CLI");
        // NB - Debugs don't log by default
        log.debug("won't log");

        CsfleExercise exercise = this.getExercise();
        exercise.runExercise();
    }

    @Override
    public String getDatabaseName() {
        return encryptedDbName;
    }

    @Bean
    public MongoClient mongoClient() {

        log.info("Getting MongoClient; exercise: " + csfleExerciseNumber);

        // This key is unused locally but ensures the second-data-key used for explicit
        // encryption exists
        keyGenerationService.generateKey("second-data-key");
        final UUID dataKey1 = keyGenerationService.generateKey();

        // Get schema map
        CsfleExercise exercise = this.getExercise();
        BsonDocument schema = exercise.getSchemaDocument(dataKey1);
        Map<String, BsonDocument> schemaMap = new HashMap<String, BsonDocument>();
        schemaMap.put(encryptedDbName + "." + encryptedCollName, schema);

        String keyVaultNamespace = keyVaultDb + "." + keyVaultColl;

        MongoClientSettings.Builder mongoClientSettingsBuilder = MongoClientSettings.builder()
                .applyConnectionString(new ConnectionString(connectionString));
        
        if (exercise.useAutoEncryption()) {
            Map<String, Object> extraOptions = new HashMap<String, Object>();
            // For using CRYPT_SHARED:
            extraOptions.put("cryptSharedLibPath", CRYPT_SHARED_LIB_PATH);
            extraOptions.put("cryptSharedLibRequired", true);
            extraOptions.put("mongocryptdBypassSpawn", true);

            mongoClientSettingsBuilder.autoEncryptionSettings(AutoEncryptionSettings.builder()
                    .keyVaultMongoClientSettings(MongoClientSettings.builder()
                            .applyConnectionString(new ConnectionString(keyVaultConnectionString))
                            .build())
                    .keyVaultNamespace(keyVaultNamespace)
                    .kmsProviders(keyGenerationService.getKmsProviders())
                    .schemaMap(schemaMap)
                    .extraOptions(extraOptions)
                    .build());
        }

        MongoClientSettings clientSettings = mongoClientSettingsBuilder.build();

        MongoClient client = MongoClients.create(clientSettings);
        return client;
    }

    public CsfleExercise getExercise() {
        if (csfleExercise == null) {
            switch (csfleExerciseNumber) {
                case 1:
                    csfleExercise = new ManualEncryptExercise(appContext);
                    break;
                case 2:
                    csfleExercise = new ManualDecryptExercise(appContext);
                    break;
                case 3:
                    csfleExercise = new ManualCompleteExercise(appContext);
                    break;
                case 4:
                    csfleExercise = new ManualEncryptAutoDecryptExercise(appContext, this.clientEncryption());
                    break;
                case 5:
                    csfleExercise = new AutoEncryptExercise(appContext);
                    break;   
                case 6:
                    csfleExercise = new AutoDecryptExercise(appContext);
                    break;   
                case 7:
                    csfleExercise = new AutoCompleteExercise(appContext);
                    break;   
                case 8:
                    csfleExercise = new UseCaseOneCreateExercise(appContext);
                    break;
                case 9:
                    csfleExercise = new UseCaseTwoCreateExercise(appContext);
                    break;
                case 10:
                    csfleExercise = new UseCaseDeleteExercise(appContext);
                    break;
                case 11:
                    csfleExercise = new UseCaseCompleteExercise(appContext);
                    break;
                default:
                    throw new UnsupportedOperationException("Unknown exercise " + csfleExerciseNumber);
            }
        }

        return csfleExercise;
    }

    @Bean
    ClientEncryption clientEncryption() {
        ClientEncryptionSettings encryptionSettings = ClientEncryptionSettings.builder()
                .keyVaultNamespace(keyVaultDb + "." + keyVaultColl)
                .kmsProviders(keyGenerationService.getKmsProviders())
                .keyVaultMongoClientSettings(MongoClientSettings.builder()
                        .applyConnectionString(new ConnectionString(connectionString))
                        .uuidRepresentation(UuidRepresentation.STANDARD)
                        .build())
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
                PropertyValueConverterFactory.beanFactoryAware(appContext));
    }
}
