package com.mongodb.ps.csfleworkshop.services;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

@Service
@Primary
public class KeyGenerationServiceKmipImpl implements KeyGenerationService {
    protected static Logger log = LoggerFactory.getLogger(KeyGenerationServiceKmipImpl.class);

    public static final String KMS_PROVIDER = "kmip";

    @Value("${spring.data.mongodb.keyvault.uri}")
    public String connectionString;
    @Value("${spring.data.mongodb.keyvault.database}")
    public String keyVaultDb;
    @Value("${spring.data.mongodb.keyvault.collection}")
    public String keyVaultColl;
    @Value("${spring.data.mongodb.kmip.endpoint}")
    public String kmipEndpoint;

    KeyManagerService keyManager;

    public KeyGenerationServiceKmipImpl(KeyManagerService keyManager) {
        this.keyManager = keyManager;
    }

    @Override
    public Map<String, Map<String, Object>> getKmsProviders() {
        Map<String, Object> kmipMap = new HashMap<String, Object>();
        kmipMap.put("endpoint", kmipEndpoint);
        Map<String, Map<String, Object>> kmsProviders = new HashMap<>();
        kmsProviders.put(KMS_PROVIDER, kmipMap);

        return kmsProviders;
    }


    @Override
    public void deleteKey(String keyAltName) {
        this.keyManager.deleteKey(keyAltName);
    }


    @Override
    public UUID generateKey() {
        return this.keyManager.generateKey(this.getKmsProviders(), KMS_PROVIDER);
    }


    @Override
    public UUID generateKey(String keyAltName) {
        return this.keyManager.generateKey(this.getKmsProviders(), KMS_PROVIDER, keyAltName);
    }
    
}
