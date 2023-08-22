package com.mongodb.ps.csfleworkshop.services;

import com.mongodb.ClientEncryptionSettings;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.vault.DataKeyOptions;
import com.mongodb.client.vault.ClientEncryption;
import com.mongodb.client.vault.ClientEncryptions;

import org.bson.*;
import org.bson.conversions.Bson;
import org.bson.types.Binary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.*;

@Service
@Qualifier("localKms")
public class KeyGenerationServiceLocalImpl implements KeyGenerationService {
    protected static Logger log = LoggerFactory.getLogger(KeyGenerationServiceLocalImpl.class);

    public static final String KMS_PROVIDER = "local";
    public static final String MASTER_KEY_FILE_PATH = "./master-key.txt";

    @Value("${spring.data.mongodb.keyvault.uri}")
    public String connectionString;
    @Value("${spring.data.mongodb.keyvault.database}")
    public String keyVaultDb;
    @Value("${spring.data.mongodb.keyvault.collection}")
    public String keyVaultColl;

    /**
     * Generate a local master key. In production scenarios, use a key management
     * service
     */
    public void generateLocalMasterKey() throws IOException {
        byte[] localMasterKeyWrite = new byte[96];
        new SecureRandom().nextBytes(localMasterKeyWrite);
        try (FileOutputStream stream = new FileOutputStream(MASTER_KEY_FILE_PATH)) {
            stream.write(localMasterKeyWrite);
        }
    }

    public Map<String, Map<String, Object>> getKmsProviders() {
        String kmsProvider = "local";

        byte[] localMasterKeyRead = new byte[96];

        try (FileInputStream fis = new FileInputStream(MASTER_KEY_FILE_PATH)) {
            if (fis.read(localMasterKeyRead) < 96)
                throw new Exception("Expected to find a file and read 96 bytes from file");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        Map<String, Object> keyMap = new HashMap<>();
        keyMap.put("key", localMasterKeyRead);

        Map<String, Map<String, Object>> kmsProviders = new HashMap<>();
        kmsProviders.put(kmsProvider, keyMap);

        return kmsProviders;
    }

    public UUID generateKey() {
        return this.generateKey("dataKey1");
    }

    public UUID generateKey(String keyAltName) {
        createIndexOnKeyVaultCollection();

        UUID dataKeyId = null;
        // find the key
        MongoClient keyVaultClient = MongoClients.create(connectionString);
        MongoCollection<Document> keyVaultCollection = keyVaultClient
                .getDatabase(keyVaultDb)
                .getCollection(keyVaultColl);
        Bson byKeyAltName = Filters.eq("keyAltNames", keyAltName);
        FindIterable<Document> keyDocs = keyVaultCollection.find(byKeyAltName);
        if (keyDocs.iterator().hasNext()) {
            Document keyDocument = keyDocs.first();
            Binary bDataKeyId = keyDocument.get("_id", Binary.class);
            dataKeyId = this.toUUID(bDataKeyId);
        } else {
            // If not present then create a new one
            MongoClientSettings mcs = MongoClientSettings.builder()
                    .applyConnectionString(new ConnectionString(connectionString))
                    .build();
            String keyVaultNamespace = keyVaultDb + "." + keyVaultColl;
            ClientEncryptionSettings clientEncryptionSettings = ClientEncryptionSettings.builder()
                    .keyVaultMongoClientSettings(mcs)
                    .keyVaultNamespace(keyVaultNamespace)
                    .kmsProviders(this.getKmsProviders())
                    .build();
            ClientEncryption clientEncryption = ClientEncryptions.create(clientEncryptionSettings);
            List<String> keyAltNames = new ArrayList<>();
            keyAltNames.add(keyAltName);
            BsonBinary bbDataKeyId = clientEncryption.createDataKey(
                    KMS_PROVIDER,
                    new DataKeyOptions().keyAltNames(keyAltNames));
            dataKeyId = this.toUUID(bbDataKeyId);
            clientEncryption.close();
        }

        log.info("DataKeyId [UUID]: " + dataKeyId);
        return dataKeyId;
    }

    public void deleteKey(String keyAltName) {
        MongoClient keyVaultClient = MongoClients.create(connectionString);
        MongoCollection<Document> keyVaultCollection = keyVaultClient.getDatabase(keyVaultDb)
                .getCollection(keyVaultColl);
        keyVaultCollection.deleteOne(Filters.eq("keyAltNames", keyAltName));
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

    private void createIndexOnKeyVaultCollection() {
        MongoClient keyVaultClient = MongoClients.create(connectionString);
        // keyVaultClient.getDatabase(KEY_VAULT_DB).getCollection(KEY_VAULT_COLL).drop();
        // keyVaultClient.getDatabase(<<DB Name>>).getCollection(<<Collection>>).drop();
        MongoCollection<Document> keyVaultCollection = keyVaultClient.getDatabase(keyVaultDb)
                .getCollection(keyVaultColl);
        IndexOptions indexOpts = new IndexOptions()
                .partialFilterExpression(
                        new BsonDocument("keyAltNames", new BsonDocument("$exists", new BsonBoolean(true))))
                .unique(true);
        keyVaultCollection.createIndex(new BsonDocument("keyAltNames", new BsonInt32(1)), indexOpts);
        keyVaultClient.close();
    }
}
