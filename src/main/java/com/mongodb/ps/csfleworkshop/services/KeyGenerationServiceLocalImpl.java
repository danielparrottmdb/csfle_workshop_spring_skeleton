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
import com.mongodb.ps.csfleworkshop.CsfleworkshopApplication;

import org.bson.*;
import org.bson.conversions.Bson;
import org.bson.types.Binary;
import org.springframework.beans.factory.annotation.Qualifier;
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

    String KMS_PROVIDER = "local";
    String MASTER_KEY_FILE_PATH = "./master-key.txt";

    /**
     * Generate a local master key. In production scenarios, use a key management service
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

    public UUID generateKey(String keyVaultNamespace, Map<String, Map<String, Object>> kmsProviders, String connectionString) {
        return this.generateKey(keyVaultNamespace, kmsProviders, connectionString, "demo-data-key");
    }

    public UUID generateKey(String keyVaultNamespace, Map<String, Map<String, Object>> kmsProviders, String connectionString, String keyAltName) {
        createIndexOnKeyVaultCollection(connectionString);

        UUID dataKeyId = null;
        // find the key
        MongoClient keyVaultClient = MongoClients.create(connectionString);
        MongoCollection<Document> keyVaultCollection = keyVaultClient.getDatabase(CsfleworkshopApplication.KEY_VAULT_DB).getCollection(CsfleworkshopApplication.KEY_VAULT_COLL);
        Bson byKeyAltName = Filters.eq("keyAltNames", keyAltName);
        FindIterable<Document> keyDocs = keyVaultCollection.find(byKeyAltName);
        if (keyDocs.iterator().hasNext()) {
            Document keyDocument = keyDocs.first();
            Binary bDataKeyId = keyDocument.get("_id", Binary.class);
            dataKeyId = this.toUUID(bDataKeyId);
        } else {
            // If not present then create a new one 
            MongoClientSettings mcs = MongoClientSettings.builder().applyConnectionString(new ConnectionString(connectionString)).build();
            ClientEncryptionSettings clientEncryptionSettings = ClientEncryptionSettings.builder().keyVaultMongoClientSettings(mcs).keyVaultNamespace(keyVaultNamespace).kmsProviders(kmsProviders).build();
            ClientEncryption clientEncryption = ClientEncryptions.create(clientEncryptionSettings);
            List<String> keyAltNames = new ArrayList<>();
            keyAltNames.add(keyAltName);
            BsonBinary bbDataKeyId = clientEncryption.createDataKey(KMS_PROVIDER, new DataKeyOptions().keyAltNames(keyAltNames));
            dataKeyId = this.toUUID(bbDataKeyId);
            clientEncryption.close();
        }
        //String base64DataKeyId = Base64.getEncoder().encodeToString(binaryKeyId);
        //System.out.println("DataKeyId [base64]: " + base64DataKeyId);
        //UUID uuidKey = new UUID()
        System.out.println("DataKeyId [UUID]: " + dataKeyId);
        return dataKeyId;
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

    private void createIndexOnKeyVaultCollection(String connectionString) {
        MongoClient keyVaultClient = MongoClients.create(connectionString);
        //keyVaultClient.getDatabase(KEY_VAULT_DB).getCollection(KEY_VAULT_COLL).drop();
        // keyVaultClient.getDatabase(<<DB Name>>).getCollection(<<Collection>>).drop();
        MongoCollection<Document> keyVaultCollection = keyVaultClient.getDatabase(CsfleworkshopApplication.KEY_VAULT_DB).getCollection(CsfleworkshopApplication.KEY_VAULT_COLL);
        IndexOptions indexOpts = new IndexOptions().partialFilterExpression(new BsonDocument("keyAltNames", new BsonDocument("$exists", new BsonBoolean(true)))).unique(true);
        keyVaultCollection.createIndex(new BsonDocument("keyAltNames", new BsonInt32(1)), indexOpts);
        keyVaultClient.close();
    }
}
