package com.mongodb.ps.csfleworkshop.services;

import java.util.Map;
import java.util.UUID;

public interface KeyGenerationService {

    // void generateLocalMasterKey() throws IOException;

    Map<String, Map<String, Object>> getKmsProviders();

    UUID generateKey();

    UUID generateKey(String keyAltName);
}
