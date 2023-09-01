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

import org.bson.BsonBinary;
import org.bson.BsonBoolean;
import org.bson.BsonDocument;
import org.bson.BsonInt32;
import org.bson.BsonString;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.Binary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;
import java.util.*;

@Service
public class KeyManagerServiceImpl implements KeyManagerService {
    protected static Logger log = LoggerFactory.getLogger(KeyManagerServiceImpl.class);

    @Value("${spring.data.mongodb.keyvault.uri}")
    public String connectionString;
    @Value("${spring.data.mongodb.keyvault.database}")
    public String keyVaultDb;
    @Value("${spring.data.mongodb.keyvault.collection}")
    public String keyVaultColl;

    @Override
    public UUID generateKey(Map<String, Map<String, Object>> kmsProviders, String kmsProvider) {
        return this.generateKey(kmsProviders, kmsProvider, "dataKey1");
    }

    @Override
    public UUID generateKey(Map<String, Map<String, Object>> kmsProviders, String kmsProvider, String keyAltName) {
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
                    .kmsProviders(kmsProviders)
                    .build();
            ClientEncryption clientEncryption = ClientEncryptions.create(clientEncryptionSettings);
            List<String> keyAltNames = new ArrayList<>();
            keyAltNames.add(keyAltName);

            String keyId = "1"; // ideally as spring configured param
            String kmipEndpoint = (String) kmsProviders.get(kmsProvider).get("endpoint");
            BsonDocument masterKey = new BsonDocument();
            masterKey.append("keyId", new BsonString(keyId))
                .append("endpoint", new BsonString(kmipEndpoint));

            BsonBinary bbDataKeyId = clientEncryption.createDataKey(
                    kmsProvider,
                    new DataKeyOptions().keyAltNames(keyAltNames).masterKey(masterKey));
            dataKeyId = this.toUUID(bbDataKeyId);
            clientEncryption.close();
        }

        log.info("DataKeyId [UUID]: " + dataKeyId);
        return dataKeyId;
    }

    @Override
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
