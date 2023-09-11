package com.mongodb.ps.csfleworkshop.ex4_manual_complete.client;

import java.util.HashMap;
import java.util.Map;

import org.bson.BsonBinary;
import org.bson.BsonDocument;
import org.bson.BsonValue;
import org.bson.UuidRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.convert.PropertyValueConverterFactory;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions.MongoConverterConfigurationAdapter;
import org.springframework.data.mongodb.core.convert.encryption.MongoEncryptionConverter;
import org.springframework.data.mongodb.core.encryption.Encryption;
import org.springframework.data.mongodb.core.encryption.EncryptionKeyResolver;
import org.springframework.data.mongodb.core.encryption.MongoClientEncryption;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import com.mongodb.AutoEncryptionSettings;
import com.mongodb.ClientEncryptionSettings;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.vault.ClientEncryption;
import com.mongodb.client.vault.ClientEncryptions;
import com.mongodb.ps.csfleworkshop.ex4_manual_complete.repositories.EmployeeRepository4M;
import com.mongodb.ps.csfleworkshop.services.KeyGenerationService;

// @Component
@Configuration
@EnableMongoRepositories(basePackageClasses = EmployeeRepository4M.class, mongoTemplateRef = "ex4MongoTemplate")
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
	protected ApplicationContext appContext;

    public Ex4MongoClientConfiguration(KeyGenerationService keyGenerationService, ApplicationContext applicationContext) {
        this.keyGenerationService = keyGenerationService;
        this.appContext = applicationContext;
    }

    @Override
    protected String getDatabaseName() {
        return encryptedDbName;
    }
    
    @Bean(name = "ex4MongoClient")
    public MongoClient mongoClient() {
        log.warn("Getting ### ex4MongoClient ###");

        // Get blank schema map
        Map<String, BsonDocument> schemaMap = new HashMap<String, BsonDocument>();

        String keyVaultNamespace = keyVaultDb + "." + keyVaultColl;

        MongoClientSettings.Builder mongoClientSettingsBuilder = MongoClientSettings.builder()
                .applyConnectionString(new ConnectionString(connectionString));
        
        MongoClientSettings clientSettings = mongoClientSettingsBuilder.build();
        MongoClient client = MongoClients.create(clientSettings);
        return client;
    }

    // @Primary
    //@Bean
    @Bean(name = "ex4MongoDBFactory")
    public MongoDatabaseFactory mongoDatabaseFactory(@Qualifier("ex4MongoClient") MongoClient mongoClient) {
        log.warn("Getting ### ex4MongoDBFactory ###");
        return new SimpleMongoClientDatabaseFactory(mongoClient, this.getDatabaseName());
    }

    // @Primary
    //public MongoTemplate mongoTemplate(@Qualifier(value="ex4MongoDBFactory") MongoDatabaseFactory mongoDatabaseFactory) {
    @Bean(name = "ex4MongoTemplate")
    public MongoTemplate mongoTemplate(@Qualifier(value="ex4MongoDBFactory") MongoDatabaseFactory mongoDatabaseFactory) {
        log.warn("Getting ### ex4MongoTemplate ###");
        return new MongoTemplate(mongoDatabaseFactory);
    }

    // @Primary
    @Bean(name = "ex4ClientEncryption")
    ClientEncryption clientEncryption() {
        log.warn("Getting ### ex4ClientEncryption ###");
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

    // @Primary
    @Bean(name = "ex4Converter")
    MongoEncryptionConverter mongoEncryptionConverter(@Qualifier("ex4ClientEncryption")ClientEncryption clientEncryption) {
        log.warn("Getting ### ex4Converter ###");
        Encryption<BsonValue, BsonBinary> encryption = MongoClientEncryption.just(clientEncryption);
        EncryptionKeyResolver keyResolver = EncryptionKeyResolver.annotated((ctx) -> null);
        return new MongoEncryptionConverter(encryption, keyResolver);
    }

    /*
     * 
     */
    @Override
    protected void configureConverters(MongoConverterConfigurationAdapter adapter) {
        log.warn("### Ex4 Configuring Converters ###");
        // Need different type of converter....
        // adapter.registerConverter(this.mongoEncryptionConverter(this.clientEncryption()));
        adapter.registerPropertyValueConverterFactory(
                PropertyValueConverterFactory.beanFactoryAware(appContext));
    }
}
