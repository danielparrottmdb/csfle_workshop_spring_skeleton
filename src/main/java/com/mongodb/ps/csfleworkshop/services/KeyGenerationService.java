package com.mongodb.ps.csfleworkshop.services;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

public interface KeyGenerationService {

    void generateLocalMasterKey() throws IOException;

    Map<String, Map<String, Object>> getKmsProviders();

    UUID generateLocalKeyId(String keyVaultNamespace, Map<String, Map<String, Object>> kmsProviders,
                                     String connectionString);

    UUID generateLocalKeyId(String keyVaultNamespace, Map<String, Map<String, Object>> kmsProviders,
                                     String connectionString, String keyAltName);
}
