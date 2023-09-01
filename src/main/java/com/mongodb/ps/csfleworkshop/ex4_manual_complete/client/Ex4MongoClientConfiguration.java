package com.mongodb.ps.csfleworkshop.ex4_manual_complete.client;

import java.util.HashMap;
import java.util.Map;

import org.bson.BsonDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import com.mongodb.AutoEncryptionSettings;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.ps.csfleworkshop.ex4_manual_complete.repositories.EmployeeRepository4X;
import com.mongodb.ps.csfleworkshop.services.KeyGenerationService;

@EnableMongoRepositories(basePackageClasses = EmployeeRepository4X.class, mongoTemplateRef = "ex4MongoTemplate")
public class Ex4MongoClientConfiguration extends AbstractMongoClientConfiguration {

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

    protected static Logger log = LoggerFactory.getLogger(Ex4MongoClientConfiguration.class);

    private final KeyGenerationService keyGenerationService;

    public Ex4MongoClientConfiguration(KeyGenerationService keyGenerationService) {
        this.keyGenerationService = keyGenerationService;
    }

    @Override
    protected String getDatabaseName() {
        return encryptedDbName;
    }
    
    @Bean(name = "ex4MongoClient")
    public MongoClient mongoClient() {
        log.info("Getting ### ex4MongoClient ###");

        // Get blank schema map
        Map<String, BsonDocument> schemaMap = new HashMap<String, BsonDocument>();

        String keyVaultNamespace = keyVaultDb + "." + keyVaultColl;

        MongoClientSettings.Builder mongoClientSettingsBuilder = MongoClientSettings.builder()
                .applyConnectionString(new ConnectionString(connectionString));
        
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

        MongoClientSettings clientSettings = mongoClientSettingsBuilder.build();
        MongoClient client = MongoClients.create(clientSettings);
        return client;
    }

    @Bean(name = "ex4MongoDBFactory")
    public MongoDatabaseFactory mongoDatabaseFactory(@Qualifier("ex4MongoClient") MongoClient mongoClient) {
        return new SimpleMongoClientDatabaseFactory(mongoClient, this.getDatabaseName());
    }

    @Bean(name = "ex4MongoTemplate")
    public MongoTemplate mongoTemplate(@Qualifier("ex4MongoDBFactory") MongoDatabaseFactory mongoDatabaseFactory) {
        return new MongoTemplate(mongoDatabaseFactory);
    }
}